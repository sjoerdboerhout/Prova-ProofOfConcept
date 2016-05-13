package nl.dictu.prova.runners.cli;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import nl.dictu.prova.Config;
import nl.dictu.prova.runners.ProvaRunner;

/**
 * Run Prova from the command line. 
 * - Parse command line arguments
 * - Set log options
 * - Create a prova instance
 * - Configure Prova
 *  - Via properties file(s)
 *  - Via command line arguments
 * - Start Prova
 * - Wait until Prova finished
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-15
 */
public class Cli extends ProvaRunner
{
  private CommandLineParser cmdLineParser;
  private CommandLine       cmdLine;
  private Options           options;
  private Properties        cliProperties;
  private ArrayList<String> cliArguments;
  
  /**
   * Create, initialize and start a Cli instance
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    try
    {
      System.out.println("Hello world, I am Prova the testing framework!");
      
      Cli cli = new Cli();
      
      // Initialize
      cli.init(args);
      
      cli.run();
    }
    catch(Exception eX)
    {
      LOGGER.fatal(eX);
      eX.printStackTrace();
    }
    finally
    {
      System.out.println("Bye bye cruel world...");
    }
  }  
  
  /**
   * Force the use of the local main function.
   */
  private Cli() throws Exception
  {
    super();
    
    cmdLineParser = new DefaultParser();
    options       = new Options();
    cmdLine       = null;
    cliProperties = new Properties();
    cliArguments  = new ArrayList<String>();
  }
  

  /**
   * Configure the instance of Prova
   * - Parse options and arguments
   * - Detect Prova root path
   * - Handle command line arguments
   * - Configure Prova
   * 
   * @param args
   */
  private void init(String[] args) throws Exception
  {
    try
    {
      // Read and save command line options and arguments
      cliProperties = parseCliOptions(args, options);
      cliArguments  = parseCliArguments(args, options);
      
      // Update log level immediately if it was supplied as option
      if(cliProperties.containsKey("prova.log.level"))
        setDebugLevel(this.getClass().getName(), cliProperties.getProperty(Config.PROVA_LOG_LEVEL));
      
      // Detect Prova rootdir
      // Load default settings
      super.init();
      
      // Check if the required project name is found
      if(cliArguments.size() > 0)
      {
        LOGGER.debug("Project name: '{}'", () -> cliArguments.get(0));
        provaProperties.setProperty(Config.PROVA_PROJECT, cliArguments.get(0));
      }
      else
      {
        throw new Exception("Required project name not found!");
      }
      
      // Read project property file(s)
      provaProperties.putAll(loadPropertyFiles());
      
      // Read cli property file if set
      if(cliProperties.containsKey(Config.PROVA_CONF_FILE_USER))
      {
        LOGGER.debug("Load user defined property file '{}'", () -> cliProperties.getProperty(Config.PROVA_CONF_FILE_USER));
        provaProperties.putAll(loadPropertiesFromFile(cliProperties.getProperty(Config.PROVA_CONF_FILE_USER)));
      }
      
      // And last but not least apply the cli properties that rule them all
      provaProperties.putAll(cliProperties);
       
      // Update logging properties
      setDebugLevel(ProvaRunner.class.getName(), provaProperties.getProperty(Config.PROVA_LOG_LEVEL));
      setLogPatternConsole(provaProperties.getProperty(Config.PROVA_LOG_PATTERN_CONS));
      setLogPatternFile(provaProperties.getProperty(Config.PROVA_LOG_PATTERN_FILE));
      
      // SetUp Prova
      prova.setUp(provaProperties.getProperty(Config.PROVA_PROJECT),provaProperties);
    }
    catch(Exception eX)
    {
      throw eX;
    }    
  }

  
  /**
   * Start the execution of Prova and wait until it's finished
   */
  private void run()
  {
    try
    {
      LOGGER.trace("Cli: run");
      
      // Start Prova execution (in it's own thread)
      prova.start(); 
      
      // Wait until Prova thread finished executing
      prova.join();
    }
    catch(Exception ex)
    {
      LOGGER.error(ex);
    }     
  }


