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
package nl.dictu.prova.framework.web;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.dictu.prova.Junit;
import nl.dictu.prova.framework.TestAction;

public class WebActionFactoryTest
{
  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    Junit.configure();
  }

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestCaptureScreen() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("CaptureScreen");
      assertTrue(testAction instanceof CaptureScreen);
      
      testAction = new WebActionFactory().getAction("capturescreen");
      assertTrue(testAction instanceof CaptureScreen);
      
      testAction = new WebActionFactory().getAction("CAPTURESCREEN");
      assertTrue(testAction instanceof CaptureScreen);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestClick() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("Click");
      assertTrue(testAction instanceof Click);
      
      testAction = new WebActionFactory().getAction("click");
      assertTrue(testAction instanceof Click);
      
      testAction = new WebActionFactory().getAction("CLICK");
      assertTrue(testAction instanceof Click);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 
  
  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestDownloadFile()
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("DownloadFile");
      assertTrue(testAction instanceof DownloadFile);
      
      testAction = new WebActionFactory().getAction("downloadfile");
      assertTrue(testAction instanceof DownloadFile);
      
      testAction = new WebActionFactory().getAction("DOWNLOADFILE");
      assertTrue(testAction instanceof DownloadFile);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestSelect() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("Select");
      assertTrue(testAction instanceof Select);
      
      testAction = new WebActionFactory().getAction("select");
      assertTrue(testAction instanceof Select);
      
      testAction = new WebActionFactory().getAction("SELECT");
      assertTrue(testAction instanceof Select);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestSendKeys() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("SendKeys");
      assertTrue(testAction instanceof SendKeys);
      
      testAction = new WebActionFactory().getAction("sendkeys");
      assertTrue(testAction instanceof SendKeys);
      
      testAction = new WebActionFactory().getAction("SENDKEYS");
      assertTrue(testAction instanceof SendKeys);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestSetText() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("SetText");
      assertTrue(testAction instanceof SetText);
      
      testAction = new WebActionFactory().getAction("settext");
      assertTrue(testAction instanceof SetText);
      
      testAction = new WebActionFactory().getAction("SETTEXT");
      assertTrue(testAction instanceof SetText);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestSleep() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("Sleep");
      assertTrue(testAction instanceof Sleep);
      
      testAction = new WebActionFactory().getAction("sleep");
      assertTrue(testAction instanceof Sleep);
      
      testAction = new WebActionFactory().getAction("SLEEP");
      assertTrue(testAction instanceof Sleep);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestUploadFile() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("UploadFile");
      assertTrue(testAction instanceof UploadFile);
      
      testAction = new WebActionFactory().getAction("uploadfile");
      assertTrue(testAction instanceof UploadFile);
      
      testAction = new WebActionFactory().getAction("UPLOADFILE");
      assertTrue(testAction instanceof UploadFile);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestValidateElement() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("ValidateElement");
      assertTrue(testAction instanceof ValidateElement);
      
      testAction = new WebActionFactory().getAction("validateelement");
      assertTrue(testAction instanceof ValidateElement);
      
      testAction = new WebActionFactory().getAction("VALIDATEELEMENT");
      assertTrue(testAction instanceof ValidateElement);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  /*
   * Issue ID:    PROVA-38
   * Requirement: A factory produces specific actions with a common interface 
   * 
   * Test if the action word gives the correct action
   */
  @Test
  public void TestValidateText() 
  {
    try
    {
      TestAction testAction = new WebActionFactory().getAction("ValidateText");
      assertTrue(testAction instanceof ValidateText);
      
      testAction = new WebActionFactory().getAction("validatetext");
      assertTrue(testAction instanceof ValidateText);
      
      testAction = new WebActionFactory().getAction("VALIDATETEXT");
      assertTrue(testAction instanceof ValidateText);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  }  
}
