package org.incredible;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.certProcessor.views.HTMLGenerator;
import org.incredible.certProcessor.views.HTMLTemplateProvider;
import org.incredible.certProcessor.views.PdfConverter;
import org.incredible.certProcessor.views.PdfSignature;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.CertificateResponse;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Generates certificate json, qrcode , html and pdf
 */
public class CertificateGenerator {


    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    private Map<String, String> properties;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CertificateFactory certificateFactory = new CertificateFactory();

    private String directory;

    public CertificateGenerator(Map<String, String> properties, String directory) {
        this.properties = properties;
        this.directory = directory;
    }


    public CertificateResponse createCertificate(CertModel certModel, HTMLTemplateProvider htmlTemplateProvider) throws
            SignatureException.UnreachableException, InvalidDateFormatException, SignatureException.CreationException,
            IOException, FontFormatException, NotFoundException, WriterException, DocumentException, GeneralSecurityException {
        String uuid;
        CertificateExtension certificateExtension = certificateFactory.createCertificate(certModel, properties);
        String jsonData = generateCertificateJson(certificateExtension);
        String accessCode = generateQrCodeForCertificate(certificateExtension);
        String htmlContent = htmlTemplateProvider.getTemplateContent(directory);
        if (htmlTemplateProvider.checkHtmlTemplateIsValid(htmlContent)) {
            HTMLGenerator htmlGenerator = new HTMLGenerator(htmlContent);
            htmlGenerator.generate(certificateExtension, directory);
            uuid = getUUID(certificateExtension.getId());
            generatePdf(uuid);
        } else {
            return new CertificateResponse();
        }
        return new CertificateResponse(uuid, accessCode, jsonData, certModel.getIdentifier());
    }

    private String getUUID(String certId) {
        String idStr;
        try {
            URI uri = new URI(certId);
            String path = uri.getPath();
            idStr = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            return null;
        }
        return StringUtils.substringBefore(idStr, ".");
    }

    private void generatePdf(String uuid) throws IOException, GeneralSecurityException, DocumentException {
        PdfConverter pdfConverter = new PdfConverter();
        if (BooleanUtils.toBoolean(properties.get(JsonKey.SIGN_PDF))) {
            PdfSignature pdfSignature = new PdfSignature();
            pdfConverter.converter(new File(directory + uuid + ".html"), directory + uuid + "unsigned.pdf");
            pdfSignature.sign(directory.concat(uuid + "unsigned.pdf"), directory.concat(uuid + ".pdf"));
        } else {
            pdfConverter.converter(new File(directory + uuid + ".html"), directory.concat(uuid + ".pdf"));
        }
    }

    private String generateCertificateJson(CertificateExtension certificateExtension) {
        checkDirectoryExists();
        File file = new File(directory + getUUID(certificateExtension.getId()) + ".json");
        String jsonData = null;
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.writeValue(file, certificateExtension);
            jsonData = objectMapper.writeValueAsString(certificateExtension);
            logger.info("Json file has been generated for the certificate");
        } catch (IOException e) {
            logger.error("Exception while generating json");
        }
        return jsonData;
    }

    private void checkDirectoryExists() {
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private String generateQrCodeForCertificate(CertificateExtension certificateExtension) throws WriterException,
            FontFormatException, NotFoundException, IOException {
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(Double.valueOf(properties.get(JsonKey.ACCESS_CODE_LENGTH)));
        String accessCode = accessCodeGenerator.generate();
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        qrCodeGenerationModel.setText(accessCode);
        qrCodeGenerationModel.setFileName(directory + getUUID(certificateExtension.getId()));
        qrCodeGenerationModel.setData(properties.get(JsonKey.DOMAIN_URL).concat("/") +
                properties.get(JsonKey.SLUG).concat("/") + getUUID(certificateExtension.getId()));
        QRCodeImageGenerator qrCodeImageGenerator = new QRCodeImageGenerator();
        File qrCode = qrCodeImageGenerator.createQRImages(qrCodeGenerationModel);
        logger.info("Qrcode {} is created for the certificate", qrCode.getName());
        return accessCode;
    }
}