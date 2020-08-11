package org.sunbird.response;

import java.util.Map;

public class CertificateResponseV2 {

    private String id;
    private String accessCode;
    private Map<String, Object> jsonData;
    private String recipientId;
    private String qrCodeUrl;
    private String jsonUrl;

    public CertificateResponseV2(String id, String accessCode, String recipientId, String qrCodeUrl, Map<String, Object> jsonData) {
        this.id = id;
        this.accessCode = accessCode;
        this.recipientId = recipientId;
        this.qrCodeUrl = qrCodeUrl;
        this.jsonData = jsonData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public Map<String, Object> getJsonData() {
        return jsonData;
    }

    public void setJsonData(Map<String, Object> jsonData) {
        this.jsonData = jsonData;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public void setJsonUrl(String jsonUrl) {
        this.jsonUrl = jsonUrl;
    }
}
