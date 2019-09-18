package org.incredible.certProcessor.store;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;
import java.util.Map;

public class StorageParams extends CertStore {


    private static BaseStorageService storageService = null;

    private Logger logger = Logger.getLogger(StorageParams.class);

    private static Map<String, String> properties;

    public StorageParams(Map<String, String> properties) {
        this.properties = properties;
    }

    private Store store = new Store();

    private String containerName;

    private String path;

    @Override
    public String store(File file) {
        String orgId = properties.get(JsonKey.ROOT_ORG_ID);
        String batchId = properties.get(JsonKey.TAG);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.setLength(0);
        if (StringUtils.isNotEmpty(orgId)) {
            stringBuilder.append(orgId + "/");
        }
        if (StringUtils.isNotEmpty(batchId)) {
            stringBuilder.append(batchId + "/");
        }
        if (StringUtils.isNotEmpty(path))
            stringBuilder.append(path);
        logger.info("Path of " + stringBuilder.toString());
        CloudStorage cloudStorage = new CloudStorage(storageService);
        int retryCount = Integer.parseInt(store.getCloudRetryCount());
        logger.info("StorageParams:upload:container name got:" + containerName);
        return cloudStorage.uploadFile(containerName, stringBuilder.toString(), file, false, retryCount);
    }


    @Override
    public void download(String fileName, String localPath) {
        logger.info("StorageParams : download : file name: " + fileName + " to  local path " + localPath);
        CloudStorage cloudStorage = new CloudStorage(storageService);
        String containerName = properties.get(JsonKey.CONTAINER_NAME);
        cloudStorage.downloadFile(containerName, fileName, localPath, false);

    }

    public void init() {
        store = CertStore.getCloudProperties();
        String cloudStoreType = store.getType();
        if (StringUtils.isNotBlank(cloudStoreType)) {
            if (StringUtils.equalsIgnoreCase(cloudStoreType, JsonKey.AZURE)) {
                String storageKey = store.getAzureStore().getAccount();
                String storageSecret = store.getAzureStore().getKey();
                containerName = store.getAzureStore().getContainerName();
                path = store.getAzureStore().getPath();
                StorageConfig storageConfig = new StorageConfig(cloudStoreType, storageKey, storageSecret);
                logger.info("StorageParams:init:all storage params initialized for azure block");
                storageService = StorageServiceFactory.getStorageService(storageConfig);
            } else if (StringUtils.equalsIgnoreCase(cloudStoreType, JsonKey.AWS)) {
                String storageKey = store.getAwsStore().getAccount();
                String storageSecret = store.getAwsStore().getKey();
                containerName = store.getAwsStore().getContainerName();
                path = store.getAwsStore().getPath();
                storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
                logger.info("StorageParams:init:all storage params initialized for aws block");
            } else {
                logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(cloudStoreType));
            }
        }
    }

}

