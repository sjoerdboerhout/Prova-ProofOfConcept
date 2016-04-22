package nl.dictu.prova.framework.parameters;

import org.junit.BeforeClass;

import nl.dictu.prova.Junit;

/**
 * Contains all the common functions of the test action parameter Url.
 * Url extends the basic functions of Text
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class UrlTest
{ 
  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    Junit.configure();
  } 
  
/*  *//**
   * Constructor
   * @throws Exception 
   *//*
  public Url() throws Exception
  {
    super();
    super.setMinLength(3);
  }
  
  *//**
   * Validate if <value> is a valid text.
   * 
   * @param value
   * @return
   *//*
  public boolean isValid(String text)
  {
    LOGGER.trace("Validate '{}' as an URL. Min: {}, Max: {}", 
                 () -> text, () -> minLength, () -> maxLength);
    
    // TODO Implement a proper filename validation
    return validateString(text, minLength, maxLength);
  }*/
}
