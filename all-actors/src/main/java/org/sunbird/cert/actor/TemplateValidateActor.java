package org.sunbird.cert.actor;


import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.store.LocalStore;
import org.incredible.certProcessor.views.HTMLTemplateValidator;
import org.incredible.certProcessor.views.HTMLTemplateZip;
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.io.IOException;

/**
 * This actor is responsible for certificate verification.
 */
@ActorConfig(
        tasks = {JsonKey.VALIDATE_TEMPLATE},
        asyncTasks = {}
)
public class TemplateValidateActor extends BaseActor {

    private Logger logger = Logger.getLogger(TemplateValidateActor.class);

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.VALIDATE_TEMPLATE.equalsIgnoreCase(operation)) {
            validateTemplate(request);
        }
    }


    private void validateTemplate(Request request) throws BaseException {
        String templateUrl = (String) request.getRequest().get(JsonKey.TEMPLATE_URL);
        HTMLTemplateZip htmlTemplateZip = new HTMLTemplateZip(new LocalStore(null), templateUrl);
        htmlTemplateZip.init();
        HTMLValidatorResponse validatorResponse;

        //download the file

        try {
            htmlTemplateZip.download();
        } catch (StorageServiceException | IOException e) {
            logger.info("expec downloading" + e.getMessage());
            throw new BaseException(IResponseMessage.INTERNAL_ERROR, e.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        }
        //check if zip file downloaded or not if downloaded unzip

        if (htmlTemplateZip.isZipFileExists()) {
            htmlTemplateZip.unzip();
            if (htmlTemplateZip.isIndexHTMlFileExits()) {
                validatorResponse = validateHtml(htmlTemplateZip);
            } else {
                throw new BaseException(IResponseMessage.INTERNAL_ERROR, "zip file format is wrong , as we cound'nt find index.html file", ResponseCode.SERVER_ERROR.getCode());
            }

        } else {
            throw new BaseException(IResponseMessage.INTERNAL_ERROR, "zip file in the url does not exist", ResponseCode.SERVER_ERROR.getCode());
        }
        htmlTemplateZip.cleanUp();
        Response response = new Response();
        response.getResult().put("response", validatorResponse);

        sender().tell(response, getSelf());
        logger.info("onReceive method call End" + response.toString());
    }

    private HTMLValidatorResponse validateHtml(HTMLTemplateZip htmlTemplateZip) {
        HTMLValidatorResponse validatorResponse = new HTMLValidatorResponse();
        try {
            String content = htmlTemplateZip.getTemplateContent();
            HTMLTemplateValidator htmlTemplateValidator = new HTMLTemplateValidator(content);
            Boolean isValid = htmlTemplateValidator.validate();
            validatorResponse.setValid(isValid);

        } catch (Exception e) {
            validatorResponse.setValid(false);
            validatorResponse.setErrMessage(e.getMessage());
        }
        return validatorResponse;
    }


}
