package nl.dictu.prova.framework.parameters;

/**
 * Contains all the common functions of the test action parameter Bool.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class Bool extends Parameter
{
  private Boolean value = null;
  
  
  /**
   * Constructor with default value False
   */
  public Bool()
  {
    super();
    this.value = false;
  }

  
  /**
   * Constructor with configurable default value
   *
   * @param defaultValue
   */
  public Bool(Boolean defaultValue) throws Exception
  {
    super();
    
    this.setValue(defaultValue);
  }
  
  
  /**
   * Set value.
   * 
   * @param value
   * @throws Exception
   */
  public void setValue(Boolean value) throws Exception
  {
    if(isValid(value))
      this.value = value;
    else
      throw this.getLastException();
  }
  
  
  /**
   * Set value.
   * 
   * @param value
   * @throws Exception
   */
  public void setValue(String value) throws Exception
  {
    setValue(parseBoolean(value));
  }

  
  /**
   * Get configured trueFalse value.
   * 
   * @return
   */
  public Boolean getValue()
  {
    return this.value;
  }
  
  
  /**
   * Validate if configured value is a valid Boolean.
   * 
   * @param value
   * @return
   */
  public boolean isValid()
  {
    return isValid(value);
  }
  
  
  /**
   * Validate if <value> is a valid Boolean.
   * 
   * @param value
   * @return
   */
  public boolean isValid(Boolean value)
  {
    LOGGER.trace("Validate '{}' as a Boolean.", () -> value);
    
    return validateBoolean(value);
  }
  
  
  /**
   * Validate if <value> is a valid Boolean.
   * 
   * @param value
   * @return
   */
  public boolean isValid(String value)
  { 
    try
    {
      LOGGER.trace("Validate '{}' as a Boolean.", () -> value);
      
      return validateBoolean(parseBoolean(value));
    }
    catch (Exception e){}
    
    return false;
  }
}
