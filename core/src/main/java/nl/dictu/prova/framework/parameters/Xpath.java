package nl.dictu.prova.framework.parameters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
