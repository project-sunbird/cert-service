package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.HtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public void converter(File htmlSource, String fileName) throws IOException {
        HtmlConverter.convertToPdf(htmlSource, new File(fileName));
        logger.info("Pdf file is created ");
    }

}
