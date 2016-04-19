package nl.dictu.prova;

/**
 * This class defines all Prova's properties names, type and purpose
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-19
 */
public class Config
{
  //                         Name in Prova          Name in property file         Type          Used for:
  public final static String PROVA_CONF_DIR         = "prova.conf.dir";           // String,    Relative dir with config files
  public final static String PROVA_CONF_FILE_DEF    = "prova.conf.file.default";  // String,    Default property file for Prova 
  public final static String PROVA_CONF_FILE_TEST   = "prova.conf.file.test";     // String,    Default property file for Prova 
  public final static String PROVA_CONF_FILE_USER   = "prova.conf.file.user";     // String,    Default property file for Prova
  public final static String PROVA_CONF_FILE_EXT    = "prova.conf.file.ext";      // String,    File extension for property files
  
  public final static String PROVA_PROJECT          = "prova.project";            // String,    Name of the project Prova is testing
  public final static String PROVA_ROOTPATH         = "prova.rootpath";           // String,    Root directory of Prova.
  public final static String PROVA_ENVIRONMENT      = "prova.environment";        // String,    Defines the environment (OTAP) to use
  public final static String PROVA_TIMEOUT          = "prova.timeout";            // Long,      0-180000 Ms default timeout on actions
  
  public final static String PROVA_INPUT            = "prova.in";                 // String,    Active input plug-in 
  public final static String PROVA_OUTPUT_WEB       = "prova.out.web";            // String,    Active output plug-in for web 
  public final static String PROVA_OUTPUT_SHELL     = "prova.out.shell";          // String,    Active output plug-in for shell 
  public final static String PROVA_REPORTING        = "prova.report";             // Boolean,   Execute or only validate 

  public final static String PROVA_CLI_CONFIG       = "prova.cli.config";         // String,    Full path to a properties file 
  public final static String PROVA_CLI_PLUGINS      = "prova.cli.plugins";        // String[],  Comma separated list of plug-ins 
  
  public final static String PROVA_LOG_LEVEL        = "prova.log.level";          // String,    Log level for Prova 
  public final static String PROVA_LOG_DIR_ROOT     = "prova.log.dir.root";       // String,    Log level for Prova 
  public final static String PROVA_LOG_DIR_HIST     = "prova.log.dir.history";    // String,    Log level for Prova 
  public final static String PROVA_LOG_EXT_TXT      = "prova.log.ext.txt";        // String,    Log level for Prova 
  public final static String PROVA_LOG_PATTERN_CONS = "prova.log.pattern.console";// String,    Log level for Prova 
  public final static String PROVA_LOG_PATTERN_FILE = "prova.log.pattern.file";   // String,    Log level for Prova 
  
  public final static String PROVA_RESULTS_FILE     = "prova.results.file";       // String,    Full path to file for logging test results 
  
  public final static String PROVA_TESTS_EXECUTE    = "prova.tests.execute";      // Boolean,   Execute or only validate 
  public final static String PROVA_TESTS_FILTERS    = "prova.tests.filters";      // String[],  Comma separated filtering for test scripts
  public final static String PROVA_TESTS_ROOT       = "prova.tests.root";         // String,    Points to the root of the test scripts
  public final static String PROVA_TESTS_START      = "prova.tests.start";        // Integer,   (Re)Start the first test on this line
}
