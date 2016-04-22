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
      TestAction testAction = WebActionFactory.getAction("CaptureScreen");
      assertTrue(testAction instanceof CaptureScreen);
      
      testAction = WebActionFactory.getAction("capturescreen");
      assertTrue(testAction instanceof CaptureScreen);
      
      testAction = WebActionFactory.getAction("CAPTURESCREEN");
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
      TestAction testAction = WebActionFactory.getAction("Click");
      assertTrue(testAction instanceof Click);
      
      testAction = WebActionFactory.getAction("click");
      assertTrue(testAction instanceof Click);
      
      testAction = WebActionFactory.getAction("CLICK");
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
      TestAction testAction = WebActionFactory.getAction("DownloadFile");
      assertTrue(testAction instanceof DownloadFile);
      
      testAction = WebActionFactory.getAction("downloadfile");
      assertTrue(testAction instanceof DownloadFile);
      
      testAction = WebActionFactory.getAction("DOWNLOADFILE");
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
      TestAction testAction = WebActionFactory.getAction("Select");
      assertTrue(testAction instanceof Select);
      
      testAction = WebActionFactory.getAction("select");
      assertTrue(testAction instanceof Select);
      
      testAction = WebActionFactory.getAction("SELECT");
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
      TestAction testAction = WebActionFactory.getAction("SendKeys");
      assertTrue(testAction instanceof SendKeys);
      
      testAction = WebActionFactory.getAction("sendkeys");
      assertTrue(testAction instanceof SendKeys);
      
      testAction = WebActionFactory.getAction("SENDKEYS");
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
      TestAction testAction = WebActionFactory.getAction("SetText");
      assertTrue(testAction instanceof SetText);
      
      testAction = WebActionFactory.getAction("settext");
      assertTrue(testAction instanceof SetText);
      
      testAction = WebActionFactory.getAction("SETTEXT");
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
      TestAction testAction = WebActionFactory.getAction("Sleep");
      assertTrue(testAction instanceof Sleep);
      
      testAction = WebActionFactory.getAction("sleep");
      assertTrue(testAction instanceof Sleep);
      
      testAction = WebActionFactory.getAction("SLEEP");
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
      TestAction testAction = WebActionFactory.getAction("UploadFile");
      assertTrue(testAction instanceof UploadFile);
      
      testAction = WebActionFactory.getAction("uploadfile");
      assertTrue(testAction instanceof UploadFile);
      
      testAction = WebActionFactory.getAction("UPLOADFILE");
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
      TestAction testAction = WebActionFactory.getAction("ValidateElement");
      assertTrue(testAction instanceof ValidateElement);
      
      testAction = WebActionFactory.getAction("validateelement");
      assertTrue(testAction instanceof ValidateElement);
      
      testAction = WebActionFactory.getAction("VALIDATEELEMENT");
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
      TestAction testAction = WebActionFactory.getAction("ValidateText");
      assertTrue(testAction instanceof ValidateText);
      
      testAction = WebActionFactory.getAction("validatetext");
      assertTrue(testAction instanceof ValidateText);
      
      testAction = WebActionFactory.getAction("VALIDATETEXT");
      assertTrue(testAction instanceof ValidateText);
    }
    catch (Exception eX)
    {
      fail(eX.getMessage());
    }
  }  
}
