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
   * Validate if <value> is a valid Xpath.
   * 
   * @param value
   * @return
   */
  public boolean isValid(String xPath)
  {
    LOGGER.trace("Validate '{}' as Xpath. Min: {}, Max: {}", 
                 () -> xPath, () -> minLength, () -> maxLength);
    
    // TODO implement a proper Xpath validation
    return validateString(xPath, minLength, maxLength);
  }  
}
