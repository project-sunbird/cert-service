package org.incredible.certProcessor.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Store {

    private ObjectMapper mapper = new ObjectMapper();

    private String type;

    private String cloudRetryCount = "3";

    private AWSStore awsStore;

    private AzureStore azureStore;

    public Store() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AWSStore getAwsStore() {
        return awsStore;
    }

    public void setAwsStore(AWSStore awsStore) {
        this.awsStore = awsStore;
    }

    public AzureStore getAzureStore() {
        return azureStore;
    }

    public void setAzureStore(AzureStore azureStore) {
        this.azureStore = azureStore;
    }

    public String getCloudRetryCount() {
        return cloudRetryCount;
    }

    public void setCloudRetryCount(String cloudRetryCount) {
        this.cloudRetryCount = cloudRetryCount;
    }

    @Override
    public String toString() {
        String stringRep = null;
        try {
            stringRep = mapper.writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
        return stringRep;
    }
}
