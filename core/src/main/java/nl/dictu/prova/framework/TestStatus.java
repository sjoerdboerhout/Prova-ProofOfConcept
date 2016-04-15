package nl.dictu.prova.framework;

/**
 * @author Sjoerd Boerhout
 * 
 * @since 2016-04-14
 *
 */
public enum TestStatus
{
  NOTRUN("NotRun"),
  BLOCKED("Blocked"),
  PASSED("Passed"),
  FAILED("Failed");  
  
  private String name;
  
  private TestStatus(String name)
  {
    this.name = name;
  }
  
  /**
   * Get the name of this log level
   * 
   * @return
   */
  @Override
  public String toString() 
  {
    return this.name;
  }
  
  /**
   * Get the name for this test status
   * 
   * @return
   */
  public String getValue()
  {
    return this.name;
  }
  
  /**
   * Find enum by it's name
   * 
   * @param name
   * @return
   */
  public static TestStatus lookup(String name)
  {
    name = name.toUpperCase();
    
    for(TestStatus testStatus : TestStatus.values()) 
    {     
      if(testStatus.name().equalsIgnoreCase(name)) 
      {
        return testStatus;
      }
    }
    return null;
  }
 }
