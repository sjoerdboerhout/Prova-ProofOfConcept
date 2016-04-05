package nl.dictu.prova.logging;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Issue ID: PROVA-13
 * 
 * Test the different log levels and their order 
 *
 * @author Sjoerd Boerhout
 * @since 0.0.1
 */
public class LogLevelTest
{  
  /*
  OFF(0),
  FATAL(1),
  ERROR(2),
  WARNING(3),
  INFO(4),
  DEBUG(5),
  TRACE(6),
  ALL(7);
  */
  
  @Test
  public void checkNumberOfEnums()
  {
    assertTrue(LogLevel.values().length == 8);
  }
  
  @Test
  public void checkValuesOfEnums()
  {
    assertTrue(LogLevel.OFF.getValue()      == 0);
    assertTrue(LogLevel.FATAL.getValue()    == 1);
    assertTrue(LogLevel.ERROR.getValue()    == 2);
    assertTrue(LogLevel.WARNING.getValue()  == 3);
    assertTrue(LogLevel.INFO.getValue()     == 4);
    assertTrue(LogLevel.DEBUG.getValue()    == 5);
    assertTrue(LogLevel.TRACE.getValue()    == 6);
    assertTrue(LogLevel.ALL.getValue()      == 7);
  }
  
  @Test
  public void checkOff()
  {    
    assertTrue( LogLevel.lookup("off").name().equals("OFF") );
    assertTrue( LogLevel.lookup("OFF").name().equals("OFF") );
    assertTrue( LogLevel.lookup("OFF").toString().equals("OFF") );
  }

  @Test
  public void checkFatal()
  {
    assertTrue( LogLevel.lookup("fatal").name().equals("FATAL") );
    assertTrue( LogLevel.lookup("FATAL").name().equals("FATAL") );
    assertTrue( LogLevel.lookup("FATAL").toString().equals("FATAL") );
  }

  @Test
  public void checkError()
  {    
    assertTrue( LogLevel.lookup("error").name().equals("ERROR") );
    assertTrue( LogLevel.lookup("ERROR").name().equals("ERROR") );
    assertTrue( LogLevel.lookup("ERROR").toString().equals("ERROR") );
  }

  @Test
  public void checkWarning()
  {    
    assertTrue( LogLevel.lookup("warning").name().equals("WARNING") );
    assertTrue( LogLevel.lookup("WARNING").name().equals("WARNING") );
    assertTrue( LogLevel.lookup("WARNING").toString().equals("WARNING") );
  }

  @Test
  public void checkInfo()
  {    
    assertTrue( LogLevel.lookup("info").name().equals("INFO") );
    assertTrue( LogLevel.lookup("INFO").name().equals("INFO") );
    assertTrue( LogLevel.lookup("INFO").toString().equals("INFO") );
  }

  @Test
  public void checkDebug()
  {    
    assertTrue( LogLevel.lookup("debug").name().equals("DEBUG") );
    assertTrue( LogLevel.lookup("DEBUG").name().equals("DEBUG") );
    assertTrue( LogLevel.lookup("DEBUG").toString().equals("DEBUG") );
  }

  @Test
  public void checkTrace()
  {    
    assertTrue( LogLevel.lookup("trace").name().equals("TRACE") );
    assertTrue( LogLevel.lookup("TRACE").name().equals("TRACE") );
    assertTrue( LogLevel.lookup("TRACE").toString().equals("TRACE") );
  }

  @Test
  public void checkAll()
  {    
    assertTrue( LogLevel.lookup("all").name().equals("ALL") );
    assertTrue( LogLevel.lookup("ALL").name().equals("ALL") );
    assertTrue( LogLevel.lookup("ALL").toString().equals("ALL") );
  }
}