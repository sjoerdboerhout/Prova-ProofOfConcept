package nl.dictu.prova;

import java.lang.Thread.State;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.logging.LogLevel;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.output.OutputPlugin;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/**
 * Core class Prova facilitates the whole process of executing the tests
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-06
 */
public class Prova implements Runnable, TestRunner
{
  private static LogLevel logLevel = Constants.DEFAULT_LOGLEVEL;
  final static Logger LOGGER = LogManager.getLogger();

  private Thread                      thread;

  private InputPlugin                 inputPlugin;
  private OutputPlugin                shellOutputPlugin;
  private OutputPlugin                webOutputPlugin;
  private ArrayList<ReportingPlugin>  reportPlugins;
  
  private TestSuite                   rootTestSuite;
  
  
  /**
   * Constructor of the Prova runner. 
   * The argument 'project' indicates which project Prova should start.
   * Configuration files for this project will be processed before starting 
   * test execution.
   * 
   * @param project
   */
  public Prova(String project)
  {
    try
    {          
      System.out.println("Hello World, I'm Prova and starting project: " + project); 
      
      if(project.trim().length() < 1)
      {
        throw new Exception("Invalid project name supplied! (" + project + ")");
      }
      
      this.thread = new Thread(this, project);
    }
    catch(Exception eX)
    {
      LOGGER.fatal(eX);
    }
  }
  
  /**
   * Start Prova execution
   */
  public void start()
  { 
    LOGGER.debug("Starting Prova execution");
    this.thread.start();
  }

  /**
   * Initiate stop of Prova execution
   */
  public void stop()
  {
    LOGGER.debug("Interrupting Prova execution");
    this.thread.interrupt();
  }

  /**
   * Request a join of the thread 
   */
  public void join()
  {
    try
    {
      LOGGER.trace("join requested");
      
      this.thread.join();
    }
    catch(InterruptedException ex)
    {
      LOGGER.warn(ex);
    }
  }

  /**
   * Check if the Prova thread was interrupted 
   */
  public boolean isInterrupted()
  {
    LOGGER.trace("Thread isInterrupted requested");
    
    return this.thread.isInterrupted();
  }

