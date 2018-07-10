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
package nl.dictu.prova.framework.soap;

import nl.dictu.prova.framework.TestAction;
import java.util.LinkedHashMap;
import java.util.Map;

public class SetSoapProperties extends TestAction
{
  Map<String, String> propMap = new LinkedHashMap<>();
  //clean up after test
  //String url;
  //String user;
  //String pass;
  String prefix;

  @Override
  public void setAttribute(String key, String value) throws Exception
  {

      switch (key.toLowerCase()) {
          case "prova.properties.prefix":
              prefix = value;
              LOGGER.debug("SetProperties prefix set to " + prefix);
              break;
          default:
              LOGGER.debug("setting property " + key.replace("prova.properties.", "") + " to " + value);
              propMap.put(key.replace("prova.properties.", ""), value);
      }
  }

  @Override
  public void execute() throws Exception
  {
    if (!isValid())
    {
      throw new Exception("testRunner or Properties not properly set!");
    }
    //if (!propMap.containsKey("user"))
    //{
    //    propMap.put("user","null");
    //}
    this.testRunner.getSoapActionPlugin().doSetProperties(propMap, prefix);
  }

  @Override
  public boolean isValid() throws Exception
  {
    if (testRunner == null)
    {
      return false;
    }
    if (!propMap.containsKey("url"))
    {
      return false;
    }
    if (prefix == null)
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
    public String toString() {
        //return ("'" + this.getClass().getSimpleName().toUpperCase() + "': '" + url + "', '" + user + "', '" + pass + "'");
        return ("'" + this.getClass().getSimpleName().toUpperCase() + "': '" + propMap.get("url") + "', '" + prefix + "', '**********'");
    }

}
