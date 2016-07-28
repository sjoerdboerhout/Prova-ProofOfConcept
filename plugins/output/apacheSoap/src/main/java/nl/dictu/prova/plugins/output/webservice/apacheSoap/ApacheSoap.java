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

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.dictu.prova.framework.TestStatus;
import static nl.dictu.prova.plugins.output.webservice.apacheSoap.ApacheSoap.LOGGER;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

public class ApacheSoap implements SoapOutputPlugin {

    final static Logger LOGGER = LogManager.getLogger();

    private TestRunner testRunner = null;
    private Integer maxRetries = 1;
    private long maxTimeOut = 1000; // milliseconds
    
    private HttpClient httpClient = null;
    private HttpResponse httpResponse = null;
    private StringEntity requestMessageEntity = null;
    private HttpPost post = null;
    
    private String currentAuthorization = null;
    private String currentMessage = null;
    private URI currentUrl = null;
    private String currentPrefix = null;
    private Properties currentTests = null;
    private TestCase testCase = null;

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
        post = new HttpPost();
        this.testCase = testCase;
    }

    @Override
    public void tearDown(TestCase testCase) throws Exception {
        LOGGER.debug("TearDown Test Case ID '{}'. Status: '{}'", () -> testCase.getId(), () -> testCase.getStatus().name());

        try {
            cleanUp();
            //httpResponse.close();
        } catch (Exception e) {
            LOGGER.trace("Exception while closing httpClient : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Properties doProcessResponse() throws Exception {        
        //Set headers
        post.addHeader("SOAPAction", "\"\"");
        post.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.0");
        post.addHeader("Host", this.currentUrl.toString());
        post.addHeader("Authorization", "Basic " + currentAuthorization);
        
        //Set soap message
        post.setEntity(requestMessageEntity);
        
        //Set target
        post.setURI(currentUrl);
        
        //Send it
        httpResponse = httpClient.execute(post);
        LOGGER.debug("Message with prefix " + currentPrefix + " has been sent.");
        
        //Convert response to string
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        
        //Log the result
        LOGGER.debug("Header response : " + httpResponse.getStatusLine().getStatusCode());
        LOGGER.debug("Response body   : \n" + result);
        
        //Split the response string and run tests on the result variables
        Properties splitResultmessage = splitSoapMessage(result.toString());
        //runTests(splitResultmessage, currentTests);
        
        return splitResultmessage;
    }

    @Override
    public void doSetProperties(String url, String user, String pass, String prefix) throws Exception {
        //Url
        try {
            this.currentUrl = new URI(url);
        } catch (Exception e) {
            LOGGER.trace("Exception! Please check provided url");
        }
        
        //Authorization
        if(user != null & pass != null){
            LOGGER.trace("Encoding the authorization with user '" + user + "' and pass '" + pass + "'.");
            String authorization = user + ":" + pass;
            byte[] encodedAuthorizationBytes = Base64.encode(authorization.getBytes());
            this.currentAuthorization =  new String(encodedAuthorizationBytes);
        } else {
            LOGGER.info("User and/or password supplied to Soap plugin were null, sending message without authorization.");
        }
        
        //Prefix
        if(prefix != null){
            this.currentPrefix = prefix;
        } else {
            throw new Exception("Supplied prefix is null!");
        }
    }

    @Override
    public void doSetMessage(String message) throws Exception {
        //http://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/examples/PostSOAP.java
        
        //Message
        if(message != null){
            requestMessageEntity = new StringEntity(message);
            requestMessageEntity.setContentType("text/xml;charset=UTF-8");
            currentMessage = message;
        }
    }
    
    @Override
    public void doSetTests(Properties testCases) throws Exception {
        this.currentTests = testCases;
    }
    
    public void cleanUp() {
        LOGGER.debug("Cleaning Soap variables.");
        currentAuthorization = null;
        currentMessage = null;
        currentUrl = null;
        currentPrefix = null;
        currentTests = null;
    }
    
    public String doGetCurrentPrefix() {
        return this.currentPrefix;
    }
    
    public Properties splitSoapMessage(String message){
        Properties temp = new Properties();
        
        //Pattern openingParentPattern = Pattern.compile("\\<[A-Za-z0-9:]+\\>");
        //Pattern closingParentPattern = Pattern.compile("\\<\\/[A-Za-z0-9:]+\\>");
        Pattern standalonePattern = Pattern.compile("\\<[A-Za-z0-9:]+\\/\\>");
        Pattern variablePattern = Pattern.compile("\\<[A-Za-z0-9:]+\\>[A-Za-z0-9]\\<\\/[A-Za-z0-9:]+\\>");
        
        Matcher variableMatcher = variablePattern.matcher(message);
        
        while(variableMatcher.find()){
            System.out.println(variableMatcher.group(0));
            variableMatcher.replaceFirst("");
        }
        
        Matcher standaloneMatcher = standalonePattern.matcher(message);
        //Matcher openingParentMatcher = openingParentPattern.matcher(message);
        //Matcher closingParentMatcher = closingParentPattern.matcher(message);
        
        
        
        
        temp.put("prop1", "val1");
        temp.put("prop2", "val2");
        temp.put("prop3", "val3");
        temp.put("prop4", "val4");
        temp.put("authorization", currentAuthorization);
        
        return temp;
    }
    
    public void runTests(Properties splitResponse, Properties testCases){
        Properties nullChecks = new Properties();
        
        //Check if element has expected value
        for(Entry entry : testCases.entrySet()){
            if(entry.getValue().equals("{null}")){
                nullChecks.put(entry.getKey(), "");
                testCases.remove(entry.getKey());
                break;
            }
            if(splitResponse.containsKey(entry.getKey())){
                String value = (String) entry.getValue();
                if(splitResponse.getProperty((String) entry.getKey()).trim().toLowerCase().equals(value.trim().toLowerCase())){
                    LOGGER.info("Test for element " + entry.getKey() + " succeeded, element has expected value.");
                    if(testCase.getStatus() != TestStatus.FAILED)
                        testCase.setStatus(TestStatus.PASSED);
                }
            } else {
                LOGGER.info("Test for element " + entry.getKey() + " failed, could not find specified element in response.");
                testCase.setStatus(TestStatus.FAILED);
            }
        }
        
        //Check if element is null
        if(!nullChecks.entrySet().isEmpty()){
            for(Entry entry : nullChecks.entrySet()){
                if(splitResponse.containsKey(entry.getKey())){
                    if(splitResponse.getProperty((String) entry.getKey()) == null){
                        LOGGER.info("Test for element " + entry.getKey() + " succeeded, element is null.");
                        if(testCase.getStatus() != TestStatus.FAILED)
                            testCase.setStatus(TestStatus.PASSED);
                    }
                    //Indien niet voorkomt ook goed.
                } else {
                    LOGGER.info("Test for element " + entry.getKey() + " failed, could not find specified element in response.");
                    testCase.setStatus(TestStatus.FAILED);
                }
            }
        }        
    }
}
