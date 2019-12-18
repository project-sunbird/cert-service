package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.incredible.CertificateGenerator;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.certProcessor.store.*;
import org.incredible.certProcessor.views.HTMLTemplateZip;
import org.incredible.certProcessor.views.HeadlessChromeHtmlToPdfConverter;
import org.incredible.pojos.CertificateResponse;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.cert.actor.CertificateGeneratorActor;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.message.Localizer;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateGeneratorActor.class, HeadlessChromeHtmlToPdfConverter.class, CloudStorage.class, ICertStore.class, Localizer.class, BaseActor.class, CertificateGenerator.class, CloudStorage.class, AzureStore.class})
@PowerMockIgnore("javax.management.*")
public class CertificateGeneratorActorTest {

    private static ActorSystem system;
    private static final Props props = Props.create(CertificateGeneratorActor.class);
    private static CertificateGenerator certificateGenerator;
    private static ICertStore certStore;
    private static AzureStore azureStore;
    private static CertStoreFactory certStoreFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        system = ActorSystem.create("system");
        PowerMockito.mockStatic(Localizer.class);
        when(Localizer.getInstance()).thenReturn(null);
        certStoreFactory =  Mockito.mock(CertStoreFactory.class);
        Mockito.mock(CloudStorage.class);
        certStore = Mockito.mock(AzureStore.class);
        certificateGenerator = PowerMockito.mock(CertificateGenerator.class);
        azureStore = PowerMockito.mock(AzureStore.class);
        PowerMockito.when(certStoreFactory.getCertStore(Mockito.any(StoreConfig.class), Mockito.anyBoolean())).thenReturn(certStore);
        PowerMockito.whenNew(CertificateGenerator.class).withArguments(Mockito.anyMap(), Mockito.anyString()).thenReturn(certificateGenerator);
        PowerMockito.whenNew(AzureStore.class).withArguments(Mockito.any(StoreConfig.class)).thenReturn(azureStore);
        doNothing().when(azureStore).init();


    }


    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void generateCertificate() throws SignatureException.UnreachableException, SignatureException.CreationException, InvalidDateFormatException, FontFormatException, StorageServiceException, NotFoundException, WriterException, IOException {
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        Request request = createCertRequest();
        when(certificateGenerator.createCertificate(Mockito.any(CertModel.class), Mockito.any(HTMLTemplateZip.class))).thenReturn(getGenerateRes());

        when(certStore.save(Mockito.any(File.class), Mockito.anyString())).thenReturn("string");
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Response.class);
        Assert.assertEquals(res, getResponse());
    }

    private Response getResponse() {
        Response response = new Response();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put(JsonKey.PDF_URL, "");
        list.add(innerMap);
        response.put(JsonKey.RESPONSE, list);
        return response;

    }

    private CertificateResponse getGenerateRes() {
        return new CertificateResponse("anyId", "accessCode", "data", "id");
    }

    private Request createCertRequest() {
        Request reqObj = new Request();
        reqObj.setOperation(JsonKey.GENERATE_CERT);
        Map<String, Object> innerMap = new HashMap<>();
        List<Map<String, Object>> listOfData = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put(JsonKey.RECIPIENT_NAME, "name");
        listOfData.add(data);
        Map<String, Object> issuer = new HashMap<>();
        issuer.put(JsonKey.NAME, "issuer name");
        issuer.put(JsonKey.URL, "issuer url");
        List<Map<String, Object>> signatoryList = new ArrayList<>();
        Map<String, Object> signatory = new HashMap<>();
        signatory.put(JsonKey.NAME, "signatory name");
        signatoryList.add(signatory);
        innerMap.put(JsonKey.SIGNATORY_LIST, signatoryList);
        innerMap.put(JsonKey.DATA, listOfData);
        innerMap.put(JsonKey.KEYS, null);
        innerMap.put(JsonKey.ISSUER, issuer);
        innerMap.put(JsonKey.HTML_TEMPLATE, "https://drive.google.com/a/ilimi.in/uc?authuser=1&id=16WgZrm-1Dh44uFryMTo_0uVjZv65mp4u&export=download");
        reqObj.getRequest().put(JsonKey.CERTIFICATE, innerMap);
        return reqObj;
    }

}