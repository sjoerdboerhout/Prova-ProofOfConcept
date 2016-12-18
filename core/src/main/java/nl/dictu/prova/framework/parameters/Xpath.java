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
package nl.dictu.prova.framework.parameters;

/**
 * Contains all the common functions of the test action parameter XPath.
 * XPath extends the basic functions of Text
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class Xpath extends Text
{
  
  /**
   * Constructor
   */
  public Xpath()
  {
    super();
    
    // Update limitations
    minLength = 5;
    maxLength = Integer.MAX_VALUE;
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
  public void setAttribute(String key, String value) throws Exception
  {
    try
    {
      LOGGER.trace("Check if key '{}' exists in xpath '{}'", () -> key, () -> this.value);
      
      // Replace the optional index with 1 if not filled in
      if(key.equalsIgnoreCase("idx") && value.isEmpty())
      {
        value = "1";
      }
      
      if(this.value.contains(key.toLowerCase()))
      {
        LOGGER.trace("Replace key '{}' with '{}'", key, value);
        this.value = this.value.replace("{" + key + "}", value);
      }
    }
    catch(Exception eX)
    {
      LOGGER.debug(eX);
    }
  }
  
  /**
   * Validate if <value> is a valid Xpath.
   * 
   * @param value
   * @return
   */
  public boolean isValid(String xPath)
  {
    LOGGER.trace("Validate '{}' as Xpath. Min: {}, Max: {}", 
                 () -> xPath, () -> minLength, () -> maxLength);
    
    //Pattern KEYWORD_PATTERN = Pattern.compile("\\{(.*?)\\}");
    //Matcher matcher = KEYWORD_PATTERN.matcher(this.getValue());
    
    // Check if all keywords are replaced with a value
    // TODO activate code
    // if(matcher.matches()) return false;
    
    // TODO implement a proper Xpath validation
    return validateString(xPath, minLength, maxLength);
  }


  /**
   * Return a string representation of the objects content
   * 
   * @return 
   */
  @Override
  public String toString()
  {
    return("'" + this.getClass().getSimpleName().toUpperCase() + "': " + this.value);
  } 
}
