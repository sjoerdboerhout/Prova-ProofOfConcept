package nl.dictu.prova.framework.parameters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.dictu.prova.Junit;

/**
 * Contains all the common functions of the test action parameter Bool.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class BoolTest
{
  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    Junit.configure();
  }

  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test default value
   */
  @Test
  public void TestDefaultValue() 
  {
    try
    {
      Bool bool = new Bool();
      
      assertFalse(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test initial value true
   */
  @Test
  public void TestInitialValueTrue() 
  {
    try
    {
      Bool bool = new Bool(true);
      
      assertTrue(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  } 

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test initial value false
   */
  @Test
  public void TestInitialValueFalse() 
  {
    try
    {
      Bool bool = new Bool(false);
      
      assertFalse(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set/get bool value false
   */
  @Test
  public void TestSetBoolValueFalse() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue(false);
      
      assertFalse(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set/get string value false
   */
  @Test
  public void TestSetStringValueFalse() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue("false");
      assertFalse(bool.getValue());

      bool.setValue("FALSE");
      assertFalse(bool.getValue());

      bool.setValue("False");
      assertFalse(bool.getValue());

      bool.setValue("FaLsE");
      assertFalse(bool.getValue());

    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set/get bool value true
   */
  @Test
  public void TestSetBoolValueTrue() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue(true);
      
      assertTrue(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set/get string value true
   */
  @Test
  public void TestSetStringValueTrue() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue("true");
      assertTrue(bool.getValue());

      bool.setValue("TRUE");
      assertTrue(bool.getValue());

      bool.setValue("True");
      assertTrue(bool.getValue());

      bool.setValue("TrUe");
      assertTrue(bool.getValue());

    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }
  
  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set invalid bool value "null"
   */
  @Test
  public void TestSetInvalidBoolValueNull() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue("null");
      
      assertFalse(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }


  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test set/get empty string value
   */
  @Test
  public void TestSetInvalidEmptyStringValue() 
  {
    try
    {
      Bool bool = new Bool();
      
      bool.setValue("");
      assertFalse(bool.getValue());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }


  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test isValid function
   */
  @Test
  public void TestIsValidWithValueTrue() 
  {
    try
    {
      Bool bool = new Bool();
      
      //bool.setValue(true);
      assertTrue(bool.isValid());
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test valid with string value true
   */
  @Test
  public void TestValidWithValueTrue() 
  {
    try
    {
      Bool bool = new Bool();
      
      assertTrue(bool.isValid("true"));
      assertTrue(bool.isValid("TRUE"));
      assertTrue(bool.isValid("True"));
      assertTrue(bool.isValid("TrUe"));
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }

  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test valid with string value false
   */
  @Test
  public void TestValidWithValueFalse() 
  {
    try
    {
      Bool bool = new Bool();
      
      assertTrue(bool.isValid("false"));
      assertTrue(bool.isValid("FALSE"));
      assertTrue(bool.isValid("False"));
      assertTrue(bool.isValid("FaLsE"));
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }
  
  /*
   * Issue ID:    PROVA-38
   * Requirement: ...
   * 
   * Test random string values are parsed to
   * a valid boolean value
   */
  @Test
  public void TestFalseForInValidValues() 
  {
    try
    {
      Bool bool = new Bool();

      assertTrue(bool.isValid(""));
      assertTrue(bool.isValid("sheep"));
      assertTrue(bool.isValid("sjoerd"));
      assertTrue(bool.isValid("PROVA"));
    }
    catch(Exception eX)
    {
      fail(eX.getMessage());
    }
  }
}
