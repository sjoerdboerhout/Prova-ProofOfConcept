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
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.plugins.output.web.selenium;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.parameters.Xpath;
import nl.dictu.prova.plugins.output.WebOutputPlugin;

/**
 * Driver for controlling Selenium Webdriver
 * 
 * @author Sjoerd Boerhout
 * @since  2016-05-11
 *
 */
public class Selenium implements WebOutputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner = null;
  private WebDriver  webdriver = null;
  private String     currentWindow = "";
  private Integer    maxRetries = 1;
  private long	     maxTimeOut = 1000; // milliseconds
  private String browserType;
  private TestCase testCase;
  
  
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
    	currentWindow = "";
    	webdriver.close();
    	webdriver = null;
    	
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
    browserType = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_TYPE);
    this.testCase = testCase;
    LOGGER.debug("Setup: Test Case ID '{}' with browser '{}'", () -> testCase.getId(), () -> browserType);
  }
  
  
  public void prepareWebdriver() throws Exception
  {
    try
    {
      if(browserType.equalsIgnoreCase("FireFox"))
      {
        String fireFoxPath = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_PATH_GECKO);
          
        //LOGGER.trace("Try to load webdriver 'Gecko' ({})", fireFoxPath);
        LOGGER.trace("Try to load webdriver 'Gecko'");
        System.setProperty("webdriver.gecko.driver", fireFoxPath);
    	  
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
  
      if(testRunner.hasPropertyValue(url))
      {
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
      else
      {
        LOGGER.error("URL property not available!");
      }
    }
    catch(Exception eX)
    {
      LOGGER.trace(eX);
      
      if( webdriver != null)
    	  webdriver.close();
      
      throw eX;
    }
  }

  public void doCaptureScreen(String fileName) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    File scrFile = ((TakesScreenshot)webdriver).getScreenshotAs(OutputType.FILE);
    
    try 
    {
    	
    	fileName = fileName + ".png";
    	LOGGER.debug("Pad1: " + fileName);
    	if(!new File(fileName).isFile()&&!fileName.contains(":"))
    	{
    		fileName = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ File.separator + "Screenshots" + File.separator + fileName;
    		LOGGER.debug("Pad2: " + fileName);
    		if(!new File(fileName.substring(0, fileName.lastIndexOf(File.separator))).isFile()&&!fileName.contains(":"))
        	{
            		fileName = testRunner.getPropertyValue(Config.PROVA_DIR) + File.separator + fileName;
            		LOGGER.debug("Pad3: " + fileName);
        	}
    	}
    	FileUtils.copyFile(scrFile, new File(fileName));
        LOGGER.debug("Placed screen shot in " + fileName);
        this.testRunner.setPropertyValue("SCREENSHOT_PATH", fileName);
        LOGGER.debug("Property set: Name=SCREENSHOT_PATH ; Value =" + fileName);
    	/*if(!new File(fileName.substring(0, fileName.lastIndexOf(File.separator))).isDirectory()||!fileName.contains(File.separator))
    	{
    		fileName = testRunner.getPropertyValue(Config.PROVA_PLUGINS_REPORTING_DIR)+ File.separator + "Screenshots" + File.separator + fileName;
    		LOGGER.debug("Pad2: " + fileName);
    	}	
    	if(!new File(fileName.substring(0, fileName.lastIndexOf(File.separator))).isDirectory())
    	{
        		fileName = testRunner.getPropertyValue(Config.PROVA_DIR) + File.separator + fileName;
        		LOGGER.debug("Pad3: " + fileName);
    	}
      FileUtils.copyFile(scrFile, new File(fileName + ".png"));
      LOGGER.debug("Placed screen shot in " + fileName + ".png");
      this.testRunner.setPropertyValue("SCREENSHOT_PATH", fileName + ".png");
      LOGGER.trace("Property set: Name=SCREENSHOT_PATH ; Value =" + fileName + ".png");*/
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }


  @Override
  public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
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
        //First navigate to element to make sure the element to be clicked is on te screen
        Actions actions = new Actions(webdriver);
        actions.moveToElement(element).perform();

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
  public void doNavigate(String url) 
  {	  
    if(this.webdriver == null)
    {
      try
      {
        prepareWebdriver();
      }
      catch (Exception ex)
      {
        LOGGER.error(ex);
      }
    }
    
	  URL qualifiedUrl = null;
	  LOGGER.debug("Checking url for malformations");
	  
	  try {
		qualifiedUrl = new URL(url);
                
                LOGGER.debug("Url not malformed, navigating to " + qualifiedUrl.getPath());
	  
                webdriver.navigate().to(qualifiedUrl);
	  } catch (MalformedURLException e) {
	  	// TODO Auto-generated catch block
	  	LOGGER.debug("Provided URL is malformed.");
	  } 
	  
  }


  @Override
  public void doSelect(String xPath, Boolean select) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
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
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    LOGGER.debug("> Select '{}' on {}",select , xPath);
    int count = 0;
    
    while(true)
    {
      try
      {
        Select dropdown = new Select(findElement(xPath));
        
        LOGGER.trace("Element '" + xPath + "' found.");
      
        try
        {
	        LOGGER.trace("Trying to select '{}' ByVisibleText (doSelectDropdown)", select);
        	// Set dropdown by visible text
	        dropdown.selectByVisibleText(select);
        }
        catch(NoSuchElementException ex)
        {
        	LOGGER.trace("'{}' not found ByVisibleText (doSelectDropdown)", select);
        	LOGGER.trace("Trying to select '{}' ByValue (doSelectDropdown)", select);
        	// Set dropdown by value
	        try
	        {
	        	dropdown.selectByValue(select);
	        }
	        catch(NoSuchElementException nseex)
	        {
	        	LOGGER.trace("'{}' not found ByValue (doSelectDropdown)", select);
	        	throw new Exception("'" + select + "' can not be selected as 'text' or as 'value' in element: '" + xPath + "'");
	        }
        }
        
        
        // Action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
        {
          LOGGER.debug("Exception while selecting '{}': {} (retry count: {})", xPath, eX.getMessage(), count);
          this.doCaptureScreen("doSelectDropdown");
          throw eX;
        }
      }
    }
  }
  @Override
  public void doSendKeys(String xPath, String keys) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    try
    {
      LOGGER.debug(">> Send key '{}' to element (doSendKeys)", keys);
      //replace the given keys with keyboard presses
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
      
      //if xPath is not filled, sendKeys to the active element
      if (xPath.equalsIgnoreCase("/html/body"))
      {
	      LOGGER.trace("> Send keys '{}' to active element (doSendKeys)", keys);
	      webdriver.switchTo().activeElement().sendKeys(keys);
      }
      //xPath is filled. Find element and send keys
      else
      {
    	  WebElement element = findElement(xPath);
          
          if(element == null || !element.isEnabled())
          {
            throw new Exception("Element '" + xPath + "' not found.");
          }
          LOGGER.trace("Sending keys to element '{}' (doSendKeys)", xPath);
          element.sendKeys(keys);
      }
      
    }
    catch(Exception eX)
    {
      LOGGER.debug("Exception while sending keys '{}'", keys);
      this.doCaptureScreen("doSendKeys");
      throw eX;
    }
  }


  @Override
  public void doSetText(String xPath, String text, Boolean replace) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
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
        
        LOGGER.trace("Sending keys to element '{}'. Replace={} (doSetText)", xPath, replace);
        
        if(replace)
          element.sendKeys(Keys.chord(Keys.CONTROL, "a"),text);
        else
          element.sendKeys(text);
        
        // Action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
        {
          LOGGER.debug("Exception while setting text '{}' in '{}': {} (retry count: {})", 
                        xPath, text, eX.getMessage(), count);
          this.doCaptureScreen("doSetText");
          throw eX;
        }
      }
    }    
  }


  @Override
  public void doSleep(long waitTime) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
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
  public void doSwitchScreen(String name) throws Exception 
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    boolean useName = false;
    boolean switched = false;
    boolean failOnError = Boolean.parseBoolean(testRunner.getPropertyValue(
                          Config.PROVA_PLUGINS_OUT_WEB_BROWSER_FAILSWTCHSCR));
    
    //If doSwitchScreen is called for the first time, the initial window handle is saved
    if (currentWindow.equals(""))
    	{
    		LOGGER.debug("switchScreen is called for the first time, so saving the window handle.");
    		currentWindow = webdriver.getWindowHandle();
    	}
    //If title is passed to the function, switch to the window with that given title
    if(name != null)
    {
      if (!name.equals(""))
      {
    	useName = true;
        LOGGER.debug("Using parameter '{}' as screen name to switch to.");
      }
    }
	  try
	  {
      if(useName)
      {
    	  //Switch to the stored handle which has been initial set
    	  if (name.toLowerCase().equals("default"))
    	  {
    		  LOGGER.debug("Switching back to default");
    		  try
    		  {
    			  webdriver.switchTo().window(currentWindow);
    		  }
    		  catch(Exception e)
    		  {
    			  webdriver.switchTo().defaultContent();
    		  }
    	  }
    	  //switch tot the window with the given title
    	  else
    	  {
    		  //currentWindow = webdriver.getWindowHandle(); 
          Set<String> availableWindows = webdriver.getWindowHandles(); 
          if (!availableWindows.isEmpty()) { 
            for (String windowId : availableWindows) 
            { 
              if (webdriver.switchTo().window(windowId).getTitle().startsWith(name)) 
              { 
                LOGGER.debug("Window " + name + " found and webdriver switched to it");
                switched = true;
              } 
              else 
              { 
                webdriver.switchTo().window(currentWindow);
                LOGGER.debug("Window " + name + " not found (yet)");
              } 
            }
          }
          //Handled all windows and failed to switch to the given window.
          if (!switched)
          {
            throw new Exception("Switch to " + name + "failed");
          }
    	  }
      }
      //Switch to the first available window
      else
      {
        Set<String> windowHandles = webdriver.getWindowHandles();
        String currentHandle = webdriver.getWindowHandle();

        if(windowHandles.isEmpty()){
          LOGGER.debug("No window handles available.");
          throw new Exception("No window handles available.");
        }

        if(windowHandles.size() == 1){
          LOGGER.debug("No second screen available to switch to.");
          throw new Exception("No second screen available to switch to.");
        }

        for(String handle : windowHandles){
          if(!currentHandle.equals(handle)){
            LOGGER.trace("Switching to screen: " + handle);
            webdriver.switchTo().window(handle);
            break;
          }
        }
      }
    }
	  catch(NoSuchWindowException eX)
	  {
		  LOGGER.debug("Exception while switching screens: No such window! (fail: {}", failOnError);
		  
		  if(failOnError)this.doCaptureScreen("doSwitchScreen"); throw eX;
	  }
	  catch(Exception eX)
	  {
		  LOGGER.debug("Exception while switching screens");
		  this.doCaptureScreen("doSwitchScreen");
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
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    LOGGER.debug("> Validate '{}' with text '{}'", xPath, value);
    int iTimeOut = 0;
    try
    {
    	if (timeOut == 0)
    	{
    		try
    		{
    			LOGGER.trace("Timeout not set; setting timeout to default");
    			timeOut = Integer.parseInt(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
    		}
    		catch(Exception eX)
    		{
    			LOGGER.debug("Setting default timeout failed: " + eX);
    		}
    	LOGGER.trace("Converting {} from milliseconds to seconds", timeOut);
    	iTimeOut = Integer.valueOf((int) (timeOut/1000));
    	if(iTimeOut < 1) iTimeOut = 1;
    	LOGGER.trace("Convertion to seconds complete, timeout is {} seconds", iTimeOut);
    	}
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
        String text = element.getText()+ "\r\n";
        text = text + "Attribute @value: " + element.getAttribute("value");
        //LOGGER.trace(text);
        // If exists is false, check if text is not present in element
        if (!exists)
        {
        	//validate if value is not present in text
        	try
        	{
	        	LOGGER.trace("Check if text {} isn't present in element {}", value, xPath);
	        	Assert.assertFalse("The value \"" + value + "\" is found in the text: " + text,
	        			            //wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(element, value))));
	        						wait.until(ExpectedConditions.textToBePresentInElement(element, value)));
        	}
        	catch(AssertionError eX)
        	
        	//catch(TimeoutException eX)
        	{
        		//validate if value is not present in attribute @value
        		try
        		{
        			LOGGER.trace("Check if the attribute @value, in element {}, doesn't contain the text {}", xPath,value );
        			Assert.assertFalse("The value \"" + value + "\" is found in the text: " + text,
    			            //wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementValue(element, value))));
        					wait.until(ExpectedConditions.textToBePresentInElementValue(element, value)));
        		}
        		catch(AssertionError e)
        		//catch(TimeoutException e)
        		{
        			//this.doCaptureScreen("doValidateText");
        			throw new TimeoutException("The value \"" + value + "\" is found in the text: " + text);
        			//throw e;
        		}
        	}
        }
        // Check if element contains the given text
        else
        {
        	//validate if value is present in text
        	try
        	{
        		LOGGER.trace("Check if text {} is present in element {}", value, xPath);
        		Assert.assertTrue("The value \"" + value + "\" is not found in the text: " + text,
 			           wait.until(ExpectedConditions.textToBePresentInElement(element, value)));
        	}
        	catch(TimeoutException eX)
        	{
        		//validate if value is present in attribute @value
        		try
        		{
        			LOGGER.trace("Check if the attribute @value, in element {}, contains the text {}", xPath,value );
        			Assert.assertTrue("The value \"" + value + "\" is not found in the text: " + text,
      			           wait.until(ExpectedConditions.textToBePresentInElementValue(element, value)));
        		}
        		catch(TimeoutException e)
        		{
        			//this.doCaptureScreen("doValidateText");
        			throw new TimeoutException("The value \"" + value + "\" is not found in the text: " + text);
        		}
        	}
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
          this.doCaptureScreen("doValidateText");
          throw eX;
        }
      }
    }   
  }
  
  @Override
  public void doStoreText(String xPath, String regex, String name, double timeOut) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    LOGGER.debug("> Store text '{}'", xPath);
    int iTimeOut = 0;
    try
    {
    	if (timeOut == 0)
    	{
    		try
    		{
    			LOGGER.trace("Timeout not set; setting timeout to default");
    			timeOut = Integer.parseInt(testRunner.getPropertyValue(Config.PROVA_TIMEOUT));
    		}
    		catch(Exception eX)
    		{
    			LOGGER.debug("Setting default timeout failed: " + eX);
    		}
    	LOGGER.trace("Converting {} from milliseconds to seconds", timeOut);
    	iTimeOut = (int) (timeOut/1000);
    	if(iTimeOut < 1) iTimeOut = 1;
    	LOGGER.trace("Conversion to seconds complete, timeout is {} seconds", iTimeOut);
    	}
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
        String text = element.getText()+ "\r\n";
        
        LOGGER.debug("Found the following text in element: " + text);
        
        if(regex != null)
        {
          if(regex.trim().length() > 0)
          {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);        

            if(matcher.find())
            {
              text = matcher.group(0);
            }
            else
            {
              LOGGER.warn("No value has been retrieved using regex, element text is stored instead.");
            }

            LOGGER.trace("The following text has been retrieved with regex: " + text);
          }
        }
        
        //Store the found text als a property under the provided name
        this.testRunner.setPropertyValue(name, text);
        
        // action succeeded. Return.
        return;
      }
      catch(Exception eX)
      {
        if(++count > maxRetries)
        {
          LOGGER.debug("Exception storing text");
          this.doCaptureScreen("doStoreText");
          throw eX;
        }
      }
    }   
  }
  
  @Override
  public void doSwitchFrame(String xPath, Boolean alert, Boolean accept) throws Exception
  {
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    LOGGER.debug(">> Switch to frame");
    
    int count = 0;
    
    while(true)
    {
      try
      {
        if (alert)
        {
        	//if 'alert' is true, we're expecting a non web message
        	LOGGER.trace("Switching to alert (doSwitchFrame)");
        	Alert popupalert = webdriver.switchTo().alert();
        	if (accept)
        	{
        		//accepting the message by clicking 'yes' or whatever
        		LOGGER.trace("Accepting alert (doSwitchFrame)");
        		popupalert.accept();
        	}
        	else if (!accept)
        	{
        		//dismissing the message by clicking 'no' or whatever
        		LOGGER.trace("Dismissing alert (doSwitchFrame)");
        		popupalert.dismiss();
        	}
        	else
        	{
        		//if check on boolean works properly, the else is never reached
        		throw new Exception("Value of Boolean 'alert' not valid");
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
		        
		        // switching to frame by element, selected by xpath
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
          this.doCaptureScreen("doSwitchFrame");
          throw eX;
        }
      }
    }    
  }
  
  private WebElement findElement(String xPath)
  {
    if(this.webdriver == null)
    {
      try
      {
        prepareWebdriver();
      }
      catch (Exception ex)
      {
        LOGGER.error(ex);
      }
    }
    
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

    public void tearDown(TestCase tc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
