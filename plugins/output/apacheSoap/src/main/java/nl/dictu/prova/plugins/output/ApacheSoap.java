/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 * <p>
 * Date: 24-08-2016 Author(s): Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.plugins.output;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.OutputPlugin;

import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.dictu.prova.TestType;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

public class ApacheSoap implements OutputPlugin
{

  final static Logger LOGGER = LogManager.getLogger();

  private TestRunner testRunner = null;
  private Integer maxRetries = 1;
  private long maxTimeOut = 1000; // milliseconds

  private HttpClient httpClient = null;
  private HttpResponse httpResponse = null;
  private StringEntity requestMessageEntity = null;
  private HttpPost post = null;
  private Integer status = null;

  private String currentAuthorization = null;
  private String currentMessage = null;
  private URI currentUrl = null;
  private String currentPrefix = null;
  private Properties currentTests = null;
  private TestCase testCase = null;

  @Override
  public String getName()
  {
    return "Soap messaging implementation";
  }

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: output plugin Soap messaging!");

    if (testRunner == null)
    {
      throw new Exception("No testRunner supplied!");
    }

    this.testRunner = testRunner;

    maxTimeOut = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
    if (maxTimeOut < 1000)
    {
      maxTimeOut = 1000;
    }

    maxRetries = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_MAX_RETRIES));
    if (maxRetries < 0)
    {
      maxRetries = 0;
    }

    LOGGER.debug("Soap messaging initialized with timeout: {} ms, max retries: {}", maxTimeOut, maxRetries);
  }
  
  @Override
  public TestType[] getTestType()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void tearDown(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TestAction getTestAction(String string) throws InvalidParameterException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void shutDown()
  {
    try
    {
      //httpClient.close();
      //httpResponse.close();
    }
    catch (NullPointerException eX)
    {
      // Ignore. HttpClient already closed
    }
    catch (Exception eX)
    {
      LOGGER.error("Exception during shutDown: '{}'", eX.getMessage());
    }
  }

  @Override
  public void setUp(TestCase testCase)
  {
    httpClient = HttpClientBuilder.create().build();
    post = new HttpPost();
    this.testCase = testCase;
  }

  public Properties doProcessResponse() throws Exception
  {
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
    status = httpResponse.getStatusLine().getStatusCode();
    if (status != 200)
    {
      LOGGER.error("\n********************\n*\n*   Error: Message with prefix " + currentPrefix + " has been sent unsuccesfully.\n*\n********************");
      testCase.setStatus(TestStatus.FAILED);
      return new Properties();
    }
    else
    {
      LOGGER.info("\n********************\n*\n*   Message with prefix " + currentPrefix + " has been sent succesfully.\n*\n********************");

      //Convert response to string
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = bufferedReader.readLine()) != null)
      {
        result.append(line);
      }

      //Log the result
      LOGGER.debug("Response body   : \n" + result);

      //Split the response string and run tests on the result variables
      Properties splitResultmessage = splitSoapMessage(result.toString());
      //runTests(splitResultmessage, currentTests);

      return splitResultmessage;
    }
  }

  public void doSetProperties(String url, String user, String pass, String prefix) throws Exception
  {
    //Url
    try
    {
      this.currentUrl = new URI(url);
    }
    catch (Exception e)
    {
      LOGGER.trace("Exception! Please check provided url");
    }

    //Authorization
    if (user != null & pass != null)
    {
      LOGGER.trace("Encoding the authorization with user '" + user + "' and pass '" + pass + "'.");
      String authorization = user + ":" + pass;
      byte[] encodedAuthorizationBytes = Base64.encode(authorization.getBytes());
      this.currentAuthorization = new String(encodedAuthorizationBytes);
    }
    else
    {
      LOGGER.info("User and/or password supplied to Soap plugin were null, sending message without authorization.");
    }

    //Prefix
    if (prefix != null)
    {
      this.currentPrefix = prefix;
    }
    else
    {
      throw new Exception("Supplied prefix is null!");
    }
  }

  public void doSetMessage(String message) throws Exception
  {
    //Message
    if (message != null)
    {
      requestMessageEntity = new StringEntity(message);
      requestMessageEntity.setContentType("text/xml;charset=UTF-8");
      currentMessage = message;
    }
  }

  public void doSetTests(Properties testCases) throws Exception
  {
    this.currentTests = testCases;
  }

  public void cleanUp()
  {
    LOGGER.debug("Cleaning Soap variables.");
    currentAuthorization = null;
    currentMessage = null;
    currentUrl = null;
    currentPrefix = null;
    currentTests = null;
  }

  public String doGetCurrentPrefix()
  {
    return this.currentPrefix;
  }

  public Properties splitSoapMessage(String message)
  {
    Properties result = new Properties();

    //Pattern standalonePattern = Pattern.compile("\\<[A-Za-z0-9:]+\\/\\>");
    Pattern variablePattern = Pattern.compile("\\<[A-Za-z0-9:]+\\>[^<>]+\\<\\/[A-Za-z0-9:]+\\>");
    Pattern variableName = Pattern.compile("\\:(.+?)\\>");
    Pattern variable = Pattern.compile("\\>(.+?)\\<");
    ArrayList<String> matches = new ArrayList<String>();

    Matcher variablePatternMatcher = variablePattern.matcher(message);

    while (variablePatternMatcher.find())
    {
      String match = variablePatternMatcher.group(0);
      matches.add(match);
    }
    for (String string : matches)
    {
      Matcher variableNameMatcher = variableName.matcher(string);
      Matcher variableMatcher = variable.matcher(string);

      String key = null;
      String value = null;

      while (variableNameMatcher.find())
      {
        key = variableNameMatcher.group(1);
      }
      while (variableMatcher.find())
      {
        value = variableMatcher.group(1);
      }
      LOGGER.info("Key : " + key + ", value : " + value);
      if (key != null)
      {
        result.put(key, value);
      }
    }
    //TODO: standalone elementen toevoegen
    result.put("authorization", currentAuthorization);

    return result;
  }

  public void runTests(Properties splitResponse, Properties testCases)
  {
    Properties nullChecks = new Properties();

    //Check if element has expected value
    for (Entry entry : testCases.entrySet())
    {
      if (entry.getValue().equals("{null}"))
      {
        nullChecks.put(entry.getKey(), "");
        testCases.remove(entry.getKey());
        break;
      }
      if (splitResponse.containsKey(entry.getKey()))
      {
        String value = (String) entry.getValue();
        if (splitResponse.getProperty((String) entry.getKey()).trim().toLowerCase().equals(value.trim().toLowerCase()))
        {
          LOGGER.info("Test for element " + entry.getKey() + " succeeded, element has expected value.");
          if (testCase.getStatus() != TestStatus.FAILED)
          {
            testCase.setStatus(TestStatus.PASSED);
          }
        }
      }
      else
      {
        LOGGER.info("Test for element " + entry.getKey() + " failed, could not find specified element in response.");
        testCase.setStatus(TestStatus.FAILED);
      }
    }

    //Check if element is null
    if (!nullChecks.entrySet().isEmpty())
    {
      for (Entry entry : nullChecks.entrySet())
      {
        if (splitResponse.containsKey(entry.getKey()))
        {
          if (splitResponse.getProperty((String) entry.getKey()) == null)
          {
            LOGGER.info("Test for element " + entry.getKey() + " succeeded, element is null.");
            if (testCase.getStatus() != TestStatus.FAILED)
            {
              testCase.setStatus(TestStatus.PASSED);
            }
          }
          //TODO: Indien niet voorkomt moet ook goed zijn.
        }
        else
        {
          LOGGER.info("Test for element " + entry.getKey() + " failed, could not find specified element in response.");
          testCase.setStatus(TestStatus.FAILED);
        }
      }
    }
  }

}
