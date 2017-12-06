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
package nl.dictu.prova.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.plugins.output.OutputPlugin;

/**
 * Contains all the common functions of a test action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-06
 */
public abstract class TestAction
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ATTR_PARAMETER = "PARAMETER";
  
  protected TestRunner   testRunner;
  protected OutputPlugin outputPlugin;
  protected Exception    lastValidationException = null;
 
  public abstract void setAttribute(String key, String value) throws Exception;
  
  public abstract void execute() throws Exception;
  public abstract boolean isValid() throws Exception;
  
  protected String actionId;
  
  /**
   * Constructor
   */
  public TestAction()
  {
    // TODO: Implement constructor
  }
  
  /**
   * Set a reference to the test runner. Needed to access the output plug-in.
   * 
   * @param testRunner
   */
  public void setTestRunner(TestRunner testRunner)
  {
    this.testRunner = testRunner;
  }
  
  /**
   * Get the last validation exception that occurred
   * @return
   */
  public Exception getLastValidationException()
  {
    return this.lastValidationException;
  }
  
  /**
   * Set the action identifier.
   * This could be the line number in the test script
   * 
   * @param newActionId
   */
  public void setId(String newActionId)
  {
    this.actionId = newActionId;
  }
  
  /**
   * Get the actions unique Id
   * @return
   */
  public String getId()
  {
    return this.actionId;
  }
}
