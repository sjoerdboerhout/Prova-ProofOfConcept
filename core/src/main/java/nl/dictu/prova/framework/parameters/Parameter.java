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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains all the common functions of a test action parameter.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public abstract class Parameter
{
  protected final static Logger LOGGER = LogManager.getLogger();
  
  public abstract boolean isValid();
  
  protected Exception lastException = null;
  
  /**
   * Constructor
   */
  public Parameter()
  {
  }

  
  /**
   * Get the last exception that occurred
   * 
   * @return
   */
  public Exception getLastException()
  {
    return this.lastException;
  }
 
  
  
  /**
   * Parse <value> to a string, trim spaces if requested.
   * 
   * @param value
   * @param trimValue
   * @param nullAsEmpty
   * @return
   * @throws Exception
   */
  protected String parseString(String value, Boolean trimValue, Boolean nullAsEmpty) throws Exception
  {
    try
    {
      LOGGER.trace("Parse string '{}', Trim: {}, NullAsEmpty: {}", 
                   () -> value, () -> trimValue, () -> nullAsEmpty);
      
      String parsedString = null;
      
      if(value == null && !nullAsEmpty) 
      {
        throw new NullPointerException();
      }
      else if(value == null)
      {
        parsedString = "";
      }
      else
      {
        // Remove spaces at start and end of value if requested
        parsedString = trimValue ? value.trim() : value;
      }
      
      return parsedString;
    }
    catch(NullPointerException eX)
    {
      LOGGER.warn("NullPointerException while parsing string.");
      this.lastException = eX;
      throw eX;
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing string: " + eX.getMessage());
      this.lastException = eX;
      throw eX;
    } 
  }
  

  /**
   * Validate <value> according to the given limitations.
   * 
   * @param value
   * @param minLength
   * @param maxLength
   * @return
   * @throws Exception
   */
  protected Boolean validateString(String value, Integer minLength, Integer maxLength)
  {
    try
    {
      LOGGER.trace("Validate string '{}'. Min length: {}, Max length: {}", 
                   () -> value, () -> minLength, () -> maxLength);

      if(value == null) 
        throw new NullPointerException("Value 'null' is not allowed.");
      
      if(value.length() < minLength)  
        throw new Exception("Minimum lenght is: " + minLength + " (" + value + ")");
        
      if(value.length() > maxLength)  
        throw new Exception("Maximum lenght is: " + maxLength + " (" + value + ")");
      
      return true;
    }
    catch(NullPointerException eX)
    {
      LOGGER.trace(eX.getMessage());
      this.lastException = eX;
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing string: " + eX.getMessage());
      this.lastException = eX;
    }
    
    return false;
  }

  
  
  /**
   * Parse <value> to an integer.
   * 
   * @param value
   * @param nullAsZero
   * @return
   * @throws Exception
   */
  protected Integer parseInteger(String value, Boolean nullAsZero) throws Exception
  {
    try
    {
      LOGGER.trace("Parse to integer: '{}', NullAsZero: {}.", () -> value, () -> nullAsZero);
      
      Integer parsedInteger = null;
      
      if(value == null && !nullAsZero)
      {
        throw new NullPointerException();
      }
      else if(value == null)
      {
        parsedInteger = 0;
      }
      else
      {
        parsedInteger = Integer.parseInt(value);
      }
      
      return parsedInteger;
    }
    catch(NullPointerException eX)
    {
      LOGGER.warn("NullPointerException while parsing integer.");
      this.lastException = eX;
      throw eX;
    }
    catch(NumberFormatException eX)
    {
      LOGGER.warn("NumberFormatException while parsing integer. (" + eX.getMessage() + ")");
      this.lastException = eX;
      throw eX;
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing string: " + eX.getMessage());
      this.lastException = eX;
      throw eX;
    }
  }
  
  /**
   * Validate <value> according to given limitations.
   * 
   * @param value
   * @param minValue
   * @param maxValue
   * @return
   * @throws Exception
   */
  protected Boolean validateInteger(Integer value, Integer minValue, Integer maxValue)
  {
    LOGGER.trace("Validate integer '{}'. Min: {}, Max: {}", 
                 () -> value, () -> minValue, () -> maxValue);
    try
    {
      if(value == null) 
        throw new NullPointerException("Value 'null' is not allowed.");
      
      if(value != null)
      { 
        if(value < minValue) 
          throw new Exception("Value '" + value + "' is < " + minValue );
  
        if(value > maxValue) 
          throw new Exception("Value '" + value + "' is > " + maxValue);
      }
      
      return true;
    }
    catch(NullPointerException eX)
    {
      LOGGER.trace(eX.getMessage());
      this.lastException = eX;
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing integer: " + eX.getMessage());
      this.lastException = eX;
    }
    
    return false;
  }
 
  
  
  /**
   * Parses the string <value> as a boolean. The boolean returned represents 
   * the value true if the string argument is not null and is equal, ignoring
   * case, to the string "true".
   * 
   * @param value
   * @return
   * @throws Exception
   */
  protected Boolean parseBoolean(String value) throws Exception
  {
    LOGGER.trace("Parse to boolean '{}'", () -> value);
    
    try
    {
      return Boolean.parseBoolean(value);
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing boolean: " + eX.getMessage());
      this.lastException = eX;
      throw eX;
    }
  }
 
  /**
   * Validate boolean <value> according to given limitations.
   * 
   * @param value
   * @return
   * @throws Exception
   */
  protected Boolean validateBoolean(Boolean value)
  {
    LOGGER.trace("Validate boolean '{}'", () -> value);
    
    try
    {
      if(value == null)
        throw new NullPointerException("Value 'null' is not allowed.");
        
      return true;
    }
    catch(NullPointerException eX)
    {
      LOGGER.trace(eX.getMessage());
      this.lastException = eX;
    }
    catch(Exception eX)
    {
      LOGGER.error("Unhandeld exception while parsing boolean: " + eX.getMessage());
      this.lastException = eX;
    }
    
    return false;
  }
}
