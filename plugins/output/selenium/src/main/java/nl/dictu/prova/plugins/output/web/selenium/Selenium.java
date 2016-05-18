package nl.dictu.prova.plugins.output.web.selenium;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.OutputPlugin;

/**
 * Driver for controlling Selenium Webdriver
 * 
 * @author Sjoerd Boerhout
 * @since  2016-05-11
 *
 */
public class Selenium implements OutputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner = null;
  private WebDriver  webdriver = null;
  private Integer    maxRetries = 1;
  private long	     maxTimeOut = 1;
  
  
  @Override
  public String getName()
  {
    return "Selenium Webdriver";
  }

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: output plugin Selenium Webdriver!");
    
    if(testRunner == null)
       throw new Exception("No testRunner supplied!");
    
    this.testRunner = testRunner; 
    maxTimeOut = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
    maxRetries = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_MAX_RETRIES));
  }


  @Override
  public void shutDown()
  {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void setUp(TestCase testCase) throws Exception
  {
    try
    {
      String browserType = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_TYPE);
      
      LOGGER.debug("Setup: Test Case ID '{}' with browser '{}'", () -> testCase.getId(), () -> browserType);
      
      if(browserType.equalsIgnoreCase("FireFox"))
      {
        if(testRunner.hasPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PROFILE))
        {
          ProfilesIni profile = new ProfilesIni();
          FirefoxProfile ffProfile = profile.getProfile(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PROFILE));
          LOGGER.trace("Try to load webdriver 'FireFox' with profile '{}'", testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PROFILE));
          webdriver = new FirefoxDriver(ffProfile);
        }
        else
        {
          LOGGER.trace("Try to load webdriver 'FireFox'");
          webdriver = new FirefoxDriver();
        }
      }
      else if(browserType.equalsIgnoreCase("Chrome"))
      {
        LOGGER.trace("Try to load webdriver 'Chrome'");
        webdriver = new ChromeDriver();
      }
      else if(browserType.equalsIgnoreCase("InternetExplorer") || browserType.equalsIgnoreCase("IE"))
      {
        LOGGER.trace("Try to load webdriver 'InternetExplorer'");
        webdriver = new InternetExplorerDriver();
      }
      else if(browserType.equalsIgnoreCase("Safari"))
      {
        LOGGER.trace("Try to load webdriver 'Safari'");
        webdriver = new SafariDriver();
      }
      //else if(browserType.equalsIgnoreCase("PhantomJS"))
      //{
      //  LOGGER.trace("Try to load webdriver 'PhantomJSsele'");
      //  webdriver = new PhantomJSDriver();
      //}
      else
      {
        throw new Exception("Unsupported browser '" + browserType + "' requested.");
      }
      
      
      
      // Compose the setting name of the project url.
      String url =  Config.PROVA_ENV_PFX + "." +
                    testRunner.getPropertyValue(Config.PROVA_ENV).toLowerCase() + "." +
                    testCase.getProjectName().toLowerCase();
  
      LOGGER.trace("URL property name '{}', value: '{}'", url, testRunner.getPropertyValue(url));
      
      // Get the setting with the url
      url = testRunner.getPropertyValue(url);
      
      // TODO fix timeout
      //webdriver.manage().timeouts().implicitlyWait(maxTimeOut, TimeUnit.MILLISECONDS);
      webdriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      webdriver.get(url);
      
      LOGGER.debug("Selenium is ready to start the test!");
    }
    catch(Exception eX)
    {
      LOGGER.trace(eX);
      webdriver.close();
      throw eX;
    }
  }


  @Override
  public void tearDown(TestCase testCase) throws Exception
  {
    LOGGER.debug("TearDown Test Case ID '{}'. Status: '{}'", () -> testCase.getId(), () -> testCase.getStatus().name());
     
    if(webdriver != null)
      webdriver.close();
  }


  @Override
  public void doCaptureScreen(String fileName) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doCaptureScreen is not supported yet.");
  }


  @Override
  public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception
  {
    LOGGER.trace("Click with {} on '{}', Wait for page = {}", (rightClick ? "right" : "left"), xPath, waitUntilPageLoaded);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = webdriver.findElement(By.xpath(xPath));
        
        if(element == null)
          throw new Exception("Element '" + xPath + "' not found.");
      
        // TODO support right click
        if(rightClick) throw new Exception("Right click is not supported yet.");
        
        element.click();
        
        // TODO Add support for waitUntilPageLoaded
        
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
          throw eX;
      }
    }
  }


  @Override
  public void doDownloadFile(String url, String saveAs) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doDownloadFile is not supported yet.");
    
  }


  @Override
  public void doSelect(String xPath, Boolean select) throws Exception
  {
    LOGGER.trace("{}select '{}'", (select ? "" : "de-"), xPath);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = webdriver.findElement(By.xpath(xPath));
        
        if(element == null)
          throw new Exception("Element '" + xPath + "' not found.");
      
        // Check if current and desired state are not equal
        if(select != element.isSelected())
          element.click();
        
        // Action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
          throw eX;
      }
    }
  }


  @Override
  public void doSendKeys(String keys) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doSendKeys is not supported yet.");
    
  }


  @Override
  public void doSetText(String xPath, String text) throws Exception
  {
    LOGGER.trace("Set '{}' with text '{}'", xPath, text);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = webdriver.findElement(By.xpath(xPath));
        
        if(element == null)
          throw new Exception("Element '" + xPath + "' not found.");
      
        // Select the element.
        element.click();
        
        // To prevent typing in existing text first select all and then replace
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"),text);
        
        
        // Action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
          throw eX;
      }
    }    
  }


  @Override
  public void doSleep(double waitTime) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doSleep is not supported yet.");
    
  }


  @Override
  public void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doValidateElement is not supported yet.");
    
  }


  @Override
  public void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception
  {
    // TODO Auto-generated method stub
    throw new Exception("doValidateText is not supported yet.");
    
  }
}
