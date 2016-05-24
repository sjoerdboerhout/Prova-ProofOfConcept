package nl.dictu.prova.plugins.output.web.selenium;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    try
    {
      webdriver.close();
    }
    catch(NullPointerException eX)
    {
      // Ignore. Browser already closed
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception during shutDown: '{}'", eX.getMessage());
    }
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
      webdriver.manage().timeouts().implicitlyWait(maxTimeOut, TimeUnit.MILLISECONDS);
      //webdriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      //webdriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
      
      LOGGER.debug("Open URL: '{}'", url);
      webdriver.get(url);
      
      LOGGER.trace("Selenium is ready to start the test!");
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
    
    try
    {
      webdriver.close();
    }
    catch(NullPointerException eX)
    {
      // Ignore. Browser already closed
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception during tearDown: '{}'", eX.getMessage());
    }      
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
    LOGGER.debug("> Click with {} on '{}', Wait for page = {}", (rightClick ? "right" : "left"), xPath, waitUntilPageLoaded);
    
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
        if(waitUntilPageLoaded)
        {
          //new WebDriverWait(webdriver, 30).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
          //new WebDriverWait(webdriver, 30).until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(element)));
        }
        
        return;
      }
      catch(NoSuchElementException eX)
      {
        LOGGER.debug("Element not found. Wait for it and try again. ({})", xPath);
        new WebDriverWait(webdriver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
      }
      catch(TimeoutException eX)
      {
        LOGGER.debug("TimeOut Exception. Wait for it and try again. ({})", xPath);
        new WebDriverWait(webdriver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
      }
      catch(Exception eX)
      {
        LOGGER.debug("Exception while clicking on element '{}' retry count: '{}', Type: '{}' : '{}'", 
          xPath,  
          count, 
          eX.getClass().getSimpleName(),
          eX.getMessage());
        
        if(++count > maxRetries)
        { 
          throw eX;
        }
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
    LOGGER.debug("> {}Select '{}'", (select ? "" : "De-"), xPath);
    
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
        {
          LOGGER.debug("Exception while selecting '{}': {} (retry count: {})", xPath, eX.getMessage(), count);
          
          throw eX;
        }
      }
    }
  }


  @Override
  public void doSendKeys(String keys) throws Exception
  {
    try
    {
      LOGGER.debug("> Send key '{}' to active element", keys);
      
      keys = keys.replace("<DOWN>", Keys.DOWN);
      keys = keys.replace("<END>", Keys.END);
      keys = keys.replace("<ESC>", Keys.ESCAPE);
      keys = keys.replace("<HOME>", Keys.HOME);
      keys = keys.replace("<INSERT>", Keys.INSERT);
      keys = keys.replace("<LEFT>", Keys.LEFT);
      keys = keys.replace("<RIGHT>", Keys.RIGHT);
      keys = keys.replace("<TAB>", Keys.TAB);
      keys = keys.replace("<UP>", Keys.UP);
      
      LOGGER.trace("> Send keys '{}' to active element", keys);
      
      webdriver.switchTo().activeElement().sendKeys(keys);
    }
    catch(Exception eX)
    {
      LOGGER.debug("Exception while sending keys '{}'", keys);
      throw eX;
    }
  }


  @Override
  public void doSetText(String xPath, String text) throws Exception
  {
    LOGGER.debug("> Set '{}' with text '{}'", xPath, text);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = webdriver.findElement(By.xpath(xPath));
        
        if(element == null || !element.isEnabled())
        {
          throw new Exception("Element '" + xPath + "' not found.");
        }
        
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
        {
          LOGGER.debug("Exception while setting text '{}' in '{}': {} (retry count: {})", 
                        xPath, text, eX.getMessage(), count);
          
          throw eX;
        }
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
  
  
  private WebElement findElement(String xPath)
  {
    WebElement element = null;
    
    int count = 0;
    
    while(true)
    {
      try
      {
        LOGGER.trace("Find element: '{}'", xPath);
        
        element = webdriver.findElement(By.xpath(xPath));
        
        LOGGER.trace("Found element: '{}'", (element != null ? element.toString() : "not found"));
        
        return element;
      }
      catch(NoSuchElementException eX)
      {
        LOGGER.debug("Element not found. Wait for it and try again. ({})", xPath);
        new WebDriverWait(webdriver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
      }
      catch(TimeoutException eX)
      {
        LOGGER.debug("TimeOut Exception. Wait for it and try again. ({})", xPath);
        new WebDriverWait(webdriver, 30).until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
      }
      catch(Exception eX)
      {
        LOGGER.debug("Exception while clicking on element '{}' retry count: '{}', Type: '{}' : '{}'", 
          xPath,  
          count, 
          eX.getClass().getSimpleName(),
          eX.getMessage());
        
        if(++count > maxRetries)
        { 
          throw eX;
        }
      }
    }
  }
   
}
