package org.incredible.certProcessor.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.incredible.certProcessor.JsonKey;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.lang.StringUtils.capitalize;

public class PdfGenerator {

    private static Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String PRINT_SERVICE_URL = "print-service:5000/v1/print/pdf";

    public static String generate(String htmlTemplateUrl, CertificateExtension certificateExtension , Map<String,Object> qrMap,
                                  String container, String path) {
        try {
            Map<String, Object> printServiceReq = new HashMap<>();
            Map<String, Object> request = new HashMap<>();
            printServiceReq.put(JsonKey.REQUEST, request);
            request.put("context", getContext(certificateExtension, qrMap));
            request.put(JsonKey.HTML_TEMPLATE, htmlTemplateUrl);
            Map<String, String> storageParams = new HashMap<>();
            storageParams.put(JsonKey.containerName,container);
            storageParams.put(JsonKey.PATH,path);
            request.put("storageParams",storageParams);
            String pdfUrl = callPrintService(printServiceReq);
            String [] arr = pdfUrl.split("/");
            return "/"+path+arr[arr.length-1];
        } catch (Exception ex) {
            logger.error("Exception occurred while generating pdf :: "+ex.getMessage(),ex);
        }
        return "";
    }

    private static  Map<String,Object> getContext(CertificateExtension certificateExtension, Map<String, Object> qrMap) {
        Map<String,Object> context = new HashMap<>();
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        List<String> supportedVarList = HTMLVars.get();
        Iterator<String> iterator = supportedVarList.iterator();
        while (iterator.hasNext()) {
            String macro = iterator.next().substring(1);
            try {
                Method method = htmlVarResolver.getClass().getMethod("get" + capitalize(macro));
                method.setAccessible(true);
                context.put(macro, method.invoke(htmlVarResolver));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error("exception "+ e.getMessage(),e);
            }
        }
        context.put("qrCodeImage",qrMap.get("qrImageUrl"));
        return context;
    }

    private static String callPrintService(Map<String, Object> request) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(PRINT_SERVICE_URL);
        String json = mapper.writeValueAsString(request);
        json = new String(json.getBytes(), StandardCharsets.UTF_8);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(httpPost);
        String pdfUrl = generateResponse(response);
        return pdfUrl;
    }

    private static String generateResponse(HttpResponse httpResponse) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader br =
                new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));
        String output;
        while ((output = br.readLine()) != null) {
            builder.append(output);
        }
        Map<String,Object> resMap = mapper.readValue(builder.toString(),Map.class);
        Map<String,Object> printResponse = (Map<String,Object>)resMap.get(JsonKey.RESULT);
        String pdfUrl = (String)(printResponse.get(JsonKey.PDF_URL));
        return pdfUrl;
    }

}