  /**
   * Request the current state of the thread 
   */
  public State getState()
  {
    LOGGER.trace("Thread state requested");
    
    return this.thread.getState();
  }
  
  
  /**
   *  Called when the Prova thread starts.
   *  Run all steps of the test process in correct order
   */
  @Override 
  public void run()
  {
    try
    {
      LOGGER.debug("Starting Prova thread execution");
      
      init();
      setup();
      execute();
      tearDown();
    }
    catch(Exception ex)
    {
      LOGGER.error(ex);
    }
    
    try
    {
      shutDown();
      LOGGER.debug("Ending Prova thread execution");
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);  
    }
  }
  
  /**
   * Initialize Prova and set everything ready for test execution.
   * - Read projects properties file(s)
   * - Check for a valid input plug-in
   * - Check for a valid output plug-in
   * - Check for (a) valid report plug-in(s)
   */
  private void init() throws Exception
  {
    try
    {
      LOGGER.info("Initializing Prova");
      
      // TODO: Read project configuration
      
      // TODO: Load and initialize input plug-in
      // TODO: Load and initialize output plug-in
      // TODO: Load and initialize report plug-in(s)
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      throw eX;
    }
  }
    
  /**
   *  Index and prepare test scripts for test execution.
   *  - Search for test scripts
   *  - Create structure of test suites and test cases
   *    - Only read the 'headers' and not the full test cases
   *  - Execute the (optional) setup commands to prepare the test environment
   */
  private void setup() throws Exception
  {
    try
    {
      LOGGER.info("Setting up Prova.");
      
      // TODO: Search for test scripts
      // TODO: Build structure of test suites and test cases
      // TODO: Run one time setup script(s)
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      throw eX;
    } 
  }
    
  /**
   *  Execute all prepared tests
   */
  private void execute() throws Exception
  {
    try
    {
      LOGGER.info("Executing Prova");
      
      // Start test execution
      if(rootTestSuite != null)
      {  
        rootTestSuite.execute();
      }
      else
      {
        LOGGER.warn("No tests found to execute!");
      }
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      throw eX;
    }  
  }
    
  /**
   *  TearDown after test execution
   *  - Execute the (optional) tear down commands to reset the test environment
   */
  private void tearDown() throws Exception
  {
    try
    {
      LOGGER.info("Teardown Prova");
      
      // TODO: Run one tear down script(s)
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      throw eX;
    }       
  }
    
  /**
   *  Prepare Prova for exit 
   *  - Provide a test summary
   *  - Shutdown input plug-in
   *  - Shutdown output plug-in
   *  - Shutdown report plug-in(s) 
   */
  private void shutDown() throws Exception
  {
    try
    {
      LOGGER.info("Shutting down Prova");
      
      // TODO: Provide test summary
      
      // TODO: Shutdown input plug-in
      // TODO: Shutdown output plug-in
      // TODO: Shutdown report plug-in(s)
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      throw eX;
    } 
  }
 
  /**
   * Set the root test suite.
   * This test suite should contain the whole structure of test suites and
   * test cases
   * 
   * @param rootTestSuite
   */
  @Override
  public void setRootTestSuite(TestSuite rootTestSuite)
  {
    if(rootTestSuite != null)
    {
      this.rootTestSuite = rootTestSuite;
    }
  }

  
  
  /**
   * Get a reference to the active input plug-in
   * 
   * @return
   */
  @Override
  public InputPlugin getInputPlugin()
  {
    return this.inputPlugin;
  }

  /**
   * Get a reference to the active web action plug-in
   * 
   * @return
   */
  @Override
  public OutputPlugin getWebActionPlugin()
  {
    return this.webOutputPlugin;
  }

  /**
   * Get a reference to the active shell action plug-in
   * 
   * @return
   */
  @Override
  public OutputPlugin getShellActionPlugin()
  {
    return this.shellOutputPlugin;
  }

  /**
   * Get a list of the active report plug-in(s)
   * 
   * @return
   */
  @Override
  public ArrayList<ReportingPlugin> getReportingPlugins()
  {
    // TODO Auto-generated method
    return this.reportPlugins;
  }

  
  
  /**
   * Update the loglevel of Prova
   * 
   * @param logger
   * @param name
   * @return
   */
  public String setDebugLevel(String logger, String name)
  {
    try
    {
      logLevel = LogLevel.lookup(name);
      
      // Log4j2 configuration uses the systems properties.
      System.setProperty("prova.log.level", logLevel.name());
      
      // TODO add support for updating the log level per logger
      
      // Force a reconfiguration of Log4j to activate the settings immediately
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      ctx.reconfigure();
      
      System.out.println("Config updated. Level: " + ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
      
      return ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel().name();
    }
    catch(Exception eX)
    {
      LOGGER.warn(eX);
    }
    
    return logLevel.name();
  }
  
  /**
   * Update the location to save the Prova log files 
   * 
   * @param logFile
   */
  public void updateLogfile(String logFile)
  {     
    try
    {
      // Log4j2 configuration uses the systems properties.
      System.setProperty("prova.log.filename", logFile);
      
      // Force a reconfiguration of Log4j to activate the settings immediately
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      ctx.reconfigure();
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
    }
  }
    
  /**
   * Override the log pattern with a new pattern.
   * The pattern is set for both console and file logging
   * This function assumes a valid Log4j2 is supplied!
   * 
   * @param newPattern
   */
  public void updateLogPattern(String newPattern)
  {
    try
    {
      // Log4j2 configuration uses the systems properties.
      System.setProperty("prova.log.pattern.console", newPattern);
      System.setProperty("prova.log.pattern.file", newPattern);
    
      // Force a reconfiguration of Log4j to activate the settings immediately
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      ctx.reconfigure();
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
    }
  }
}
