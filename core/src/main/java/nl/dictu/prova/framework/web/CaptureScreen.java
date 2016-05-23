package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.FileName;
import nl.dictu.prova.framework.parameters.Text;

/**
 * Handles the Prova function 'capture screen' to save a screendump of the
 * current view.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class CaptureScreen extends TestAction
{
  //Action attribute names
  public final static String ATTR_FILENAME = "FILENAME";

  private FileName fileName = null;
  
  
  /**
   * Constructor
   * @throws Exception 
   */
  public CaptureScreen() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits
    fileName = new FileName();
  }

  
  /**
   * Set attribute <key> with <value>
   * - Unknown attributes are ignored
   * - Invalid values result in an exception
   * 
   * @param key
   * @param value
   * @throws Exception
   */
  @Override
  public void setAttribute(String key, String value) throws Exception
  {
    LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
    
    switch(key.toUpperCase())
    {
      case ATTR_PARAMETER:
      case ATTR_FILENAME:  
        fileName.setValue(value);
      break;
    }
  }
  
  
  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid()
  {
    if(testRunner == null)  return false;
    if(!fileName.isValid()) return false;
    
    return true;
  }


  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    LOGGER.trace("Execute test action: {}", () -> this.toString());
    
    if(!isValid())
      throw new Exception("Action is not validated!");
    
    testRunner.getWebActionPlugin().doCaptureScreen(fileName.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': Save a screendump to file '" + fileName.getValue() + "'");
  }
}
