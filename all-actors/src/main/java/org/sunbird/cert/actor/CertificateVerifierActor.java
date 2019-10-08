package org.sunbird.cert.actor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.certProcessor.store.CertStoreFactory;
import org.incredible.certProcessor.store.ICertStore;
import org.incredible.certProcessor.store.StoreConfig;
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.CertsConstant;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * This actor is responsible for certificate verification.
 *
 */
@ActorConfig(
        tasks = {JsonKey.VERIFY_CERT},
        asyncTasks = {}
)
public class CertificateVerifierActor extends BaseActor {

    private Logger logger = Logger.getLogger(CertificateVerifierActor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private CertsConstant certsConstant = new CertsConstant();

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.VERIFY_CERT.equalsIgnoreCase(operation)) {
            verifyCertificate(request);
        }
    }

    private void verifyCertificate(Request request) throws BaseException {
        Boolean isVerify = false;
        Map<String, Object> certificate = new HashMap<>();
        try {
            if (((Map) request.get(JsonKey.CERTIFICATE)).containsKey(JsonKey.DATA)) {
                certificate = (Map<String, Object>) ((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.DATA);
            } else if (((Map) request.get(JsonKey.CERTIFICATE)).containsKey(JsonKey.UUID)) {
                certificate = downloadCertJson((String) ((Map<String, Object>) request.get(JsonKey.CERTIFICATE)).get(JsonKey.UUID));
            }
            logger.info("Certificate extension " + certificate);
            if (certificate.containsKey(JsonKey.SIGNATURE)) {
                isVerify = verifySignature(certificate);
            }
        } catch (IOException | SignatureException.UnreachableException | SignatureException.VerificationException ex) {
            logger.error("verifyCertificate:Exception Occurred while verifying certificate. : " + ex.getMessage());
            throw new BaseException(IResponseMessage.INTERNAL_ERROR, ex.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        }
        Map<String, Boolean> verifyRes = new HashMap<>();
        verifyRes.put(JsonKey.STATUS, isVerify);
        Response response = new Response();
        response.getResult().put("response", verifyRes);
        sender().tell(response, getSelf());
        logger.info("onReceive method call End");
    }


    private Map<String, Object> downloadCertJson(String uri) throws IOException, BaseException {
            String localPath = "conf/";
            StoreConfig storeConfig = new StoreConfig(certsConstant.getStorageParamsFromEvn());
            CertStoreFactory certStoreFactory = new CertStoreFactory(null);
            ICertStore certStore = certStoreFactory.getCloudStore(storeConfig);
            certStore.init();
            try {
                certStore.get(null, uri, localPath);
                File file = new File(localPath + getFileName(uri));
                Map<String, Object> certificate = mapper.readValue(file, new TypeReference<Map<String, Object>>() {
                });
                return certificate;
            } catch (StorageServiceException ex) {
                logger.error("downloadCertJson:Exception Occurred while downloading json certificate from the cloud. : " + ex.getMessage());
                throw new BaseException("INVALID_PARAM_VALUE", MessageFormat.format(IResponseMessage.INVALID_PARAM_VALUE,
                        uri, JsonKey.UUID), ResponseCode.CLIENT_ERROR.getCode());
            }
        }

    private String getFileName(String certId) {
        String idStr = null;
        try {
            URI uri = new URI(certId);
            String path = uri.getPath();
            idStr = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.debug("getUUID : exception occurred while getting file form the uri " + e.getMessage());
        }
        return idStr;
    }

    private Boolean verifySignature(Map<String, Object> certificateExtension) throws SignatureException.UnreachableException, SignatureException.VerificationException {
        String signatureValue = ((Map<String, String>) certificateExtension.get(JsonKey.SIGNATURE)).get(JsonKey.SIGNATURE_VALUE);
        certificateExtension.remove(JsonKey.SIGNATURE);
        JsonNode jsonNode = mapper.valueToTree(certificateExtension);
        CertificateFactory certificateFactory = new CertificateFactory();
        return certificateFactory.verifySignature(jsonNode, signatureValue, certsConstant.getEncryptionServiceUrl(),
                ((Map<String, String>) certificateExtension.get(JsonKey.VERIFICATION)).get(JsonKey.CREATOR));
    }


}
