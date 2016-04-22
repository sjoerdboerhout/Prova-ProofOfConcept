package nl.dictu.prova.framework.web;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.FileName;
import nl.dictu.prova.framework.parameters.Xpath;

/**
 * Handles the Prova function 'upload file' to upload the given file to the 
 * web page.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class UploadFile extends TestAction
{
  // Action attribute names
  public final static String ATTR_XPATH    = "XPATH";
  public final static String ATTR_FILENAME = "FILENAME";
  
  private Xpath    xPath;
  private FileName fileName;

  
  /**
   * Constructor
   * @throws Exception 
   */
  public UploadFile() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits
    xPath = new Xpath();
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
    switch(key.toUpperCase())
    {
      case ATTR_XPATH:  
        xPath.setValue(value); 
      break;
      
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
    if(!xPath.isValid())    return false;
    if(!fileName.isValid()) return false;
        
    return true;
  }
  

  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    // TODO Implement function
    System.out.println( "Upload file '" + fileName + "' to " + xPath);
  }
}
