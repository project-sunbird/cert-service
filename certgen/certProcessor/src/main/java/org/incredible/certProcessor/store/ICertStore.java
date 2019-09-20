package org.incredible.certProcessor.store;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ICertStore {

    String save(File file, Map<String, String> properties) throws IOException;

    void get(String url, String fileName) throws IOException;

    void init();


}
