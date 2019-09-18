package org.incredible.certProcessor.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;

import java.io.File;
import java.util.*;

public abstract class CertStore {

    private static ObjectMapper mapper = new ObjectMapper();

    private static Logger logger = Logger.getLogger(CertStore.class);

    private static Store store;


    abstract public String store(File file) throws Exception;

    abstract public void download(String fileName, String localPath) throws Exception;

    /**
     * to check storage params are exist , if it is not exits save file in public assets folder
     *
     * @param
     * @return
     */
    public static Boolean checkStorageParamsExist(Store storageParams) {
        if (!ObjectUtils.allNotNull(storageParams)) {
            return false;
        }
        Map<String, String> properties = new HashMap<>();
        Boolean isStorageParamsExists = true;
        List<String> keys = Arrays.asList(JsonKey.containerName, JsonKey.ACCOUNT, JsonKey.KEY);
        if (StringUtils.isNotBlank(storageParams.getType())) {
            if (storageParams.getType().equals(JsonKey.AZURE)) {
                properties = mapper.convertValue(storageParams.getAzureStore(), Map.class);
            }
            if (storageParams.getType().equals(JsonKey.AWS)) {
                properties = mapper.convertValue(storageParams.getAwsStore(), Map.class);
            }
        }
        if (MapUtils.isEmpty(properties))
            return false;
        for (String key : keys) {
            if (StringUtils.isBlank(properties.get(key))) {
                return false;
            }
        }
        return isStorageParamsExists;
    }

    public static String getDirectoryName(String zipFileName, Map<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        sb.append("conf/");
        if (StringUtils.isNotEmpty(properties.get(JsonKey.ROOT_ORG_ID))) {
            sb.append(properties.get(JsonKey.ROOT_ORG_ID) + "_");
        }
        if (StringUtils.isNotEmpty(properties.get(JsonKey.TAG))) {
            sb.append(properties.get(JsonKey.TAG) + "_");
        }
        String dirName = sb.toString().concat(zipFileName.concat("/"));
        logger.info("getDirectoryName: " + dirName);
        return dirName;
    }

    /**
     * if store params is given map to Store Object else, get params from env
     *
     * @param storeParams
     */
    public static void setCloudProperties(Map<String, Object> storeParams) {
        store = new Store();
        AzureStore azureStore = new AzureStore();
        AWSStore awsStore = new AWSStore();
        if (MapUtils.isNotEmpty(storeParams)) {
            String storageType = (String) storeParams.get(JsonKey.TYPE);
            store.setType(storageType);
            if (storageType.equals(JsonKey.AZURE)) {
                azureStore = mapper.convertValue(storeParams.get(storageType), AzureStore.class);
                store.setAzureStore(azureStore);
            }
            if (storageType.equals(JsonKey.AWS)) {
                awsStore = mapper.convertValue(storeParams.get(storageType), AWSStore.class);
                store.setAwsStore(awsStore);
            }
        } else {
            String storageType = getPropertyFromEnv(JsonKey.CLOUD_STORAGE_TYPE);
            if (StringUtils.isNotBlank(storageType)) {
                store.setType(storageType);
                if (storageType.equals(JsonKey.AZURE)) {
                    azureStore.setContainerName(getPropertyFromEnv(JsonKey.CONTAINER_NAME));
                    azureStore.setAccount(getPropertyFromEnv(JsonKey.AZURE_STORAGE_KEY));
                    azureStore.setKey(getPropertyFromEnv(JsonKey.AZURE_STORAGE_SECRET));
                    store.setAzureStore(azureStore);
                }
                if (storageType.equals(JsonKey.AWS)) {
                    awsStore.setContainerName(getPropertyFromEnv(JsonKey.CONTAINER_NAME));
                    awsStore.setKey(getPropertyFromEnv(JsonKey.AWS_STORAGE_SECRET));
                    awsStore.setAccount(getPropertyFromEnv(JsonKey.AWS_STORAGE_KEY));
                    store.setAwsStore(awsStore);
                }
            }

        }
    }

    private static String getPropertyFromEnv(String property) {
        return System.getenv(property);
    }

    public static Store getCloudProperties() {
        return store;
    }
}
