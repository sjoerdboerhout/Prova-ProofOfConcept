package nl.dictu.prova.framework.parameters;

import org.junit.BeforeClass;

import nl.dictu.prova.Junit;

/**
 * Contains all the common functions of the test action parameter TimeOut.
 * TimeOut extends the basic functions of Number
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class TimeOutTest
{
  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    Junit.configure();
  }
  
  /**
   * Constructor
   * @throws Exception 
   *//*
  public TimeOut() throws Exception
  {
    super();
    
    super.setMinValue(0);       //  0 seconds
    super.setMaxValue(1800000); // 30 minutes
  }
  
  *//**
   * Constructor with default timeout
   * 
   * @throws Exception 
   *//*
  public TimeOut(Integer defaultTimeOut) throws Exception
  {
    super();
    
    super.setMinValue(0);           //  0 seconds
    super.setMaxValue(1800000);     // 30 minutes
    super.setValue(defaultTimeOut);
  }*/
}
