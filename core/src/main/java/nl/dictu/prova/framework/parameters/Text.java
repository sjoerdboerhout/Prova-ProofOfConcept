package nl.dictu.prova.framework.parameters;

/**
 * Contains all the common functions of the test action parameter Text.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class Text extends Parameter
{
  protected String value            = null;
  
  private final Integer MIN_LENGTH  = 0;
  private final Integer MAX_LENGTH  = Integer.MAX_VALUE;
  
  protected Integer minLength       = MIN_LENGTH;
  protected Integer maxLength       = MAX_LENGTH;
  
  
  /**
   * Constructor
   */
  public Text()
  {
    super();
  }
  

  /**
   * Set text.
   * 
   * @param text
   * @throws Exception
   */
  public void setValue(String value) throws Exception
  {
    if(isValid(value))
      this.value = value;
    else
      throw this.getLastException();
  }
  
  
  /**
   * Get configured text.
   * 
   * @return
   */
  public String getValue()
  {
    return this.value;
  }

  
  /**
   * Validate if configured <value> is a valid text.
   * 
   * @param value
   * @return
   */
  public boolean isValid()
  {
    return isValid(value);
  }

  
  /**
   * Validate if <value> is a valid text.
   * 
   * @param value
   * @return
   */
  public boolean isValid(String text)
  {
    LOGGER.trace("Validate '{}' as a String. Min: {}, Max: {}", 
                 () -> text, () -> minLength, () -> maxLength);
    
    return validateString(text, minLength, maxLength);
  }
  
  
  /**
   * Set min length.
   * 
   * @param minLength
   * @throws Exception
   */
  public void setMinLength(Integer minLength) throws Exception
  {
    if(minLength < MIN_LENGTH)
    {
      throw new Exception("Minimum value must be >= " + MIN_LENGTH + 
                          " (" + minLength + ")");
    }
    
    if(minLength > maxLength)
    {
      throw new Exception("New maximum length must be <= " + maxLength + 
                          " (" + minLength + ")");
    }
    
    this.minLength = minLength;
  }

  
  /**
   * Set max length.
   * 
   * @param maxLength
   * @throws Exception
   */
  public void setMaxLength(Integer maxLength) throws Exception
  {
    if(maxLength > MAX_LENGTH)
    {
      throw new Exception("Maximum value must be <= " + MAX_LENGTH + 
                          " (" + maxLength + ")");
    }
    
    if(maxLength < minLength)
    {
      throw new Exception("New maximum timeout must be >= " + minLength + 
                          " (" + maxLength + ")");
    }
    
    this.maxLength = maxLength;
  }

}
