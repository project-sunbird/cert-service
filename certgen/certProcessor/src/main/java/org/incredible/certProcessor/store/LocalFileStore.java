package org.incredible.certProcessor.store;

import org.apache.commons.io.FileUtils;
import org.incredible.certProcessor.JsonKey;

import java.io.File;
import java.util.Map;

public class LocalFileStore  extends CertStore{


    private static Map<String, String> properties;

    public LocalFileStore(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String store(File file) throws Exception {
        FileUtils.copyFileToDirectory(file, new File("public/"));
        return properties.get(JsonKey.DOMAIN_URL) + "/" + JsonKey.ASSETS  + "/" + file.getName();
    }

    @Override
    public void download(String fileName, String localPath) {

    }
}
