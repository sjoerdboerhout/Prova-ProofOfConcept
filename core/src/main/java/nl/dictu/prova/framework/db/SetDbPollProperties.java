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

import nl.dictu.prova.framework.TestAction;

/**
 *
 * @author Coos van der Galiën
 */
public class SetDbPollProperties extends TestAction
{

  private Integer waittime;
  private Integer retries;
  private String result;

  @Override
  public void setAttribute(String key, String value) throws Exception
  {
    LOGGER.trace("Request to set '{}' to '{}'", () -> key, () -> value);
    switch (key)
    {
      case ("prova.properties.waittime"):
        waittime = Integer.parseInt(value);
        break;
      case ("prova.properties.retries"):
        retries = Integer.parseInt(value);
        break;
      case ("prova.properties.result"):
        result = value.trim();
        break;
      default:
        LOGGER.error("Attribute not supported.");
    }
  }

  @Override
  public void execute() throws Exception
  {
    LOGGER.info("> Execute test action: {}", () -> this.getClass().getSimpleName());
    if (!isValid())
    {
      throw new Exception("Poll properties not set properly!");
    }
    this.testRunner.getDbActionPlugin().doSetDbPollProperties(waittime, retries, result);
  }

  @Override
  public boolean isValid() throws Exception
  {
    if (retries == null)
    {
      return false;
    }
    if (waittime == null)
    {
      return false;
    }
    if (result == null)
    {
      return false;
    }
    return true;
  }

  /**
   * Return a string representation of the objects content
   *
   * @return
   */
  @Override
  public String toString()
  {
    return ("'" + this.getClass().getSimpleName().toUpperCase() + "': '" + retries + "', '" + waittime + "', '" + result + "'");
  }

}
