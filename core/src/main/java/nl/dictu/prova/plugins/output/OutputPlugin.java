package nl.dictu.prova.plugins.output;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;

/**
 * Describes the functions that must be available for the other parts of the 
 * framework to execute test actions.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public interface OutputPlugin
{
  public String getName();
  
  public void init(TestRunner testRunner) throws Exception;
  public void shutDown();
  
  public void setUp(TestCase testCase) throws Exception;
  public void tearDown(TestCase testCase) throws Exception;
  
  
  public void doCaptureScreen(String fileName) throws Exception;
  
  public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception;
  
  public void doDownloadFile(String url, String saveAs) throws Exception;
  
  public void doSelect(String xPath, Boolean select) throws Exception;
  
  public void doSelectDropdown(String xPath, String select) throws Exception;
  
  public void doSendKeys(String xPath, String keys) throws Exception;
  
  public void doSetText(String xPath, String text) throws Exception;
  
  public void doSleep(long waitTime) throws Exception;
  
  public void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception;
  
  public void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception;
  
  public void doSwitchFrame (String xPath, Boolean alert, Boolean accept) throws Exception;
}
