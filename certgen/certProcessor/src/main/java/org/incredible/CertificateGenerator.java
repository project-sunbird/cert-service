package org.incredible;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.certProcessor.store.CertStoreFactory;
import org.incredible.certProcessor.store.ICertStore;
import org.incredible.certProcessor.store.StoreConfig;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates certificate json, qrcode , html and pdf
 */
public class CertificateGenerator {


    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    private Map<String, String> properties;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CertificateFactory certificateFactory = new CertificateFactory();

    private String directory = "conf/";

    public CertificateGenerator(Map<String, String> properties, String directory) {
        this.properties = properties;
        this.directory = directory;
    }

    public CertificateGenerator(Map<String, String> properties) {
        this.properties = properties;
    }

    public CertificateExtension getCertificateExtension (CertModel certModel) throws SignatureException.UnreachableException,
            InvalidDateFormatException, SignatureException.CreationException, IOException {
        return certificateFactory.createCertificate(certModel, properties);
    }

    public Map<String,Object> generateQR(CertificateExtension certificateExtension) throws WriterException, FontFormatException,
            NotFoundException, IOException {
        Map<String,Object> qrMap = generateQrCode(certificateExtension);
        String qrImageUrl = uploadQrCode((File)qrMap.get(JsonKey.QR_CODE_FILE));
        qrMap.put(JsonKey.QR_IMAGE_URL,qrImageUrl);
        return qrMap;
    }

    public String getUUID(String certId) {
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

    public String generateCertificateJson(CertificateExtension certificateExtension) throws IOException {
        checkDirectoryExists();
        File file = new File(directory + getUUID(certificateExtension.getId()) + ".json");
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.writeValue(file, certificateExtension);
        String jsonData = objectMapper.writeValueAsString(certificateExtension);
        logger.info("Json file has been generated for the certificate");
        return jsonData;
    }

    private void checkDirectoryExists() {
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private Map<String,Object> generateQrCode(CertificateExtension certificateExtension) throws WriterException,
            FontFormatException, NotFoundException, IOException {
        Map<String,Object> qrMap = new HashMap<>();
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(Double.valueOf(properties.get(JsonKey.ACCESS_CODE_LENGTH)));
        String accessCode = accessCodeGenerator.generate();
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        qrCodeGenerationModel.setText(accessCode);
        qrCodeGenerationModel.setFileName(directory + getUUID(certificateExtension.getId()));
        qrCodeGenerationModel.setData(properties.get(JsonKey.BASE_PATH).concat("/") + getUUID(certificateExtension.getId()));
        QRCodeImageGenerator qrCodeImageGenerator = new QRCodeImageGenerator();
        File qrCodeFile = qrCodeImageGenerator.createQRImages(qrCodeGenerationModel);

        qrMap.put(JsonKey.QR_CODE_FILE,qrCodeFile);
        qrMap.put(JsonKey.ACCESS_CODE,accessCode);
        logger.info("Qrcode {} is created for the certificate", qrCodeFile.getName());
        return qrMap;
    }

    private String uploadQrCode(File qrCodeFile) throws IOException {
        CertStoreFactory certStoreFactory = new CertStoreFactory(properties);
        StoreConfig storeParams = new StoreConfig(getStorageParamsFromEnv());
        ICertStore certStore = certStoreFactory.getCertStore(storeParams, BooleanUtils.toBoolean(properties.get(JsonKey.PREVIEW)));
        certStore.init();
        String qrImageUrl = certStore.saveFile(qrCodeFile, certStoreFactory.setCloudPath(storeParams));
        logger.info("Qrcode {} is created for the certificate", qrCodeFile.getName());
        return qrImageUrl;
    }

    private Map<String, Object> getStorageParamsFromEnv() {
        CertsConstant certVar = new CertsConstant();
        Map<String, Object> storeParams = certVar.getStorageParamsFromEvn(JsonKey.PUBLIC);
        return storeParams;
    }
}