  /**
   * Parse the cli options and return the collected values as properties
   * 
   * @param args
   * @return
   */
  private Properties parseCliOptions(String[] args, Options options) throws Exception
  {
    Properties properties = new Properties();
    
    try
    {
      configureCliOption("c", "config",   true,  "fileName",     '=', "Set configuration file");
      configureCliOption("e", "env",      true,  "environment",  '=', "Set environment for executing the test scripts");
      configureCliOption("f", "filters",  true,  "filter(s)",    '=', "Only test scripts with these filter(s) are executed");
      configureCliOption("h", "help",     false, "",             ' ', "Show this help message");
      configureCliOption("l", "loglevel", true,  "loglevel",     '=', "Set the loglevel (fatal,error,warn, info,debug,trace");
      configureCliOption("o", "out",      true,  "filename",     '=', "Write test results to this file");
      configureCliOption("p", "plugins",  true,  "plugin(s)",    '=', "Use these plugin(s) for executing the test scripts");
      configureCliOption("r", "root",     true,  "testroot",     '=', "Root location where the test scripts are located");
      configureCliOption("s", "start",    true,  "line",         '=', "(Re)start the first test at this line");
      configureCliOption("t", "timeout",  true,  "milliseconds", '=', "Timeout for test actions before failing");
      configureCliOption("u", "uitvoeren",true,  "ja/nee",       '=', "Testen uitvoeren of alleen valideren?");
      configureCliOption("v", "version",  false, "",             ' ', "Display version information");
      
      cmdLine = cmdLineParser.parse(options, args);      
             
      if(cmdLine.hasOption("help"))       this.printHelp(this.options);
      if(cmdLine.hasOption("version"))    this.printVersion();
      
      if(cmdLine.hasOption("config"))     properties.setProperty(Config.PROVA_CONF_FILE_USER, cmdLine.getOptionValue("config"));
      if(cmdLine.hasOption("env"))        properties.setProperty(Config.PROVA_ENV,            cmdLine.getOptionValue("env"));
      if(cmdLine.hasOption("filters"))    properties.setProperty(Config.PROVA_TESTS_FILTERS,  cmdLine.getOptionValue("filters"));
      if(cmdLine.hasOption("loglevel"))   properties.setProperty(Config.PROVA_LOG_LEVEL,      cmdLine.getOptionValue("loglevel"));
      if(cmdLine.hasOption("out"))        properties.setProperty(Config.PROVA_RESULTS_FILE,   cmdLine.getOptionValue("out"));
      if(cmdLine.hasOption("plugins"))    properties.setProperty(Config.PROVA_CLI_PLUGINS,    cmdLine.getOptionValue("plugins"));
      if(cmdLine.hasOption("root"))       properties.setProperty(Config.PROVA_TESTS_ROOT,     cmdLine.getOptionValue("root"));
      if(cmdLine.hasOption("start"))      properties.setProperty(Config.PROVA_TESTS_START,    cmdLine.getOptionValue("start"));
      if(cmdLine.hasOption("timeout"))    properties.setProperty(Config.PROVA_TIMEOUT,        cmdLine.getOptionValue("timeout"));
      if(cmdLine.hasOption("uitvoeren"))  properties.setProperty(Config.PROVA_TESTS_EXECUTE,  cmdLine.getOptionValue("uitvoeren"));

      LOGGER.debug("Found {} option(s) on the command line", properties.size());
      
      if(LOGGER.isTraceEnabled())         
      {
        for(String key : properties.stringPropertyNames())
        {
          LOGGER.trace(key + " => " + properties.getProperty(key));
        }  
      }
    }
    catch(Exception eX)
    {
      throw eX;
    }

    return properties;
  }

  /**
   * Parse the cli arguments and return the collected values in a string array
   * 
   * @param args
   * @return
   */
  private ArrayList<String> parseCliArguments(String[] args, Options options) throws Exception
  {
    ArrayList<String> cliArguments = new ArrayList<String>();
    
    try
    {
      cmdLine = cmdLineParser.parse(options, args);      
        
      LOGGER.debug("Found {} argument(s) on the command line", cmdLine.getArgs().length);
      
      for(String str : cmdLine.getArgs()) 
      {
        cliArguments.add(str);
        LOGGER.trace("=> " + str);
      }
    }
    catch(Exception eX)
    {
      throw eX;
    }

    return cliArguments;
  }

  /**
   * Configure the command line parser options
   * 
   * @param shortOpt
   * @param longOpt
   * @param hasArgs
   * @param argName
   * @param valueSeparator
   * @param description
   */
  private void configureCliOption(String shortOpt,
                                  String longOpt,
                                  Boolean hasArgs,
                                  String argName,
                                  char valueSeparator,
                                  String description)
  {
    try
    {
      Option option = Option.builder(shortOpt)
                        .longOpt(longOpt)
                        .required(false)
                        .hasArg(hasArgs)
                        .argName(argName)
                        .valueSeparator(valueSeparator)
                        .desc(description)
                        .build();
      
      this.options.addOption(option);
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
    }
  }
  
  
  /**
   * Print help information based on the configured options
   * 
   * @param options
   */
  private void printHelp(Options options)
  {
    try
    {
      LOGGER.trace("Cli::printHelp()");
      
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(100);
      formatter.printHelp("Accepted command line arguments for PROVA", options);
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
      eX.printStackTrace();
    }
  }
  
  /**
   * Print help information based on the configured options
   * 
   * @param options
   */
  private void printVersion()
  {
    try
    {
      System.out.println("TODO: display version information for Prova");
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
    }
  }
}
