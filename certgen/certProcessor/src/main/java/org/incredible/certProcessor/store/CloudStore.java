package org.incredible.certProcessor.store;

import org.apache.commons.lang3.StringUtils;
import org.incredible.certProcessor.JsonKey;

import java.io.File;
import java.util.Map;

public abstract class CloudStore implements ICertStore {


    @Override
    public String save(File file, Map<String, String> properties) {
        return upload(file, setCloudPath(properties));
    }

    @Override
    public void get(String url, String fileName) {
        download(fileName, "conf/");
    }

    public String setCloudPath(Map<String, String> properties) {
        String orgId = properties.get(JsonKey.ROOT_ORG_ID);
        String batchId = properties.get(JsonKey.TAG);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.setLength(0);
        if (StringUtils.isNotEmpty(orgId)) {
            stringBuilder.append(orgId).append("/");
        }
        if (StringUtils.isNotEmpty(batchId)) {
            stringBuilder.append(batchId).append("/");
        }
        return stringBuilder.toString();
    }

    abstract public String upload(File file, String path);

    abstract public void download(String fileName, String localPath);


}
