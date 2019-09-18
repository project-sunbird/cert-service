package org.incredible.certProcessor.views;

import org.apache.commons.io.IOUtils;
import org.incredible.certProcessor.store.CertStore;
import org.incredible.certProcessor.store.StorageParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads zip file and unzips from given cloud(container) based relative url or from the public http url
 */
public class HTMLTemplateZip extends HTMLTemplateProvider {


    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateZip.class);

    /**
     * html zip file url (relative path (uri) of container based url or pubic http url)
     */
    private String zipUrl;

    private Map<String, String> properties;


    public HTMLTemplateZip(String zipUrl, Map<String, String> properties) {
        this.properties = properties;
        this.zipUrl = zipUrl;

    }

    private static final int bufferSize = 4096;

    /**
     * This  method is to download a zip file from the URL in the specified target directory.
     *
     * @param targetDirectory
     * @throws IOException
     */
    private void downloadZipFile(File targetDirectory) throws Exception {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String zipFileName = getZipFileName();
        /**
         * path to download zip file
         */
        String zipFilePath = targetDirectory.getAbsolutePath().concat("/");
        if (zipUrl.startsWith("http")) {
            logger.info("getZipFileFromURl:" + zipUrl + " is public url");
            HttpURLConnection connection = (HttpURLConnection) new URL(zipUrl).openConnection();
            connection.setRequestMethod(HttpMethod.GET);
            InputStream in = connection.getInputStream();
            FileOutputStream out = new FileOutputStream("conf/" + zipFileName);
            copy(in, out, 1024);
            out.close();
            in.close();
        } else if (CertStore.checkStorageParamsExist(CertStore.getCloudProperties())) {
            logger.info("getZipFileFromURl: " + zipUrl + " is container based  uri");
            StorageParams storageParams = new StorageParams(properties);
            storageParams.init();
            storageParams.download(zipUrl, "conf/");
        }
        logger.info("Downloading Zip file " + zipFileName + " from given url : success");
        unzip("conf/" + zipFileName, zipFilePath);
        readIndexHtmlFile(targetDirectory.getAbsolutePath());
    }


    /**
     * unzips zip file
     *
     * @param zipFile
     * @param destDir
     */
    private void unzip(String zipFile, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        try {
            fis = new FileInputStream(zipFile);
            ZipInputStream zipIn = new ZipInputStream(fis);
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File subDir = new File(filePath);
                    subDir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
            fis.close();
            logger.info("Unzipping zip file is finished");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * extracts each files in zip file (zip entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }


    /**
     * used to get file name from the url
     *
     * @return zip file name
     */
    public String getZipFileName() {
        String fileName = null;
        try {
            URI uri = new URI(zipUrl);
            String path = uri.getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.debug("Exception while getting key id from the sign-creator url : {}", e.getMessage());
        }
        if (!fileName.endsWith(".zip"))
            return fileName.concat(".zip");
        return fileName;
    }

    private void readIndexHtmlFile(String absolutePath) throws IOException {
        String htmlFileName = "/index.html";
        if (!isFileExists(new File(absolutePath + htmlFileName))) {
            unzip("conf/" + getZipFileName(), absolutePath);
        }
        FileInputStream fis = new FileInputStream(absolutePath + htmlFileName);
        content = IOUtils.toString(fis, "UTF-8");
        fis.close();

    }

    /**
     * This method is used to get Html file content in string format
     *
     * @return html string
     */
    @Override
    public String getTemplateContent(String filePath) throws Exception {
        if (content == null) {
            File targetDirectory = new File(filePath);
            if (isFileExists(new File("conf/" + getZipFileName()))) {
                readIndexHtmlFile(targetDirectory.getAbsolutePath());
            } else {
                downloadZipFile(targetDirectory);
            }
        }
        return content;
    }

}