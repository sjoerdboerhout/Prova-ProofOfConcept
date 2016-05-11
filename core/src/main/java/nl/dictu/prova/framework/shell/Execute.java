package nl.dictu.prova.framework.shell;

import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.parameters.Text;

/**
 * Handles the Prova function 'Execute' to execute a command on the shell
 * of the host system.
 * 
 * @author  Sjoerd Boerhout
 * @since   0.0.1
 */
public class Execute extends TestAction
{
  // Action attribute names
  public final static String ATTR_COMMAND = "COMMAND";

  private Text command;
  
  
  /**
   * Constructor
   * @throws Exception 
   */
  public Execute() throws Exception
  {
    super();
    
    // Create parameters with (optional) defaults and limits.
    command = new Text();
    command.setMinLength(1);
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
    switch(value.toUpperCase())
    {
      case ATTR_PARAMETER: 
      case ATTR_COMMAND:  
        command.setValue(value);
      break;
    }
  }
  

  /**
   * Check if all requirements are met to execute this action
   */
  @Override
  public boolean isValid() throws Exception
  {
    if(testRunner == null)  return false;
    if(!command.isValid())  return false;
    
    return true;
  }
  
  
  /**
   * Execute this action in the active output plug-in
   */
  @Override
  public void execute() throws Exception
  {
    if(!isValid())
      throw super.getLastValidationException();
    
    // TODO Implement function
    System.out.println("Execute command '" + command + "' on the Shell");
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return(this.getClass().getName() + ": Execute command '" + command + "' on the Shell");
  }
}
