package org.incredible.certProcessor.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;


public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public static void convertor(File htmlSource, String certUuid, String directory) {
        File file = new File(directory, certUuid + ".pdf");
        try {
            //html to pdf convertion using headLess chrome
            HeadlessChromeHtmlToPdfConverter.convert(htmlSource, file);
            //using Itext
//            ItextHtmlToPdfConverter.convert(htmlSource,file);
            logger.info("Pdf file is created for the {} ", certUuid);
        }catch (Exception e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        }
    }

}
