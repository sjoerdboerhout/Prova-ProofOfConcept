/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.runners.cli;

import java.io.IOException;
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
 * PROVA-18 - Command Line Interface
 * 
 * Run Prova from the command line. 
 * - Parse command line arguments
 * - Set log options
 * - Create a Prova instance
 * - Configure the Prova instance
 *  - Via properties file(s)
 *  - Via command line arguments
 * - Execute Prova
 * - Wait until Prova finished
 * 
 * Extends the ProvaRunner class with some common functions
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
   * Create, initialize and start a Cli instance.
   * Command line arguments overrule all other properties
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    int statusCode = 0;
    
    try
    {
      LOGGER.trace("Start of program execution");
      
      System.out.println("Hello world, I am Prova the testing framework!");
      
      Cli cli = new Cli();

      // Init sequence and handle command line options
      cli.init(args);
      
      // Start Prova execution and wait until it is finished
      cli.run();
    }
    catch(Exception eX)
    {
      LOGGER.fatal("A fatal exception occured. Program execution is aborted.", eX);
      statusCode = -1;
    }
    finally
    {
      System.out.println("Bye bye cruel world...");
    }
    
    System.exit(statusCode);
  }  
  
  
  /**
   * Force the use of the local main function with a private constructor
   * 
   * @throws Exception
   */
  private Cli() throws Exception
  {
    // Call the parent constructor
    super();
    
    LOGGER.trace("Init local variables");
     
    // Create new instances for all command line handling tools
    cmdLineParser = new DefaultParser();
    options       = new Options();
    cmdLine       = null;
    cliProperties = new Properties();
    cliArguments  = new ArrayList<String>();
  }
  

  /**
   * Create and configure an instance of Prova
   * - Parse options and arguments
   * - Detect Prova root path
   * - Handle command line arguments
   * - Configure Prova
   *   - Hardcoded defaults from resource file 
   *   - User defaults file (optional)
   *   - User defaults test file (optional)
   *   - Project file (optional)
   *   - Project test file (optional)
   *   - User provided file (optional)
   *   - Provided CLI arguments (optional)
   *   
   * @param args
   * @throws Exception
   */
  private void init(String[] args) throws Exception
  {
    try
    {
      LOGGER.trace("Start init with supplied arguments: '{}'", () -> args);
      
      // Read and save command line options and arguments
      cliProperties = parseCliOptions(args, options);
      cliArguments  = parseCliArguments(args, options);
      
      // Update log level immediately if it was supplied as a cli option
      LOGGER.trace("Check if a log level was supplied on the cli: '{}'", () -> cliProperties.containsKey(Config.PROVA_LOG_LEVEL));
      if(cliProperties.containsKey(Config.PROVA_LOG_LEVEL))
      {
        setDebugLevel(this.getClass().getName(), cliProperties.getProperty(Config.PROVA_LOG_LEVEL));
      }
      
      // Check if a project name is provided
      LOGGER.trace("Check if a project name is provided: '{}'", () -> (cliArguments.size() > 0));
      if(cliArguments.size() > 0)
      {
        LOGGER.debug("Provided project name: '{}'", () -> cliArguments.get(0));
        provaProperties.setProperty(Config.PROVA_PROJECT, cliArguments.get(0));
      }
      
      // Detect the Prova rootdir and load default and project property files
      LOGGER.trace("Init the Prova runner");
      super.init();
      
      // If supplied via cli, then read the custom property file
      LOGGER.trace("Check if a property file is provided on cli: '{}'", 
                    () -> cliProperties.containsKey(Config.PROVA_CONF_FILE_USER));
      if(cliProperties.containsKey(Config.PROVA_CONF_FILE_USER))
      {
        LOGGER.debug("Load user defined property file '{}'", () -> cliProperties.getProperty(Config.PROVA_CONF_FILE_USER));
        provaProperties.putAll(loadPropertiesFromFile(cliProperties.getProperty(Config.PROVA_CONF_FILE_USER)));
      }

      // And last but not least apply the cli properties that rule them all
      provaProperties.putAll(cliProperties);
       
      // Update logging settings
      LOGGER.trace("Update logging properties after loading all properties");
      setDebugLevel(ProvaRunner.class.getName(), provaProperties.getProperty(Config.PROVA_LOG_LEVEL));
      setLogPatternConsole(provaProperties.getProperty(Config.PROVA_LOG_PATTERN_CONS));
      setLogPatternFile(provaProperties.getProperty(Config.PROVA_LOG_PATTERN_FILE));
      
      // SetUp Prova with all the collected properties
      LOGGER.trace("Start Prova setup");
      prova.setUp(provaProperties.getProperty(Config.PROVA_PROJECT),provaProperties);
    }
    catch(Exception eX)
    {
      LOGGER.trace("Exception: '{}'", eX.getMessage());
      throw eX;
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
      LOGGER.trace("Configure all CLI options");
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
      
      LOGGER.trace("Parse CLI options");
      cmdLine = cmdLineParser.parse(options, args);      
             
      LOGGER.trace("Process parsed CLI options");
      if(cmdLine.hasOption("help"))       this.printHelp(this.options);
      if(cmdLine.hasOption("version"))    this.printVersion();
      
      if(cmdLine.hasOption("config"))     properties.setProperty(Config.PROVA_CONF_FILE_USER,         cmdLine.getOptionValue("config"));
      if(cmdLine.hasOption("env"))        properties.setProperty(Config.PROVA_ENV,                    cmdLine.getOptionValue("env"));
      if(cmdLine.hasOption("filters"))    properties.setProperty(Config.PROVA_TESTS_FILTERS,          cmdLine.getOptionValue("filters"));
      if(cmdLine.hasOption("loglevel"))   properties.setProperty(Config.PROVA_LOG_LEVEL,              cmdLine.getOptionValue("loglevel"));
      if(cmdLine.hasOption("out"))        properties.setProperty(Config.PROVA_PLUGINS_REPORTING_FILE, cmdLine.getOptionValue("out"));
      if(cmdLine.hasOption("plugins"))    properties.setProperty(Config.PROVA_CLI_PLUGINS,            cmdLine.getOptionValue("plugins"));
      if(cmdLine.hasOption("root"))       properties.setProperty(Config.PROVA_TESTS_ROOT,             cmdLine.getOptionValue("root"));
      if(cmdLine.hasOption("start"))      properties.setProperty(Config.PROVA_TESTS_START,            cmdLine.getOptionValue("start"));
      if(cmdLine.hasOption("timeout"))    properties.setProperty(Config.PROVA_TIMEOUT,                cmdLine.getOptionValue("timeout"));
      if(cmdLine.hasOption("uitvoeren"))  properties.setProperty(Config.PROVA_TESTS_EXECUTE,          cmdLine.getOptionValue("uitvoeren"));

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
      LOGGER.trace("Exception: '{}'", eX.getMessage());
      throw eX;
    }

    return properties;
  }

  
  /**
   * Parse the cli arguments and return the collected values in a string array
   * 
   * @param args
   * @param options
   * @return
   * @throws Exception
   */
  private ArrayList<String> parseCliArguments(String[] args, Options options) throws Exception
  {
    ArrayList<String> cliArguments = new ArrayList<String>();
    
    try
    {
      LOGGER.trace("Parse all CLI arguments");
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
      LOGGER.trace("Exception: '{}'", eX.getMessage());
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
   * @throws Exception
   */
  private void configureCliOption(String shortOpt,
                                  String longOpt,
                                  Boolean hasArgs,
                                  String argName,
                                  char valueSeparator,
                                  String description) throws Exception
  {
    try
    {

      LOGGER.trace("Add new cli option '{}'::'{}'", shortOpt, longOpt);
      
      Option option = Option.builder(shortOpt)
                            .longOpt(longOpt)
                            .required(false)
                            .hasArg(hasArgs)
                            .argName(argName)
                            .valueSeparator(valueSeparator)
                            .desc(description)
                            .build();
      
      options.addOption(option);
    }
    catch(Exception eX)
    {
      LOGGER.trace("Exception: '{}'", eX.getMessage());
      throw eX;
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
      LOGGER.trace("Print Help information");
      
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(100);
      formatter.printHelp("Accepted command line arguments for PROVA", options);
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception: '{}'", eX);
    }
    finally
    {
      System.exit(0);
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
      LOGGER.trace("Print Version information");
      
      System.out.println("TODO: display version information for Prova");
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception: '{}'", eX);
    }
    finally
    {
      System.exit(0);
    }
  }
}
