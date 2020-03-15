package org.incredible;

import org.incredible.certProcessor.store.ICertStore;

import java.io.File;
import java.io.IOException;

public class FileUploader {

    public static String uploadCertificate(File file, ICertStore certStore, String cloudPath) throws IOException {
        certStore.init();
        return certStore.saveFile(file, cloudPath);
    }

}
