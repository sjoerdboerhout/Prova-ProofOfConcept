package nl.dictu.prova.runners.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Prova;
import nl.dictu.prova.runners.ProvaRunner;

/**
 * Run Prova from the command line. 
 * - Handle command line arguments
 * - Create a prova instance
 * - Configure Prova
 *  - Via properties file(s)
 *  - Via command line arguments
 * - Start Prova
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-15
 */
public class Cli extends ProvaRunner
{
  private final static Logger LOGGER = LogManager.getLogger();
  
  /**
   * Create, configure, start and monitor an instance of Prova
   * Read command line input and pass input to Prova
   * 
   * @param args
   */
  public static void main( String[] args )
  {
    try
    {
      // TODO Set 'ProjectName' from command line argument
      Prova prova = new Prova("ProjectName");
      
      // TODO Set Prova configuration  
      
      // TODO Set debug level from properties or CLI argument
      prova.setDebugLevel(Cli.class.getName(), "debug");
      
      // Start Prova execution (in it's own thread)
      prova.start(); 
      
      // Wait until Prova thread finished executing
      prova.join();
    }
    catch(Exception ex)
    {
      LOGGER.error(ex);
    }
    finally
    {
      System.out.println("Bye bye cruel world...");
    }
  }  
}
