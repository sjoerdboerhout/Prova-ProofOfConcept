package nl.dictu.prova.plugins.output.webservice.soap;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.WebserviceOutputPlugin;

import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Soap implements WebserviceOutputPlugin {
	
    final static Logger LOGGER = LogManager.getLogger();
  
    private TestRunner             testRunner         = null;
    private Integer                maxRetries         = 1;
    private long	               maxTimeOut         = 1000; // milliseconds
    private CloseableHttpClient    httpClient         = null;
    private CloseableHttpResponse  httpResponse		  = null;
    private String                 soapRequestBody    = null;
    private URL				       qualifiedUrl		  = null; 

	@Override
	public String getName() {
		return "Soap messaging implementation";
	}

	@Override
	public void init(TestRunner testRunner) throws Exception {
		LOGGER.debug("Init: output plugin Soap messaging!");
		
	    if(testRunner == null)
	        throw new Exception("No testRunner supplied!");
	    
	    this.testRunner = testRunner; 
	    
	    maxTimeOut = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
	    if(maxTimeOut < 1000) maxTimeOut = 1000;
	    
	    maxRetries = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_MAX_RETRIES));
	    if(maxRetries < 0) maxRetries = 0;
	    
	    LOGGER.debug("Soap messaging initialized with timeout: {} ms, max retries: {}", maxTimeOut, maxRetries);
	}

	@Override
	public void shutDown() {
	    try
	    {
	      httpClient.close();
	      httpResponse.close();
	    }
	    catch(NullPointerException eX)
	    {
	      // Ignore. HttpClient already closed
	    }
	    catch(Exception eX)
	    {
	      LOGGER.error("Exception during shutDown: '{}'", eX.getMessage());
	    }
	}

	@Override
	public void setUp(TestCase testCase) throws Exception {
		httpClient 		= HttpClientBuilder.create().build();
	}

	@Override
	public void tearDown(TestCase testCase) throws Exception {
		LOGGER.debug("TearDown Test Case ID '{}'. Status: '{}'", () -> testCase.getId(), () -> testCase.getStatus().name());
		
		try {
			httpClient.close();
			httpResponse.close();
		} catch (Exception e){
			LOGGER.trace("Exception while closing httpClient : " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void doProcessResponse(String responseMessage) throws Exception {
		
	}

	@Override
	public void doSendMessage(String requestMessage) throws Exception {
		HttpPost post = new HttpPost();
		
		StringEntity requestMessageEntity = new StringEntity(requestMessage);
		post.setEntity(requestMessageEntity);
		post.addHeader("Host", this.qualifiedUrl.toString());
		
		httpResponse = httpClient.execute(post);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		
		while((line = bufferedReader.readLine())!= null){
			result.append(line);
		}
		LOGGER.debug("Header response : " + httpResponse.getStatusLine().getStatusCode());
		LOGGER.debug("Response body   : " + result);
	}
	
	@Override
	public void doSetLogin(String user, String password) throws Exception {
		
	}

	@Override
	public void doSetUrl(String url) throws Exception {
		try {
			qualifiedUrl = new URL(url);
		} catch (MalformedURLException e){
			LOGGER.trace("MalformedUrlException! Please check provided url: " + url);
			e.printStackTrace();
		} catch (Exception e){
			LOGGER.trace("MalformedUrlException! Please check provided url");
			e.printStackTrace();
		}
	}
}
