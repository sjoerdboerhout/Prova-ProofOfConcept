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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.WebOutputPlugin;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
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
import org.openqa.selenium.security.UserAndPassword;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"false");
        if(testRunner.hasPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_BIN_GECKO))
        {
        	System.setProperty("webdriver.firefox.bin", testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_BIN_GECKO));
        	LOGGER.trace("Try to load specific 'FireFox' on location '{}'", testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_BIN_GECKO));
        }
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
      
      setBrowserResolution(webdriver);
      
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

        webdriver.manage().timeouts().pageLoadTimeout(maxTimeOut, TimeUnit.MILLISECONDS);
        LOGGER.debug("Open URL: '{}'", url);
        try
        {
        	webdriver.get(url);
        }
        catch(TimeoutException te)
        {
        	LOGGER.debug("URL not loaded within the set timeout, trying next step anyway");
        }
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

	/**
	 * If a resolution is given in the configuration, set the browser to that resolution.
	 * 
	 * @param webDriver
	 */
	protected void setBrowserResolution(WebDriver webDriver) {
		if (testRunner.hasPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_RESOLUTION)) {
			try {
				String[] resXY = testRunner.getPropertyValue(Config.PROVA_PLUGINS_OUT_WEB_BROWSER_RESOLUTION)
						.split("x");
				webdriver.manage().window().setSize(new Dimension(new Integer(resXY[0]), new Integer(resXY[1])));
			} catch (Exception e) {
				LOGGER.warn("Problem setting browser resolution.", e);
			}
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
  public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded, Boolean continueOnNotFound) throws Exception
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
          {
        	LOGGER.trace("Element is NULL");
        	if (continueOnNotFound) 
        	{
        		LOGGER.trace("Continue on not found");
        		return;  
        	}
            else
            {
            	throw new Exception("Element '" + xPath + "' not found.");
            }
          }
      
        // TODO support right click
       
        
        LOGGER.trace("Clicking on element '{}' (doClick)", xPath);
        assert element.isDisplayed();
        assert element.isEnabled();
        
        //if(waitUntilPageLoaded)
        //  element.submit();
        //else
        //First navigate to element to make sure the element to be clicked is on te screen
        Actions actions = new Actions(webdriver);
        actions.moveToElement(element).perform();
        try
        {
        	scroll_element_into_view(element);
        }
        catch(Exception e)
        {
        	LOGGER.debug("Scrolling element into view failed");
        }
        if(rightClick)
        {
        	actions.contextClick(element).perform();
        }
        else
        {
        	element.click();
        }
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
	  if (url.equalsIgnoreCase("closebrowser"))
	  {
		  webdriver.close();
		  currentWindow = "";
	      webdriver = null;
	  }
	  else if (url.equalsIgnoreCase("closewindow"))
	  {
		  webdriver.close();
	      //webdriver = null;
	  }
	  else if (url.equalsIgnoreCase("back"))
	  {
		  webdriver.navigate().back();
	  }
	  else if (url.equalsIgnoreCase("forward"))
	  {
		  webdriver.navigate().forward();
	  }
	  else if (url.equalsIgnoreCase("refresh"))
	  {
		  webdriver.navigate().refresh();
	  }
	  else
		  {
		  try {
		  
			qualifiedUrl = new URL(url);
	                
	                LOGGER.debug("Url not malformed, navigating to " + qualifiedUrl.getPath());
		  
	                webdriver.navigate().to(qualifiedUrl);
		  } catch (MalformedURLException e) {
		  	// TODO Auto-generated catch block
		  	LOGGER.debug("Provided URL is malformed.");
		  } 
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
    	WebElement element = findElement(xPath);
    	if(element == null)
            throw new Exception("Element '" + xPath + "' not found.");
    	Select dropdown = new Select(element);
        

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
      keys = keys.replace("<CTRL>", Keys.CONTROL);
      keys = keys.replace("<ALT>", Keys.ALT);
      keys = keys.replace("<PAGEDOWN>", Keys.PAGE_DOWN);
      keys = keys.replace("<PAGEUP>", Keys.PAGE_UP);
      keys = keys.replace("<ARROWUP>", Keys.ARROW_UP);
      keys = keys.replace("<ARROWDOWN>", Keys.ARROW_DOWN);
      keys = keys.replace("<ENTER>", Keys.ENTER);
      keys = keys.replace("<DELETE>", Keys.DELETE);
      keys = keys.replace("<BACKSPACE>", Keys.BACK_SPACE);
      keys = keys.replace("<CLEAR>", Keys.CLEAR);
      
      //if xPath is not filled, sendKeys to the active element
      if (xPath.equalsIgnoreCase("/html/body"))
      {
	      LOGGER.trace("> Send keys '{}' to active element (doSendKeys)", keys);
	      webdriver.switchTo().activeElement().sendKeys(keys);
	      
      }
      else if (xPath.equalsIgnoreCase("frame"))
      {
    	  LOGGER.trace("> Send keys '{}' to frame (doSendKeys)", keys);
    	  Actions actions = new Actions(webdriver);
	      actions.sendKeys(keys).build().perform();
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
    /*if(this.webdriver == null)
    {
      prepareWebdriver();
    }*/
    
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

	private List<String> getCheckStrings(String valueIn) {
		List<String> lChecks = null;
		if (valueIn.contains(";")) {
			try {
				String[] checks = valueIn.split(";");
				lChecks = Arrays.asList(checks);

			} catch (Exception eX) {
				LOGGER.debug("Converting multiple checks to list failed: " + eX);
				throw eX;
			}
		} else {
			try {
				lChecks = Arrays.asList(valueIn);
			} catch (Exception eX) {
				LOGGER.debug("Adding check to list failed: " + eX);
				throw eX;
			}
		}
		return lChecks;
	}  

	private int getTimeout(double timeOut) {
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
	    return iTimeOut;
	}
	
	public void doFastValidateText(String xPath, String valueIn, Boolean exists, double timeOut) throws Exception {

		if (this.webdriver == null) {
			prepareWebdriver();
		}
		LOGGER.debug("> FastValidate '{}' with text '{}'", xPath, valueIn);

		List<String> lChecks = getCheckStrings(valueIn);
		int iNumberChecks = lChecks.size();
		LOGGER.debug(iNumberChecks);

		String htmlPageSource = webdriver.getPageSource();

		String value = "";
		try {
			
			Boolean bCheck = true;
			for (String check : lChecks) {
				LOGGER.debug("Checking: " + check);
				bCheck = true;
				while (bCheck) {
					value = check;

					boolean stringFound = htmlPageSource.contains(check);

					// WebElement element =
					// webdriver.findElement(By.xpath(xPath));
					// if(element == null) //|| !element.isEnabled())
					// {
					// throw new Exception("Element '" + xPath +
					// "' not found.");
					// }
					// // Get text from element
					// String text = element.getText()+ "\r\n";
					// text = text + "Attribute @value: " +
					// element.getAttribute("value");

					boolean valid = (stringFound && exists) || (!stringFound && !exists);

					if (valid) {
						if (exists) {
							LOGGER.info("The value \"" + value + "\" is found in the html");							
						} else {
							LOGGER.info("The value \"" + value + "\" is not found in the html");
						}
					} else {
						if (exists) {
							throw new Exception("The value \"" + value + "\" is not found in the html");
						} else {
							throw new Exception("The value \"" + value + "\" is found in the html");
						}
					}

					bCheck = false;
				}

			}
		} catch (Exception e) {
			LOGGER.info("Exception while fast validating text '{}', msg {}.", value, e.getMessage());
			LOGGER.debug("PageSource " + htmlPageSource);
			this.doCaptureScreen("doFastValidateText");
			throw e;
		}

	}
  
  @Override
  public void doValidateText(String xPath, String valueIn, Boolean exists, double timeOut) throws Exception
  {
//	if ("/html/body".equals(xPath)) {
//		doFastValidateText(xPath, valueIn, exists, timeOut);
//		return;
//	}
	  
    if(this.webdriver == null)
    {
      prepareWebdriver();
    }
    
    LOGGER.debug("> Validate '{}' with text '{}'", xPath, valueIn);
    List<String> lChecks = getCheckStrings(valueIn);
	int iNumberChecks = lChecks.size();
	LOGGER.debug(iNumberChecks);
    int iTimeOut = getTimeout(timeOut);    

    WebDriverWait wait = new WebDriverWait(webdriver, iTimeOut);
    int count = 0;
    String value = "";
    Boolean bCheck = true;
    for(String check: lChecks)
    {
	    LOGGER.debug("Checking: " + check);
	    bCheck = true;
	    count = 0;
    	while(bCheck)
	    {
	      try
	      {
	    	value = check;
	    	WebElement element = webdriver.findElement(By.xpath(xPath));
	    	
	        if(element == null) //|| !element.isEnabled())
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
		        	Assert.assertTrue("The value \"" + value + "\" is found in the text: " + text,
		        			            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(element, value))));
		        						//wait.until(ExpectedConditions.textToBePresentInElement(element, value)));
	        	}
	        	catch(AssertionError | TimeoutException eX)
	        	
	        	//catch(TimeoutException eX)
	        	{
	        		throw new TimeoutException("The value \"" + value + "\" is found in the text: " + text);
	        		//validate if value is not present in attribute @value
	        		/*try
	        		{
	        			LOGGER.trace("Check if the attribute @value, in element {}, doesn't contain the text {}", xPath,value );
	        			Assert.assertTrue("The value \"" + value + "\" is found in the text: " + text,
	    			            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementValue(element, value))));
	        					//wait.until(ExpectedConditions.textToBePresentInElementValue(element, value)));
	        		}
	        		catch(AssertionError | TimeoutException e)
	        		//catch(TimeoutException e)
	        		{
	        			//this.doCaptureScreen("doValidateText");
	        			throw new TimeoutException("The value \"" + value + "\" is found in the text: " + text);
	        			//throw e;
	        		}*/
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
	        	catch(AssertionError | TimeoutException eX)
	        	//catch(TimeoutException eX)
	        	{
	        		//validate if value is present in attribute @value
	        		try
	        		{
	        			LOGGER.trace("Check if the attribute @value, in element {}, contains the text {}", xPath,value );
	        			Assert.assertTrue("The value \"" + value + "\" is not found in the text: " + text,
	      			           wait.until(ExpectedConditions.textToBePresentInElementValue(element, value)));
	        		}
	        		catch(AssertionError | TimeoutException e)
	        		//catch(TimeoutException e)
	        		{
	        			//this.doCaptureScreen("doValidateText");
	        			throw new TimeoutException("The value \"" + value + "\" is not found in the text: " + text);
	        		}
	        	}
	        }
	        
	        // action succeeded. Return.
	        //return;
	        bCheck = false;
	      }
	      catch(Exception eX)
	      {
	        if(++count > maxRetries)
	        {
	          LOGGER.debug("Exception while validating text '{}' in '{}': {} (retry count: {})", 
	                        value, xPath, eX.getMessage(), count);
	          this.doCaptureScreen("doValidateText");
	          throw new Exception("Validation Failed: ", eX);
	        }
	      }
	    }
    }
  }
  
  @Override
  public void doStoreText(String xPath, String regex, String inputtext, String name, double timeOut) throws Exception
  {
	if(inputtext != null)
    {
      if(inputtext.trim().length() > 0) 
      {
    	  LOGGER.trace("> Store text '{}' in variable {}", inputtext, name);
    	  this.testRunner.setPropertyValue(name, inputtext);
      }
      else
      {
    	  LOGGER.warn("The length of the given value is < than 0. Is there a space in the field 'inputtext'?");
      }
    }
	else
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
	        String text = element.getText();//+ "\r\n";
	        
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
	        LOGGER.trace("> Store extracted text '{}' in variable {}", text, name);
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
  }
  
  @Override
  public void doSwitchFrame(String xPath, Boolean alert, Boolean accept, String username, String password) throws Exception
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
        	//if username is passed, try to login
        	if (username.length()>1)
        	{
        		LOGGER.trace("Switched to alert (doSwitchFrame) and trying to pass username and password");
        		popupalert.authenticateUsing(new UserAndPassword(username, password));
        		//webdriver.findElement(By.id("userID")).sendKeys(username);
        		//webdriver.findElement(By.id("password")).sendKeys(password);
        	}
        	if (accept)
        	{
        		//accepting the message by clicking 'yes' or whatever
        		LOGGER.trace("Accepting alert (doSwitchFrame)");
        		try
        		{
        			popupalert.accept();
        		}
        		catch(NoAlertPresentException ae)
        		{
        			LOGGER.trace("Accepting alert failed, maybe it is closed when passing user/password? (doSwitchFrame)");
        		}
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
  @Override
  public void doWaitForElement(String xPath, String type, Boolean value, double timeOut) throws Exception
  {
	    if(this.webdriver == null)
	    {
	      prepareWebdriver();
	    }
	    
	    LOGGER.debug("> Wait for element '{}' ", xPath);
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
	    	else
	    	{
	    		iTimeOut = (int)timeOut;
	    	}
	    }
	    catch(Exception eX)
	    {
	    	LOGGER.debug("Converting to seconds failed: " + eX);
	    	throw eX;
	    }
	    WebDriverWait wait = new WebDriverWait(webdriver, iTimeOut);
	    try
	      {
	    	switch(type.toLowerCase())
	    	{
	    	case("exists"):
	    		LOGGER.trace("type is exists");
	    		if (value)
	    		{
	    			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xPath)));
	    		}
	    		else
	    		{
	    			wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xPath))));
	    		}
	    		break;
	    	case("clickable"):
	    		LOGGER.trace("type is clickable");
		    	if (value)
	    		{
	    			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
	    		}
	    		else
	    		{
	    			wait.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(By.xpath(xPath))));
	    		}
		    	break;
	    	case("visible"):
	    		LOGGER.trace("type is visible");
	    		if (value)
	    		{
	    			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
	    		}
	    		else
	    		{
	    			wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath))));
	    		}
	    		break;
	    	default:
	    		throw new Exception(type + " is not a valid type to wait for...");

	    	}
	    	
	    	
	      }
	    catch(TimeoutException eX)
	    {
	    	throw new TimeoutException(eX);
	    }
  }
  
  private void scroll_element_into_view(WebElement element) {
	    int Y = (element.getLocation().getY() - 200);
	    JavascriptExecutor js = (JavascriptExecutor) webdriver;
	    js.executeScript("javascript:window.scrollTo(0," + Y + ");");}
  
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
        try
        {
        	wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        }
        catch(Exception ex){
        	LOGGER.debug("Element is NOT clickable after waiting " + timeOut +" seconds. Trying to loacate element"
        			+ "anyway, maybe it's hidden");
        }
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
        if(++count > maxRetries)
        { 
        	LOGGER.trace("Max retry count reached... Returning null");
        	return null;
        	//throw eX;
        }
        LOGGER.trace("TimeOut Exception on '{}'. Try again. ({})", xPath, count);
      }
      catch(Exception eX)
      {       
        if(++count > maxRetries)
        {
          //LOGGER.error("Exception while searching element '{}' retry count: '{}', Type: '{}' : '{}'", 
          //  xPath,  
          //  count, 
          //  eX.getClass().getSimpleName(),
          //  eX.getMessage());
          LOGGER.debug("Max retry count reached...");
          return element;
          //throw eX;
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
