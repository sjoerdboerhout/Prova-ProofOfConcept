package nl.dictu.prova.logging;

/**
 * @author Sjoerd Boerhout
 *
 */
public enum LogLevel
{
  /*
   * Enum with all log level definitions: 
   *  
   * Off:     Means that all logging is disabled. Not recommended!
   *  
   * Fatal:   Means that the execution of some task led to an event that will
   *          presumably lead the application to abort. 
   *          Something has definitively gone wrong!
   *  
   * Error:   Means that the execution of some task could not be completed; an 
   *          email couldn't be sent, a page couldn't be rendered, some data 
   *          couldn't be stored to a database, something like that. 
   *        
   * Warning: Means that something unexpected happened, but that execution can
   *          continue, perhaps in a degraded mode; a configuration file was 
   *          missing but defaults were used, a price was calculated as 
   *          negative, so it was clamped to zero, etc. 
   *          Something is not right, but it hasn't gone properly wrong yet. 
   *          Warnings are often a sign that there will be an error very soon.
   *          
   * Info:    Means that something normal but significant happened; the system
   *          started, the system stopped, the daily inventory update job ran, 
   *          etc. There shouldn't be a continual torrent of these, otherwise 
   *          there's just too much to read.
   *          
   * Debug:   Means that something normal and insignificant happened; a new user
   *          came to the site, a page was rendered, an order was taken, a price
   *          was updated. This is the stuff excluded from info because there 
   *          would be too much of it.
   *          
   * Trace:   Would be for extremely detailed and potentially high volume logs
   *          that you don't typically want enabled even during normal 
   *          development. Examples include dumping a full object hierarchy,
   *          logging some state during every iteration of a large loop, etc.
   *          
   * All:     Equivalent to previous item. Enables all logging.
   */
  
  OFF(0),
  FATAL(1),
  ERROR(2),
  WARNING(3),
  INFO(4),
  DEBUG(5),
  TRACE(6),
  ALL(7);   
  
  private int logLevelValue;
    
  /*
   * Constructor.
   * 
   * @param int Value for this log level
   */
  private LogLevel(int value)
  {
    this.logLevelValue = value;
  }
  
  
  /*
   * Find enum by it's name
   * 
   * @param String  Name for this log level
   */
  public static LogLevel lookup(String name)
  {
    name = name.toUpperCase();
    
    for(LogLevel logLevel : LogLevel.values()) 
    {     
      if(logLevel.name().equalsIgnoreCase(name)) 
      {
        return logLevel;
      }
    }
    return null;
  }
  
  
  /*
   * Get the index for this log level
   * 
   * @return  int LogLevel value
   */
  public int getValue()
  {
    return this.logLevelValue;
  }
  
  
  /*
   * Get the name of this log level
   * 
   * @return  String LogLevel name
   */
  @Override
  public String toString() 
  {
    return this.name();
  }

}
