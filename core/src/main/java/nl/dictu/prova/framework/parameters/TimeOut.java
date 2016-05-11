package nl.dictu.prova.framework.parameters;

/**
 * Contains all the common functions of the test action parameter TimeOut.
 * TimeOut extends the basic functions of Number
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-21
 */
public class TimeOut extends Number
{
  
  /**
   * Constructor
   * @throws Exception 
   */
  public TimeOut() throws Exception
  {
    super();
    
    super.setMinValue(0);       //  0 seconds
    super.setMaxValue(1800000); // 30 minutes
  }
  
  /**
   * Constructor with default timeout
   * 
   * @throws Exception 
   */
  public TimeOut(Integer defaultTimeOut) throws Exception
  {
    super();
    
    super.setMinValue(0);           //  0 seconds
    super.setMaxValue(1800000);     // 30 minutes
    super.setValue(defaultTimeOut);
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
