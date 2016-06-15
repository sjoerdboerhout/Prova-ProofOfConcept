package nl.dictu.prova.plugins.output.web.selenium;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
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
import org.openqa.selenium.support.ui.Select;
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
  private long	     maxTimeOut = 1000; // milliseconds
  
  
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
    if(maxTimeOut < 1000) maxTimeOut = 1000;
    
    maxRetries = Integer.valueOf(testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_MAX_RETRIES));
    if(maxRetries < 0) maxRetries = 0;
    
    LOGGER.debug("Webdriver initialized with timeout: {} ms, max retries: {}", maxTimeOut, maxRetries);
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
        String chromePath = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PATH_CHROME);
        
        LOGGER.trace("Try to load webdriver 'Chrome' ({})", chromePath);
        System.setProperty("webdriver.chrome.driver", chromePath);  

        webdriver = new ChromeDriver();
      }
      else if(browserType.equalsIgnoreCase("InternetExplorer") || browserType.equalsIgnoreCase("IE"))
      {
        String iePath = testRunner.getPropertyValue(Config.PROVA_DIR) +
                        File.separator +
                        testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PATH_IE);
        
        LOGGER.trace("Try to locate webdriver 'IE': ({})", iePath);
        if(!new File(iePath).isFile())
        {
          iePath = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PATH_IE);
          LOGGER.trace("Try to locate webdriver 'IE': ({})", iePath);
          
          if(!new File(iePath).isFile())
            throw new Exception("No valid path to IE driver. (" + iePath + ")");
        }
        
        LOGGER.trace("Try to load webdriver 'InternetExplorer' ({})", iePath);
        System.setProperty("webdriver.ie.driver", iePath);        
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
      
      // Set implicitly wait time when searching an element
      // Not preferred because an exception is thrown after this timeout which will
      // slow down test execution.
      // webdriver.manage().timeouts().implicitlyWait(maxTimeOut, TimeUnit.MILLISECONDS);
      
      
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
      // TODO Enable again after finishing testing
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
    File scrFile = ((TakesScreenshot)webdriver).getScreenshotAs(OutputType.FILE);
    
    try 
    {
      if(!new File(fileName).isFile())
        fileName = testRunner.getPropertyValue(Config.PROVA_TESTS_ROOT);
      
      FileUtils.copyFile(scrFile, new File(fileName + "x.png"));
      LOGGER.debug("Placed screen shot in " + fileName + "x.png");
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }


  @Override
  public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception
  {
    LOGGER.debug(">> Click with {} on '{}', Wait for page = {}", (rightClick ? "right" : "left"), xPath, waitUntilPageLoaded);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = findElement(xPath);
        
        if(element == null)
          throw new Exception("Element '" + xPath + "' not found.");
      
        // TODO support right click
        if(rightClick) throw new Exception("Right click is not supported yet.");
        
        LOGGER.trace("Clicking on element '{}' (doClick)", xPath);
        assert element.isDisplayed();
        assert element.isEnabled();
        
        //if(waitUntilPageLoaded)
        //  element.submit();
        //else
          element.click();
        //element.sendKeys(Keys.RETURN);
        
        // TODO Add support for waitUntilPageLoaded
        if(waitUntilPageLoaded)
        {
          //new WebDriverWait(webdriver, 30).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
          //new WebDriverWait(webdriver, 30).until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(element)));
        }
        
        return;
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
          this.doCaptureScreen("doClick");
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
    LOGGER.debug(">> {}Select '{}'", (select ? "" : "De-"), xPath);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = findElement(xPath);
        
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
  public void doSelectDropdown(String xPath, String select) throws Exception
  {
    LOGGER.debug("> Select '{}' on {}",select , xPath);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        Select dropdown = new Select(findElement(xPath));
        
        LOGGER.trace("Element '" + xPath + "' found.");
      
        // Set dropdown by visible text
        dropdown.selectByVisibleText(select);
        
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
      LOGGER.debug(">> Send key '{}' to active element", keys);
      
      keys = keys.replace("<DOWN>", Keys.DOWN);
      keys = keys.replace("<END>", Keys.END);
      keys = keys.replace("<ESC>", Keys.ESCAPE);
      keys = keys.replace("<HOME>", Keys.HOME);
      keys = keys.replace("<INSERT>", Keys.INSERT);
      keys = keys.replace("<LEFT>", Keys.LEFT);
      keys = keys.replace("<RETURN>", Keys.RETURN);
      keys = keys.replace("<RIGHT>", Keys.RIGHT);
      keys = keys.replace("<TAB>", Keys.TAB);
      keys = keys.replace("<UP>", Keys.UP);
      
      LOGGER.trace("> Send keys '{}' to active element", keys);
      
      webdriver.switchTo().activeElement().sendKeys(keys);
      
      doCaptureScreen("");
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
    LOGGER.debug(">> Set '{}' with text '{}'", xPath, text);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        WebElement element = findElement(xPath);
        
        if(element == null || !element.isEnabled())
        {
          throw new Exception("Element '" + xPath + "' not found.");
        }
        
        // Select the element.
        //LOGGER.trace("Clicking on element '{}' (doSetText)", xPath);
        //element.click();
        
        // To prevent typing in existing text first select all and then replace
        LOGGER.trace("Sending keys to element '{}' (doSetText)", xPath);
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
  public void doSleep(long waitTime) throws Exception
  {
    LOGGER.debug(">> Sleep '{}' ms", waitTime);
    
    try
    {
      Thread.sleep(waitTime);
    }
    catch(Exception eX)
    {
      LOGGER.debug("Exception while waiting '{}' ms: {}", 
                    waitTime, eX.getMessage());
          
          throw eX;
    }    
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
    LOGGER.debug("> Validate '{}' with text '{}'", xPath, value);
    int iTimeOut = 0;
    try
    {
    	LOGGER.trace("Converting {} from milliseconds to seconds", timeOut);
    	iTimeOut = Integer.valueOf((int) (timeOut/1000));
    	if(iTimeOut < 1) iTimeOut = 1;
    	LOGGER.trace("Convertion to seconds complete, timeout is {} seconds", iTimeOut);
    }
    catch(Exception eX)
    {
    	LOGGER.debug("Converting to seconds failed: " + eX);
    	throw eX;
    }
    WebDriverWait wait = new WebDriverWait(webdriver, iTimeOut);
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
        // Get text from element
        String text = element.getText();
        // If exists is false, check if text is not present in element
        if (!exists)
        {
        	try
        	{
	        	LOGGER.trace("Controleren of de tekst {} niet voorkomt op de pagina", value);
	        	Assert.assertTrue("The value " + value + " is found in the text: " + text,
	        			            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(element, value))));
        	}
        	catch(TimeoutException eX)
        	{
        		throw new Exception("The value " + value + " is found in the text: " + text);
        	}
        }
        // Check if element contains the given text
        else
        {
        	LOGGER.trace("Controleren of de tekst {} voorkomt op de pagina", value);
        	Assert.assertTrue("The value " + value + " is not found in the text: " + text,
        			           wait.until(ExpectedConditions.textToBePresentInElement(element, value)));
        }
        
        // action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
        {
          LOGGER.debug("Exception while validating text '{}' in '{}': {} (retry count: {})", 
                        value, xPath, eX.getMessage(), count);
          
          throw eX;
        }
      }
    }   
  }
  
  @Override
  public void doSwitchFrame(String xPath, Boolean alert, Boolean accept) throws Exception
  {
    LOGGER.debug(">> Switch to frame '{}' ", xPath);
    
    int count = 0;
    
    while(true)
    {
      try
      {
        if (alert == true)
        {
        	LOGGER.trace("Switching to alert (doSwitchFrame)");
        	Alert popupalert = webdriver.switchTo().alert();
        	if (accept == true)
        	{
        		LOGGER.trace("Accepting alert (doSwitchFrame)");
        		popupalert.accept();
        	}
        	else if (accept == false)
        	{
        		LOGGER.trace("Dismissing alert (doSwitchFrame)");
        		popupalert.dismiss();
        	}
        	else
        	{
        		throw new Exception();
        	}

        }
        else
        {
	    	if (xPath=="DEFAULT")
	        {
	        	//switching to the default frame
	        	LOGGER.trace("Switching to frame '{}' (doSwitchFrame)", xPath);
		        webdriver.switchTo().defaultContent();
	        }
	        else
	        {
		        WebElement element = findElement(xPath);
		        
		        if(element == null || !element.isEnabled())
		        {
		          throw new Exception("Element '" + xPath + "' not found.");
		        }
		        
		        // switching to frame selected by xpath
		        LOGGER.trace("Switching to frame '{}' (doSwitchFrame)", xPath);
		        webdriver.switchTo().frame(element);
	        } 
        }
        // Action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
        {
          LOGGER.debug("Exception while switching to frame '{}' : {} (retry count: {})", 
                        xPath, eX.getMessage(), count);
          
          throw eX;
        }
      }
    }    
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
                
        // Wait until element visible
        Integer timeOut = (int) (maxTimeOut / 1000);
        LOGGER.trace("Wait time for element: '{}'", timeOut);
        WebDriverWait wait = new WebDriverWait(webdriver,timeOut);
        //wait.until(ExpectedConditions.elementToBeClickable(element));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        LOGGER.trace("Found element is clickable. Let's try!");

        element = webdriver.findElement(By.xpath(xPath));
        
        LOGGER.trace("Found element: '{}'", (element != null ? "YES" : "NO"));
        
        return element;
      }
      catch(StaleElementReferenceException eX)
      {
        if(++count > maxRetries){ throw eX;}
        LOGGER.trace("Element '{}' is no longer attached to the DOM. Try again. ({})", xPath, count);
      }
      catch(NoSuchElementException eX)
      {
        if(++count > maxRetries){ throw eX;}
        LOGGER.trace("Element '{}' not found. Try again. ({})", xPath, count);
      }
      catch(TimeoutException eX)
      {
        if(++count > maxRetries){ throw eX;}
        LOGGER.trace("TimeOut Exception on '{}'. Try again. ({})", xPath, count);
      }
      catch(Exception eX)
      {       
        if(++count > maxRetries)
        {
          LOGGER.error("Exception while searching element '{}' retry count: '{}', Type: '{}' : '{}'", 
            xPath,  
            count, 
            eX.getClass().getSimpleName(),
            eX.getMessage());
          
          throw eX;
        }
        else
        {
          LOGGER.trace("Exception while searching element '{}' retry count: '{}', Type: '{}' : '{}'", 
            xPath,  
            count, 
            eX.getClass().getName(),
            eX.getMessage());
        }
      }
    }
  }
   
}
