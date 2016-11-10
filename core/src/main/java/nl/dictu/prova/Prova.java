package nl.dictu.prova;

import java.lang.Thread.State;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.framework.exceptions.SetUpActionException;
import nl.dictu.prova.framework.exceptions.TearDownActionException;
import nl.dictu.prova.framework.exceptions.TestActionException;
import nl.dictu.prova.plugins.input.InputPlugin;
import nl.dictu.prova.plugins.output.DbOutputPlugin;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import nl.dictu.prova.plugins.output.WebOutputPlugin;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;
import nl.dictu.prova.util.PluginLoader;
import nl.dictu.prova.plugins.output.SoapOutputPlugin;

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
  private PluginLoader                pluginLoader;
  
  private InputPlugin                 inputPlugin;
  private ShellOutputPlugin           shellOutputPlugin;
  private WebOutputPlugin             webOutputPlugin;
  private SoapOutputPlugin            soapOutputPlugin;
  private DbOutputPlugin              dbOutputPlugin;
  private ArrayList<ReportingPlugin>  reportPlugins = new ArrayList<ReportingPlugin>();
  
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
      LOGGER.info("Setting up Prova. Project name '{}'", project);
      
      if(project == null ||
         project.trim().length() < 1)
      {
        project = "Prova";
      }
      
      thread = new Thread(this, project);
      pluginLoader = new PluginLoader();
      
      // Add all properties read from the config files
      LOGGER.info("Setting up Prova with {} properties", () -> provaProperties.size());
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
      LOGGER.trace("Join requested");
      
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
    String pluginName = "";
    
    try
    {
      LOGGER.info("Initializing Prova");
      
      if(LOGGER.isTraceEnabled())         
      {
        LOGGER.trace("Provided set properties: ");
        for(String key : properties.stringPropertyNames())
        {
          LOGGER.trace("> " + key + " => " + properties.getProperty(key));
        }  
      }

      LOGGER.debug("Load all plug-in files in dir: '{}'", properties.getProperty(Config.PROVA_PLUGINS_DIR));
      pluginLoader.addFiles(properties.getProperty(Config.PROVA_DIR) +
                            properties.getProperty(Config.PROVA_OS_FILE_SEPARATOR) +
                            properties.getProperty(Config.PROVA_PLUGINS_DIR), 
                            properties.getProperty(Config.PROVA_PLUGINS_EXT));
     
      if(LOGGER.isTraceEnabled())
      {
        Enumeration<URL> roots = pluginLoader.getResources("");
        LOGGER.trace("Loaded directories in classloader: ");
        while(roots.hasMoreElements())
        {
          URL root = roots.nextElement();
          LOGGER.trace("> " + root.getFile());
        }
      }
      
      LOGGER.debug("Load and initialize input plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_INPUT));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_INPUT_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_INPUT).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_INPUT);
      
      inputPlugin = pluginLoader.getInstanceOf(pluginName, InputPlugin.class);
         
      if(inputPlugin != null)
        inputPlugin.init(this);
      else
        throw new Exception("Could not load input plugin '" + pluginName + "'");
      
      
      // TODO: Load and initialize output shell plug-in
      LOGGER.debug("Load and initialize web output plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_WEB));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_WEB_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_WEB).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_WEB);

      webOutputPlugin = pluginLoader.getInstanceOf(pluginName, WebOutputPlugin.class);
      
      if(webOutputPlugin != null)
        webOutputPlugin.init(this);
      else
        throw new Exception("Could not load web output plugin '" + pluginName + "'");
      
      LOGGER.debug("Load and initialize SOAP webservice output plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SOAP));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SOAP_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SOAP).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SOAP);

      soapOutputPlugin = pluginLoader.getInstanceOf(pluginName, SoapOutputPlugin.class);
      
      if(soapOutputPlugin != null)
        soapOutputPlugin.init(this);
      else
        throw new Exception("Could not load webservice output plugin '" + pluginName + "'");
      
      LOGGER.debug("Load and initialize DB output plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_DB));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_DB_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_DB).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_DB);

      dbOutputPlugin = pluginLoader.getInstanceOf(pluginName, DbOutputPlugin.class);
      
      if(dbOutputPlugin != null)
        dbOutputPlugin.init(this);
      else
        throw new Exception("Could not load db output plugin '" + pluginName + "'");
      
      LOGGER.debug("Load and initialize SHELL output plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SHELL));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SHELL_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SHELL).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_OUTPUT_SHELL);

      shellOutputPlugin = pluginLoader.getInstanceOf(pluginName, ShellOutputPlugin.class);
      
      if(shellOutputPlugin != null)
        shellOutputPlugin.init(this);
      else
        throw new Exception("Could not load db output plugin '" + pluginName + "'");

      // Load and initialize report plug-in(s)
      LOGGER.debug("Load and initialize reporting plug-in '{}'", () -> properties.getProperty(Config.PROVA_PLUGINS_REPORTING));
      pluginName = properties.getProperty(Config.PROVA_PLUGINS_REPORTING_PACKAGE) +
                   properties.getProperty(Config.PROVA_PLUGINS_REPORTING).toLowerCase() + "." +
                   properties.getProperty(Config.PROVA_PLUGINS_REPORTING);

      reportPlugins.add(pluginLoader.getInstanceOf(pluginName, ReportingPlugin.class));
      
      for(ReportingPlugin reportPlugin : getReportingPlugins())
      {
        reportPlugin.init(this);
      }
    }
    catch(ClassNotFoundException eX)
    {
      throw new Exception("Plugin '" + pluginName + "' not found!");
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
      
      // Set the root location of the test scripts.
      inputPlugin.setTestRoot(properties.getProperty(Config.PROVA_TESTS_ROOT),
                              properties.getProperty(Config.PROVA_PROJECT));
       
      // Set filters for test case labels
      inputPlugin.setLabels(properties.getProperty(Config.PROVA_TESTS_FILTERS).split(","));
      
      // Set filters for test case labels
      inputPlugin.setTestCaseFilter(properties.getProperty(Config.PROVA_TESTS_FILTERS).split(","));
      
      // Search for test scripts and read the headers
      inputPlugin.setUp();
      

      LOGGER.debug("Number of report plugins to setUp: " + getReportingPlugins().size());
      for(ReportingPlugin reportPlugin : getReportingPlugins())
      {
      	reportPlugin.setUp(this.getPropertyValue(Config.PROVA_PROJECT));
      }
      
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
   * Returns a boolean on whether the input contains any keywords.
   */
  public Boolean containsKeywords(String entry) throws Exception
  {
    Pattern pattern = Pattern.compile("\\{[A-Za-z0-9._]+\\}");
    Matcher matcher = pattern.matcher(entry);

    while (matcher.find())
    {
      return true;
    }
    return false;
  }
  
  
    
  /**
   *  Execute all prepared tests
   */
  private void execute() throws Exception
  {
    try
    {
      LOGGER.info("Start executing test scripts");
      
      // Start test execution
      if(rootTestSuite != null)
      {
        executeTestSuite(rootTestSuite); 
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
   * Execute the given test suite recursively
   * 
   * @param testSuite
   */
  private void executeTestSuite(TestSuite testSuite)
  {
    try
    {
      LOGGER.info("Execute TS: '{}' ({})", () -> testSuite.getId(), () -> testSuite.numberOfTestCases(true));
      
      for(ReportingPlugin reportPlugin : getReportingPlugins())
      {
      	LOGGER.debug("Number of report plugins: " + getReportingPlugins().size());
      	LOGGER.debug("Report: start testsuite");
      	reportPlugin.logStartTestSuite(testSuite);
      }
      // First execute all test cases
      for(Map.Entry<String, TestCase> entry : testSuite.getTestCases().entrySet())
      {
        // Split logging of each test case with a line
        LOGGER.debug("--------------------------------------------------------------------------------");
        
        try
        {
          //LOGGER.debug("Start with TC: '{}'", () -> entry.getValue().getId());
          
          // Load all details of the test script
          inputPlugin.loadTestCase(entry.getValue());
                    
          if(Boolean.parseBoolean(this.getPropertyValue(Config.PROVA_TESTS_EXECUTE)))
          {
            for(ReportingPlugin reportPlugin : getReportingPlugins())
            {
            	LOGGER.debug("Aantal in lijst: "+getReportingPlugins().size());
            	LOGGER.debug("Report: start testcase");
            	reportPlugin.logStartTest(entry.getValue());
            }
            
            //(re-)set up output plug-in(s) for a new test case
            if(webOutputPlugin != null)
              webOutputPlugin.setUp(entry.getValue());
            
            if(soapOutputPlugin != null)
              soapOutputPlugin.setUp(entry.getValue());
            
            if(shellOutputPlugin != null)
              shellOutputPlugin.setUp(entry.getValue());
            
            // Execute the test script
            entry.getValue().execute();
            
            for(ReportingPlugin reportPlugin : getReportingPlugins())
            {
            	LOGGER.debug("Report: end testcase");
            	reportPlugin.logEndTest(entry.getValue());
            }
          }
          else
          {
            LOGGER.info("Skip execution of the test script as requested by the user. ({})", entry.getValue().getId());
          }
        }
        catch(SetUpActionException eX)
        {
          LOGGER.error(">> Setup action failure <<", eX);
        }
        catch(TestActionException eX)
        {
          LOGGER.error(">> Test action failure <<", eX);
          for(ReportingPlugin reportPlugin : getReportingPlugins())
          {
          	LOGGER.debug("Report: end testcase (error)");
          	reportPlugin.logEndTest(entry.getValue());
          }
        }
        catch(TearDownActionException eX)
        {
          LOGGER.error(">> Teardown action failure <<", eX);
        }
        catch(Exception eX)
        {
          LOGGER.error(">> Unhandled Exception: ", eX);
        }
        finally
        {
          // Tear down output plug-in(s) after the test case
          if(webOutputPlugin != null)
            //webOutputPlugin.tearDown(entry.getValue());
        	  webOutputPlugin.shutDown();
          
          if(soapOutputPlugin != null)
            //soapOutputPlugin.tearDown(entry.getValue());
          
          if(shellOutputPlugin != null)
            //shellOutputPlugin.tearDown(entry.getValue());
          
          // Test script finished. Clear all actions to clear memory
          entry.getValue().clearAllActions();
        }
      }
      for(ReportingPlugin reportPlugin : getReportingPlugins())
      {
      	LOGGER.debug("Aantal in lijst: "+getReportingPlugins().size());
      	LOGGER.debug("Report: start testsuite");
      	reportPlugin.logEndTestSuite(testSuite);
      }
      // Second, execute all sub test suites
      for(Map.Entry<String, TestSuite> entry : testSuite.getTestSuites().entrySet())
      {
        try
        {
          executeTestSuite(entry.getValue());
        }
        catch(Exception eX)
        {
          LOGGER.error(eX);
        }
      }
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      eX.printStackTrace();
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
      for(ReportingPlugin reportPlugin : getReportingPlugins())
      {
      	LOGGER.debug("Report: shutdown");
      	reportPlugin.shutDown();
      }
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
      
      pluginLoader.close();
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
  public DbOutputPlugin getDbActionPlugin()
  {
    LOGGER.trace("Request for db action plugin. ({})", () -> this.dbOutputPlugin.getName() );
    
    return this.dbOutputPlugin;
  }

  /**
   * Get a reference to the active web action plug-in
   * 
   * @return
   */
  @Override
  public WebOutputPlugin getWebActionPlugin()
  {
    LOGGER.trace("Request for web action plugin. ({})", () -> this.webOutputPlugin.getName() );
    
    return this.webOutputPlugin;
  }
  
  /**
   * Get a reference to the active soap action plug-in
   * 
   * @return
   */
  @Override
  public SoapOutputPlugin getSoapActionPlugin() 
  {
	LOGGER.trace("Request for webservice action plugin. ({})", () -> this.soapOutputPlugin.getName() );
	    
    return this.soapOutputPlugin;
  }

  /**
   * Get a reference to the active shell action plug-in
   * 
   * @return
   */
  @Override
  public ShellOutputPlugin getShellActionPlugin()
  {
    LOGGER.trace("Request for shell action plugin. ({})", () -> this.shellOutputPlugin.getName() );
    
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
  
  /**
   * Check if a property with the value <key> exists in property collection
   * 
   * @param key
   * @return
   */
  @Override
  public Boolean hasPropertyValue(String key)
  {
    LOGGER.trace("Has property: '{}': ({})", 
                  () -> key, 
                  () -> properties.containsKey(key) ? properties.getProperty(key) : "No");
    
    return properties.containsKey(key);
  }
  
  /**
   * Get the value of the property with key <key>
   * 
   * @param key
   * @return
   * @throws Exception
   */
  @Override
  public String getPropertyValue(String key) throws Exception
  {
    LOGGER.trace("Get value of property: '{}' ({})", 
                  () -> key, 
                  () -> properties.containsKey(key) ? properties.getProperty(key) : "Not found");
    
    if(!properties.containsKey(key))
      throw new Exception("No property with value '" + key + "' found!");
      
    return properties.getProperty(key);
  }
  
  /**
   * Set the value of the property with key <key> to <value
   * 
   * @param key
   * @param value
   * @throws Exception
   */
  @Override
  public void setPropertyValue(String key, String value) throws Exception
  {
    LOGGER.trace("Set value of property with key '{}' to '{}'", () -> key, () -> value);
    
    properties.setProperty(key, value);
  }
  
  
  public String replaceKeywords(String entry) throws Exception
  {
    Pattern pattern = Pattern.compile("\\{[A-Za-z0-9._]+\\}");
    Matcher matcher = pattern.matcher(entry);
    StringBuffer entryBuffer = new StringBuffer("");

    while (matcher.find())
    {
      String keyword = matcher.group(0).substring(1, matcher.group(0).length() - 1);
      
      if (keyword.equalsIgnoreCase("SKIPCELL"))
      {
        LOGGER.debug("Skipping cell with keyword " + keyword);
        return "";
      }

      LOGGER.trace("Found keyword " + matcher.group(0) + " in supplied string.");
      if (!this.hasPropertyValue(keyword))
      {
        LOGGER.trace("No value found for property " + keyword + ", assuming it will be available at execute time.");
        continue;
      }
      if (this.getPropertyValue(keyword).equalsIgnoreCase("{SKIPCELL}"))
      {
        LOGGER.debug("Skipping cell with keyword '{" + keyword + "}'");
        return "";
      }
      matcher.appendReplacement(entryBuffer, this.getPropertyValue(keyword));
    }
    matcher.appendTail(entryBuffer);

    return entryBuffer.toString();
  } 

  
  /**
   * Print all properties (for debug purpose)
   * 
   */
  public void printAllProperties() throws Exception
  {
    for(String key : this.properties.stringPropertyNames())
    {
      System.out.println(key + " => " + properties.getProperty(key));
    }
  }
   
}
