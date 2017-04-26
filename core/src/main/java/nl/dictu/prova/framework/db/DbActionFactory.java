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
package nl.dictu.prova.framework.db;

import nl.dictu.prova.framework.ActionFactory;
import nl.dictu.prova.framework.db.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestAction;

/**
 * A factory that allows the input plug-in to create a new web-action.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-19
 */
public class DbActionFactory implements ActionFactory
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public final static String ACTION_PROCESSDBRESPONSE   = "PROCESSDBRESPONSE";
  public final static String ACTION_POLLFORDBRESULT     = "POLLFORDBRESULT";
  public final static String ACTION_SETDBPROPERTIES     = "SETDBPROPERTIES";
  public final static String ACTION_SETDBPOLLPROPERTIES = "SETDBPOLLPROPERTIES";
  public final static String ACTION_SETQUERY            = "SETDBQUERY";
  public final static String ACTION_EXECUTEDBTEST       = "EXECUTEDBTEST";
    
  /**
   * Get the corresponding action for <name>
   * 
   * @param name
   * @return
   * @throws Exception
   */
  public TestAction getAction(String name) throws Exception
  {
    LOGGER.trace("Request to produce dbaction '{}'", () -> name);
    
    switch(name.toUpperCase())
    {
      case ACTION_PROCESSDBRESPONSE:    return new ProcessDbResponse();
      case ACTION_SETDBPROPERTIES:      return new SetDbProperties();
      case ACTION_SETDBPOLLPROPERTIES:  return new SetDbPollProperties();
      case ACTION_SETQUERY:             return new SetQuery();
      case ACTION_EXECUTEDBTEST:        return new ExecuteDbTest();
      case ACTION_POLLFORDBRESULT:      return new PollForDbResult();
    }
    
    throw new Exception("Unknown action '" + name + "' requested");
  }
}
