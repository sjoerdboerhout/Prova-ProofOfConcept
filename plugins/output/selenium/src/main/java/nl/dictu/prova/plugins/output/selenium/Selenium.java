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
 * Date: 23-08-2016 Author(s): Sjoerd Boerhout
 * <p>
 */
package nl.dictu.prova.plugins.output.selenium;

import nl.dictu.prova.plugins.output.selenium.actions.UploadFile;
import nl.dictu.prova.plugins.output.selenium.actions.SwitchScreen;
import nl.dictu.prova.plugins.output.selenium.actions.Navigate;
import nl.dictu.prova.plugins.output.selenium.actions.DownloadFile;
import java.security.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.TestType;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.OutputPlugin;
import nl.dictu.prova.plugins.output.selenium.actions.CaptureScreen;
import nl.dictu.prova.plugins.output.selenium.actions.Click;
import nl.dictu.prova.plugins.output.selenium.actions.Select;
import nl.dictu.prova.plugins.output.selenium.actions.SelectDropDown;
import nl.dictu.prova.plugins.output.selenium.actions.SendKeys;
import nl.dictu.prova.plugins.output.selenium.actions.SetText;
import nl.dictu.prova.plugins.output.selenium.actions.Sleep;
import nl.dictu.prova.plugins.output.selenium.actions.SwitchFrame;
import nl.dictu.prova.plugins.output.selenium.actions.ValidateElement;
import nl.dictu.prova.plugins.output.selenium.actions.ValidateText;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Sjoerd Boerhout
 */
public class Selenium implements OutputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner testRunner;
  private WebDriver webdriver;
  private int maxTimeOut;
  private int maxRetries;
  
  public final static String ACTION_CAPTURESCREEN   = "CAPTURESCREEN";
  public final static String ACTION_CLICK           = "CLICK";
  public final static String ACTION_DOWNLOADFILE    = "DOWNLOADFILE";
  public final static String ACTION_NAVIGATE	      = "NAVIGATE";
  public final static String ACTION_SELECT          = "SELECT";
  public final static String ACTION_SELECTDROPDOWN  = "SELECTDROPDOWN";
  public final static String ACTION_SENDKEYS        = "SENDKEYS";
  public final static String ACTION_SETTEXT         = "SETTEXT";
  public final static String ACTION_SLEEP           = "SLEEP";
  public final static String ACTION_SWITCHFRAME     = "SWITCHFRAME";
  public final static String ACTION_SWITCHSCREEN    = "SWITCHSCREEN";
  public final static String ACTION_VALIDATEELEMENT = "VALIDATEELEMENT";
  public final static String ACTION_VALIDATETEXT    = "VALIDATETEXT";
  public final static String ACTION_UPLOADFILE      = "UPLOADFILE";

  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    this.testRunner = testRunner;
  }

  @Override
  public void shutDown()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getName()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TestType[] getTestType()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setUp(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void tearDown(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /*
     * Factory functions that returns an instance of the requested action.
     * All actions extend nl.dictu.prova.framework.TestAction.
   */
  @Override
  public TestAction getTestAction(String name) throws InvalidParameterException
  {
    LOGGER.trace("Request to produce webaction '{}'", () -> name);

    switch (name.toUpperCase())
    {
      case ACTION_CAPTURESCREEN: return new CaptureScreen(this);
      case ACTION_CLICK: return new Click(this);
      case ACTION_DOWNLOADFILE: return new DownloadFile(this);
      case ACTION_NAVIGATE: return new Navigate(this);
      case ACTION_SELECT: return new Select(this);
      case ACTION_SELECTDROPDOWN: return new SelectDropDown(this);
      case ACTION_SENDKEYS: return new SendKeys(this);
      case ACTION_SETTEXT: return new SetText(this);
      case ACTION_SLEEP: return new Sleep(this);
      case ACTION_SWITCHFRAME: return new SwitchFrame(this);
      case ACTION_SWITCHSCREEN: return new SwitchScreen(this);
      case ACTION_VALIDATEELEMENT: return new ValidateElement(this);
      case ACTION_VALIDATETEXT: return new ValidateText(this);
      case ACTION_UPLOADFILE: return new UploadFile(this);
    }
    
    throw new InvalidParameterException("Unknown action '" + name + "' requested");
  }  

  public WebElement findElement(String xPath)
  {
    WebElement element = null;

    int count = 0;

    while (true)
    {
      try
      {
        LOGGER.trace("Find element: '{}'", xPath);

        // Wait until element visible
        Integer timeOut = (int) (maxTimeOut / 1000);
        LOGGER.trace("Wait time for element: '{}'", timeOut);
        WebDriverWait wait = new WebDriverWait(webdriver, timeOut);
        //wait.until(ExpectedConditions.elementToBeClickable(element));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
        LOGGER.trace("Found element is clickable. Let's try!");

        element = webdriver.findElement(By.xpath(xPath));

        LOGGER.trace("Found element: '{}'", (element != null ? "YES" : "NO"));

        return element;
      }
      catch (StaleElementReferenceException eX)
      {
        if (++count > maxRetries)
        {
          throw eX;
        }
        LOGGER.trace("Element '{}' is no longer attached to the DOM. Try again. ({})", xPath, count);
      }
      catch (NoSuchElementException eX)
      {
        if (++count > maxRetries)
        {
          throw eX;
        }
        LOGGER.trace("Element '{}' not found. Try again. ({})", xPath, count);
      }
      catch (TimeoutException eX)
      {
        if (++count > maxRetries)
        {
          throw eX;
        }
        LOGGER.trace("TimeOut Exception on '{}'. Try again. ({})", xPath, count);
      }
      catch (Exception eX)
      {
        if (++count > maxRetries)
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
  
  public TestRunner getTestRunner()
  {
    LOGGER.trace("Request for testRunner");
    
    return testRunner;
  }

  public WebDriver getWebdriver()
  {
    LOGGER.trace("Request for webdriver '{}'", () -> webdriver.toString());
    
    return webdriver;
  }

  public int getMaxTimeOut()
  {
    LOGGER.trace("Request for max timeout '{}'", () -> maxTimeOut);
    
    return maxTimeOut;
  }

  public void setMaxTimeOut(int maxTimeOut)
  {
    LOGGER.trace("Set value of max timeout to '{}'", () -> maxTimeOut);
    
    this.maxTimeOut = maxTimeOut;
  }

  public int getMaxRetries()
  {
    LOGGER.trace("Request for max retries '{}'", () -> maxRetries);
    
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries)
  {
    LOGGER.trace("Set value of max retries to '{}'", () -> maxRetries);
    
    this.maxRetries = maxRetries;
  }

}
