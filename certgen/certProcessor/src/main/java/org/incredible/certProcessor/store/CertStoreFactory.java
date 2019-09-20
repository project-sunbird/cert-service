package org.incredible.certProcessor.store;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertStoreFactory {

    private static ObjectMapper mapper = new ObjectMapper();

    private static Logger logger = Logger.getLogger(CertStoreFactory.class);

    private Map<String, String> properties;


    public CertStoreFactory(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * templateUrl could be local and/or relative or http URL
     * RELATIVE: If storageParams exist and url doesn't start with http, it is relative to container.
     * ABSOLUTE: a)If storageParams exist and url starts with http,then it is httpURL (private, container only)
     * b)if storageParams does not exits, then it is httpURL (public).
     * LOCAL- If storageParams doesn't exist, and  url is always relative and then template is in local
     *
     * @param templateUrl
     * @param storeConfig storage params
     * @return
     */
    public ICertStore getHtmlTemplateStore(String templateUrl, StoreConfig storeConfig) {
        ICertStore certStore = null;
        if (templateUrl.startsWith("http")) {
            if (StringUtils.isNotBlank(properties.get(JsonKey.containerName)) && (templateUrl.contains(properties.get(JsonKey.containerName)) && checkStorageParamsExist(storeConfig))) {
                certStore = getCloudStore(storeConfig);
            } else {
                certStore = new LocalStore();
            }
        } else if (checkStorageParamsExist(storeConfig)) {
            certStore = getCloudStore(storeConfig);
        }
        return certStore;
    }

    /**
     * used to know whether certificate files should be stored in local or cloud
     * Scenario 1)If storage params exits then it is cloud storage
     * Scenario 2)If preview is true (even If storage params exists it not cloud store),then it always local store
     * Scenario 3)If storage params doest not exits , then it is local store
     *
     * @param storeConfig
     * @param preview
     * @return
     */
    public ICertStore getCertStore(StoreConfig storeConfig, String preview) {
        if (BooleanUtils.toBoolean(preview)) {
            return new LocalStore();
        }
        if (checkStorageParamsExist(storeConfig)) {
            return getCloudStore(storeConfig);
        } else {
            return new LocalStore();
        }
    }

    /**
     * used to clean up files
     *
     * @param fileName
     * @param path
     */
    public void cleanUp(String fileName, String path) {
        Boolean isDeleted = false;
        try {
            if (StringUtils.isNotBlank(fileName)) {
                File directory = new File(path);
                File[] files = directory.listFiles((FileFilter) new PrefixFileFilter(fileName));
                for (File file : files) {
                    isDeleted = file.delete();
                }
                logger.info("CertificateGeneratorActor: cleanUp completed: " + isDeleted);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * returns directory name to store all the certificate related files
     *
     * @param zipFileName
     * @param properties
     * @return
     */
    public String getDirectoryName(String zipFileName, Map<String, String> properties) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("conf/");
        if (StringUtils.isNotEmpty(properties.get(JsonKey.ROOT_ORG_ID))) {
            stringBuilder.append(properties.get(JsonKey.ROOT_ORG_ID) + "_");
        }
        if (StringUtils.isNotEmpty(properties.get(JsonKey.TAG))) {
            stringBuilder.append(properties.get(JsonKey.TAG) + "_");
        }
        String dirName = stringBuilder.toString().concat(zipFileName.concat("/"));
        return dirName;
    }

    /**
     * Checks all storage params(container name , account, key) exists or not
     *
     * @param storageParams
     * @return boolean value
     */
    public Boolean checkStorageParamsExist(StoreConfig storageParams) {
        Map<String, String> properties = new HashMap<>();
        List<String> keys = Arrays.asList(JsonKey.containerName, JsonKey.ACCOUNT, JsonKey.KEY);
        if (storageParams.getType().equals(JsonKey.AZURE)) {
            properties = mapper.convertValue(storageParams.getAzureStoreConfig(), Map.class);
        }
        if (storageParams.getType().equals(JsonKey.AWS)) {
            properties = mapper.convertValue(storageParams.getAwsStoreConfig(), Map.class);
        }
        if (MapUtils.isEmpty(properties)) {
            return false;
        }
        for (String key : keys) {
            if (StringUtils.isBlank(properties.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * maps store params request to StoreConfig
     * @param storeParams
     * @return
     */
    public StoreConfig setCloudProperties(Map<String, Object> storeParams) {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setType((String) storeParams.get(JsonKey.TYPE));
        if (storeParams.containsKey(JsonKey.AZURE)) {
            AzureStoreConfig azureStoreConfig = mapper.convertValue(storeParams.get(JsonKey.AZURE), AzureStoreConfig.class);
            storeConfig.setAzureStoreConfig(azureStoreConfig);
        } else if (storeParams.containsKey((JsonKey.TYPE))) {
            AwsStoreConfig awsStoreConfig = mapper.convertValue(storeParams.get(JsonKey.AWS), AwsStoreConfig.class);
            storeConfig.setAwsStoreConfig(awsStoreConfig);
        }
        return storeConfig;
    }

    /**
     * to know whether cloud store is azure or aws
     * @param storeConfig
     * @return instance of azureStore or awsStore
     */
    private CloudStore getCloudStore(StoreConfig storeConfig) {
        CloudStore cloudStore = null;
        if (storeConfig.getType().equals(JsonKey.AZURE)) {
            cloudStore = new AzureStore(storeConfig);
        } else if (storeConfig.getType().equals(JsonKey.AWS)) {
            cloudStore = new AwsStore(storeConfig);
        }
        return cloudStore;
    }

}


