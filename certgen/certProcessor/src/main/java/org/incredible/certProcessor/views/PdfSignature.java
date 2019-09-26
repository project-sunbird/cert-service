package org.incredible.certProcessor.views;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

public class PdfSignature {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    private String keyStoreName = "conf/certificate.pfx";

    private char[] password = "1234".toCharArray();

    public void sign(String src, String dest) throws GeneralSecurityException, IOException, DocumentException {

        FileOutputStream fileOutputStream = null;
        PdfReader pdfReader = null;
        PdfStamper stamper = null;

        try {
            logger.info("Signing pdf started");
            fileOutputStream = new FileOutputStream(dest);
            pdfReader = new PdfReader(src);
            stamper = PdfStamper.createSignature(pdfReader, fileOutputStream, '\0');

            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(keyStoreName), password);
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("Digital signature");
            appearance.setLocation("Banglore");
            appearance.setCertificate(chain[0]);
            appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
            appearance.setLayer4Text(PdfSignatureAppearance.questionMark);

//            appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
            // Creating the signature
            ExternalDigest digest = new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider.getName());
            MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null,
                    0, MakeSignature.CryptoStandard.CMS);
        } finally {
            fileOutputStream.close();
            pdfReader.close();
        }

    }
}
