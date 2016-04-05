package nl.dictu.prova;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.logging.LogLevel;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.output.OutputPlugin;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/*
 * Hello world!
 *
 */
public class Prova implements TestRunner
{
  private static LogLevel logLevel = LogLevel.WARNING;
  final static Logger LOGGER = LogManager.getLogger();

  private InputPlugin                 inputPlugin;
  private OutputPlugin                shellOutputPlugin;
  private OutputPlugin                webOutputPlugin;
  private ArrayList<ReportingPlugin>  reportPlugins;


  private static String ReturnString(String text)
  {
    // For testing the Lambda support of Log4J2
    // This is a very complicated function!
    return text;
  }

  private static String setDebugLevel(String logger, String name)
  {
    try
    {
      logLevel = LogLevel.lookup(name);
      
      System.setProperty("prova.log.level", logLevel.name());
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      ctx.reconfigure();
      
      System.out.println("Config updated. Level: " + ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
      
      return ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel().name();
    }
    catch(Exception e)
    {
      // TODO Implement error handling
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    
    return "";
  }
  
  
  private static void updateLogfile(String logFile)
  {     
    System.setProperty("prova.log.filename", logFile);
      
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      ctx.reconfigure();
    }
    
 
  private static void updateLogPattern(String newPattern)
  {
    System.setProperty("prova.log.pattern.console", newPattern);
    System.setProperty("prova.log.pattern.file", newPattern);
    
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
  }

  /**
   * TODO Add Javadoc
   */
  public Prova(String[] args)
  {
    try
    {
      System.out.println( "Hello World, I'm Prova!" );
          
      setDebugLevel(Prova.class.getName(), args.length > 0 ? args[0] : "error");
      
      Logger LOGGER = LogManager.getLogger();
          
      LOGGER.fatal("Fatale fout - {}", () -> ReturnString("Blaat"));
      LOGGER.error("Niet fatale Error");
      LOGGER.warn("Oeps...");
      LOGGER.info("InfoBericht");
      LOGGER.debug("DebugMessage");
      LOGGER.trace("Trace datadump");
  
      System.out.println("----------");
  
      try 
      {
        Thread.sleep(2000);
      } 
      catch(InterruptedException ex) 
      {
        Thread.currentThread().interrupt();
      }
  
      setDebugLevel(Prova.class.getName(), args.length > 1 ? args[1] : "trace");
      updateLogPattern("%d{yyyy-MM-dd HH:mm:ss:SSS} [%c:%t:%L] %-5p - %msg%n");
      
      LOGGER.fatal("Fatale fout - {}", () -> ReturnString("Schaap"));
      LOGGER.error("Niet fatale Error");
      LOGGER.warn("Oeps...");
      LOGGER.info("InfoBericht");
      LOGGER.debug("DebugMessage");
      LOGGER.trace("Trace datadump");
      
      System.out.println("----------");
      updateLogfile("ProvaX");

      LOGGER.fatal("Fatale fout - {}", () -> ReturnString("Geit"));
      LOGGER.error("Niet fatale Error");
      LOGGER.warn("Oeps...");
      LOGGER.info("InfoBericht");
      LOGGER.debug("DebugMessage");
      LOGGER.trace("Trace datadump");
    }
    catch(Exception e)
    {
      // TODO Implement error handling
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void addTestSuite(TestSuite testSuite)
  {
    // TODO Auto-generated method
  }

  @Override
  public OutputPlugin getWebActionPlugin()
  {
    // TODO Auto-generated method
    return this.webOutputPlugin;
  }

  @Override
  public OutputPlugin getShellActionPlugin()
  {
    // TODO Auto-generated method
    return this.shellOutputPlugin;
  }

  @Override
  public ArrayList<ReportingPlugin> getReportingPlugins()
  {
    // TODO Auto-generated method
    return this.reportPlugins;
  }    
}
