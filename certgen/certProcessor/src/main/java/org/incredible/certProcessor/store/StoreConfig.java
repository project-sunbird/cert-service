package org.incredible.certProcessor.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StoreConfig {

    private ObjectMapper mapper = new ObjectMapper();

    private String type;

    private String cloudRetryCount = "3";

    private AzureStoreConfig azureStoreConfig;

    private AwsStoreConfig awsStoreConfig;

    public StoreConfig() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCloudRetryCount() {
        return cloudRetryCount;
    }

    public void setCloudRetryCount(String cloudRetryCount) {
        this.cloudRetryCount = cloudRetryCount;
    }

    public AzureStoreConfig getAzureStoreConfig() {
        return azureStoreConfig;
    }

    public void setAzureStoreConfig(AzureStoreConfig azureStoreConfig) {
        this.azureStoreConfig = azureStoreConfig;
    }

    public AwsStoreConfig getAwsStoreConfig() {
        return awsStoreConfig;
    }

    public void setAwsStoreConfig(AwsStoreConfig awsStoreConfig) {
        this.awsStoreConfig = awsStoreConfig;
    }

    @Override
    public String toString() {
        String stringRep = null;
        try {
            stringRep = mapper.writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
        }
        return stringRep;
    }
}
