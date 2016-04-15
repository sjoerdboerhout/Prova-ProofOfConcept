package nl.dictu.prova;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import nl.dictu.prova.logging.LogLevel;

/*
 * Configure Junit
 */
public class Junit
{
  public static void configure()
  {
    configureLog4j();
  }
  
  private static void configureLog4j()
  {
    System.setProperty("prova.log.level", LogLevel.DEBUG.name());
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    ctx.reconfigure();
  }
}
