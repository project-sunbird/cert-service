package org.sunbird.cert.actor;


public class HTMLValidatorResponse {


    private Boolean valid;

    private String errMessage;


    public HTMLValidatorResponse() {
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    @Override
    public String toString() {
        return "HTMLValidatorResponse{" +
                "valid=" + valid +
                ", errMessage='" + errMessage + '\'' +
                '}';
    }
}
