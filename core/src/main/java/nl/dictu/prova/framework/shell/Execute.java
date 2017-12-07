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
    LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
    
    switch(key.toUpperCase())
    {
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
    if(testRunner.containsKeywords(command.getValue()))
    {
      command.setValue(testRunner.replaceKeywords(command.getValue()));
    }
    this.testRunner.getShellActionPlugin().doExecute(command.getValue());
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return(this.getClass().getName() + ": Execute command '" + command.getValue() + "' on the Shell");
  }
}
