package nl.dictu.prova.plugins.output.webservice.apacheSoap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.SoapOutputPlugin;

import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URL;
import java.util.Map;
import java.util.Properties;
import static nl.dictu.prova.plugins.output.webservice.apacheSoap.apacheSoap.LOGGER;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

public class apacheSoap implements SoapOutputPlugin {

    final static Logger LOGGER = LogManager.getLogger();
    
    public final String PROVA_SOAP_AUTHORIZATION    = "prova.plugins.out.webservice.soap.authorization";
    public final String PROVA_SOAP_MESSAGE          = "prova.plugins.out.webservice.soap.message";
    public final String PROVA_SOAP_URL              = "prova.plugins.out.webservice.soap.url";

    private TestRunner testRunner = null;
    private Integer maxRetries = 1;
    private long maxTimeOut = 1000; // milliseconds
    private HttpClient httpClient = null;
    private HttpResponse httpResponse = null;
    private String soapRequestBody = null;
    private URL qualifiedUrl = null;
    private HttpEntity requestMessageEntity = null;
    private HttpPost post = null;

    @Override
    public String getName() {
        return "Soap messaging implementation";
    }

    @Override
    public void init(TestRunner testRunner) throws Exception {
        LOGGER.debug("Init: output plugin Soap messaging!");

        if (testRunner == null) {
            throw new Exception("No testRunner supplied!");
        }

        this.testRunner = testRunner;

        maxTimeOut = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
        if (maxTimeOut < 1000) {
            maxTimeOut = 1000;
        }

        maxRetries = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_MAX_RETRIES));
        if (maxRetries < 0) {
            maxRetries = 0;
        }

        LOGGER.debug("Soap messaging initialized with timeout: {} ms, max retries: {}", maxTimeOut, maxRetries);
    }

    @Override
    public void shutDown() {
        try {
            //httpClient.close();
            //httpResponse.close();
        } catch (NullPointerException eX) {
            // Ignore. HttpClient already closed
        } catch (Exception eX) {
            LOGGER.error("Exception during shutDown: '{}'", eX.getMessage());
        }
    }

    @Override
    public void setUp(TestCase testCase) throws Exception {
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public void tearDown(TestCase testCase) throws Exception {
        LOGGER.debug("TearDown Test Case ID '{}'. Status: '{}'", () -> testCase.getId(), () -> testCase.getStatus().name());

        try {
            //httpClient.close();
            //httpResponse.close();
        } catch (Exception e) {
            LOGGER.trace("Exception while closing httpClient : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Map<Object, Object> doProcessResponse(String string) throws Exception {
        LOGGER.debug("Starting doSendmessage with argument: " + string);
        return new Properties();
    }

    @Override
    public String doSetLogin(String string, String string1) throws Exception {
        LOGGER.debug("Starting doSendmessage with argument: " + string1);
        String authorization = string + ":" + string1;
        byte[] encodedAuthorizationBytes = Base64.encode(authorization.getBytes());
        String encodedAuthorization = new String(encodedAuthorizationBytes);
        testRunner.setPropertyValue(PROVA_SOAP_AUTHORIZATION, encodedAuthorization);
        return encodedAuthorization;
    }

    @Override
    public String doSendMessage(Map<Object, Object> soapProperties) throws Exception {
        //http://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/examples/PostSOAP.java
        LOGGER.debug("Starting doSendmessage with keys: " + soapProperties.keySet().toString() + " and values " + soapProperties.values().toString());
        post = new HttpPost();
        httpClient = new DefaultHttpClient();
        
        //Message
        if(soapProperties.containsKey("message")){
            post.setEntity(requestMessageEntity);
        }
        
        //Host
        if(soapProperties.containsKey("host")){
            post.setURI(new URI((String) soapProperties.get("host")));
        }
        //Login
        if(soapProperties.containsKey("user") & soapProperties.containsKey("pass")){
            post.addHeader("Authorization", "Basic " + doSetLogin((String) soapProperties.get("user"), (String) soapProperties.get("pass")));
        }
        
        post.addHeader("SOAPAction", "\"\"");
        post.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.0");
        
        //Send message
        httpResponse = httpClient.execute(post);
        
        //Convert response to string and return it
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";

        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        
        LOGGER.debug("Header response : " + httpResponse.getStatusLine().getStatusCode());
        LOGGER.debug("Response body   : " + result);
        String resultString = result.toString();
        return resultString;
    }

    @Override
    public URL doSetUrl(String string) throws Exception {
        LOGGER.debug("Starting doSendmessage with argument: " + string);
        try {
            qualifiedUrl = new URL(string);
        } catch (MalformedURLException e) {
            LOGGER.trace("MalformedUrlException! Please check provided url: " + string);
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.trace("MalformedUrlException! Please check provided url");
            e.printStackTrace();
        }
        return qualifiedUrl;
    }

//    @Override
//    public void doSendMessage(String string) throws Exception {
//        LOGGER.debug("Starting doSendmessage with argument: " + string);
//        HttpPost post = new HttpPost();
//
//       // StringEntity requestMessageEntity = new StringEntity();
//        post.setEntity(requestMessageEntity);
//        post.addHeader("Host", this.qualifiedUrl.toString());
//
//        httpResponse = httpClient.execute(post);
//
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//        StringBuffer result = new StringBuffer();
//        String line = "";
//
//        while ((line = bufferedReader.readLine()) != null) {
//            result.append(line);
//        }
        
        
        
//        LOGGER.debug("Header response : " + httpResponse.getStatusLine().getStatusCode());
//        LOGGER.debug("Response body   : " + result);
//        //String resultString = result.toString();
    //}

//    @Override
//    public void doSetLogin(String user, String password) throws Exception {
//        String authorization = user + ":" + password;
//        //info ("BMS_Autheticatie: auth = "+ auth);
//        byte[] encodedAuthorizationBytes = Base64.encode(authorization.getBytes());
//        String encodedAuthorization = new String(encodedAuthorizationBytes);
//        testRunner.setPropertyValue(PROVA_SOAP_AUTHORIZATION, encodedAuthorization);
//    }
//
//    @Override
//    public void doProcessResponse(String response) throws Exception {
//        Integer messageCounter = Integer.parseInt(testRunner.getPropertyValue(PROVA_SOAP_MESSAGE_COUNTER));
//        messageCounter++;
//        testRunner.setPropertyValue("prova.plugins.out.webservice.soap.response" + messageCounter, response);
//        testRunner.setPropertyValue(PROVA_SOAP_MESSAGE_COUNTER, "" + messageCounter);
//    }
//
//    @Override
//    public void doSetUrl(String url) throws Exception {
//        try {
//            qualifiedUrl = new URL(url);
//        } catch (MalformedURLException e) {
//            LOGGER.trace("MalformedUrlException! Please check provided url: " + url);
//            e.printStackTrace();
//        } catch (Exception e) {
//            LOGGER.trace("MalformedUrlException! Please check provided url");
//            e.printStackTrace();
//        }
//    }
}
