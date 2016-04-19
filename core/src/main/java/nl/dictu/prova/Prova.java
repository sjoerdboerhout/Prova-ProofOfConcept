package nl.dictu.prova;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestSuite;
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
  final static Logger LOGGER = LogManager.getLogger();

  private Thread                      thread;

  private InputPlugin                 inputPlugin;
  private OutputPlugin                shellOutputPlugin;
  private OutputPlugin                webOutputPlugin;
  private ArrayList<ReportingPlugin>  reportPlugins;
  
  private TestSuite                   rootTestSuite;
  private Properties                  properties = new Properties();
  
  
  /**
   * Set up the Prova runner. 
   * The argument 'project' indicates which project Prova starts.
   * 
   * @param project
   */
  public void setUp(String project, Properties provaProperties)
  {
    try
    {          
      if(project.trim().length() < 1)
      {
        throw new Exception("Invalid project name supplied! (" + project + ")");
      }
      
      thread = new Thread(this, project);
      
      // Add all System properties
      properties.putAll(System.getProperties());
      
      // Add all properties read from the config files
      properties.putAll(provaProperties);
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
    LOGGER.trace("Starting Prova execution");
    this.thread.start();
  }

  /**
   * Initiate stop of Prova execution
   */
  public void stop()
  {
    LOGGER.trace("Interrupting Prova execution");
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
      LOGGER.debug("Starting Prova execution");
      
      LOGGER.trace("Starting Prova state: init");
      init();
      
      LOGGER.trace("Starting Prova state: setup");
      setup();
      
      LOGGER.trace("Starting Prova state: execute");
      execute();
      
      LOGGER.trace("Starting Prova state: teardown");
      tearDown();
    }
    catch(Exception ex)
    {
      LOGGER.fatal(ex);
    }
    
    try
    {
      LOGGER.trace("Starting Prova state: shutdown");
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
   * Add a set of properties to the current collection
   * 
   * @param properties
   */
  public void addProperties(Properties properties)
  {
    LOGGER.debug("Adding new properties: " + properties.size());
    
    this.properties.putAll(properties);
  }
  
   
}
