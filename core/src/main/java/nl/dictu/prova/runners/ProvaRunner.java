package nl.dictu.prova.runners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.Config;
import nl.dictu.prova.logging.LogLevel;
import nl.dictu.prova.Prova;

/**
 * Contains all the common function needed to configure and start Prova.
 * 
 * @author  Sjoerd Boerhout
 * @since   2016-04-16
 */
public abstract class ProvaRunner
{
  protected static LogLevel logLevel;
  protected final static Logger LOGGER = LogManager.getLogger();
  
  protected Prova       prova;
  protected Properties  provaProperties;
  
  /**
   * Constructor.
   */
  protected ProvaRunner()
  {
    logLevel        = LogLevel.DEBUG;
    provaProperties = new Properties();
  }
  
  /**
   *  Setup Prova runner with 
   * - Creates a Prova instance
   * - Initializes the properties
   * - Get Prova root path
   * 
   * @throws Exception
   */
  protected void init() throws Exception
  { 
    prova       = new Prova();
      
    // Load the default Prova settings
    LOGGER.trace("Load the default Prova properties from resource file");
    provaProperties.putAll(loadPropertiesFromResource("/config/prova-defaults.prop"));  
    provaProperties.put(Config.PROVA_DIR, getProvaRootPath());
    
  }
  
  
  /**
   * Update the log level of Prova
   * 
   * @param logger
   * @param name
   * @return
   */
  public String setDebugLevel(String logger, String name)
  {
    try
    {
      String currLogLevel;
      logLevel = LogLevel.lookup(name); 
      
      // Log4j2 configuration uses the systems properties.
      System.setProperty(Config.PROVA_LOG_LEVEL, logLevel.name());
      
      // TODO add support for updating the log level per logger
      
      // Force a reconfiguration of Log4j to activate the settings immediately
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      currLogLevel = ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel().name();
      ctx.reconfigure();
      
      // Check if log level changed
      if(!currLogLevel.equalsIgnoreCase(name))
      {
        if(LOGGER.isInfoEnabled())
          LOGGER.info("Log level changed to: {}", () -> ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
        else
          System.out.println("Log level changed to: " + ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
      }
      
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
  protected void setLogfile(String logFile)
  {     
    try
    {
      // Log4j2 configuration uses the systems properties.
      System.setProperty(Config.PROVA_LOG_FILENAME, logFile);
      
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
  protected void setLogPatternConsole(String newPattern)
  {
    try
    {
      LOGGER.trace("Update log pattern for console to: '{}'", newPattern);
      
      // Log4j2 configuration uses the systems properties.
      System.setProperty(Config.PROVA_LOG_PATTERN_CONS, newPattern);
    
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
  protected void setLogPatternFile(String newPattern)
  {
    try
    {
      LOGGER.trace("Update log pattern for file to: '{}'", newPattern);
      
      // Log4j2 configuration uses the systems properties.
      System.setProperty(Config.PROVA_LOG_PATTERN_FILE, newPattern);
    
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
   * Initialize the Prova runner
   *
   * @throws Exception
   */
  private String getProvaRootPath() throws Exception
  {
    String sRootPath = "";
    File   fRootPath;
    String pathSeparator = System.getProperty("file.separator");
    
    try
    {
      // Get the root path of the Prova installation
      sRootPath = Prova.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      sRootPath = URLDecoder.decode(sRootPath, "utf-8");
      sRootPath = sRootPath.substring(1,sRootPath.lastIndexOf(pathSeparator));
      fRootPath = new File(pathSeparator + pathSeparator + sRootPath)
                          .getParentFile()
                          .getParentFile()
                          .getAbsoluteFile();
      
      LOGGER.info("Root location of Prova: '{}/'", fRootPath.getAbsolutePath());
    }
    catch(Exception eX)
    {
      LOGGER.fatal(eX);
      throw eX;
    }

    return (fRootPath.getAbsolutePath() + "/");
  }
  
  
  /**
   * Search for property files and load all properties
   * Searches for:
   * - prova-defaults.properties
   * - prova-defaults-test.properties
   * - prova-<projectName>.properties
   * - prova-<projectName>-test.properties
   */
  protected Properties loadPropertyFiles() throws Exception
  {
    Properties properties = new Properties();
    
    try
    {
      LOGGER.debug("Load default property files for Prova");
      
      // <rootPath>\config\prova-defaults.properties
      properties.putAll(loadPropertiesFromFile(provaProperties.getProperty(Config.PROVA_DIR) +
                                               provaProperties.getProperty(Config.PROVA_CONF_DIR) +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_DEF) +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT)));
      
      // <rootPath>\config\prova-defaults-test.properties
      properties.putAll(loadPropertiesFromFile(provaProperties.getProperty(Config.PROVA_DIR) + 
                                               provaProperties.getProperty(Config.PROVA_CONF_DIR) +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_TEST) +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT)));
      
      LOGGER.debug("Load project property files for project '{}'", () -> provaProperties.getProperty(Config.PROVA_PROJECT));
      
      // <rootPath>\config\prova-<projectName>.properties
      properties.putAll(loadPropertiesFromFile(provaProperties.getProperty(Config.PROVA_DIR) +
                                               provaProperties.getProperty(Config.PROVA_CONF_DIR) + 
                                               "prova-" + 
                                               provaProperties.getProperty(Config.PROVA_PROJECT).toLowerCase() +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT)));
  
      // <rootPath>\config\prova-<projectName>-test.properties
      properties.putAll(loadPropertiesFromFile(provaProperties.getProperty(Config.PROVA_DIR) +
                                               provaProperties.getProperty(Config.PROVA_CONF_DIR) + 
                                               "prova-" + 
                                               provaProperties.getProperty(Config.PROVA_PROJECT).toLowerCase() +
                                               "-test" +
                                               provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT)));
    }
    catch(Exception eX)
    {
      throw eX;
    }
    
    return properties;
  }
  
  /**
   * Load a set of properties from a resource
   * 
   * @param fileName
   * @return
   * @throws Exception
   */
  protected Properties loadPropertiesFromResource(String fileName) throws Exception
  {
    Properties properties = new Properties();
    InputStream inputStream = null;
    
    try
    {
      inputStream = this.getClass().getResourceAsStream(fileName);
    
      properties.load(inputStream);
    
      LOGGER.debug("Loaded {} properties from resource '{}'", properties.size(), fileName);
      
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
    finally
    {
      if(inputStream != null)
        inputStream.close();
    }
    
    return properties;
  }
  
  /**
   * Load a set of properties from file
   * 
   * @param fileName
   * @return
   * @throws Exception
   */
  protected Properties loadPropertiesFromFile(String fileName) throws Exception
  {
    File  propertyFile = null;
    Properties properties = new Properties();
    
    try
    {
      LOGGER.trace("Loading properties from file: {}", () -> fileName);
      
      propertyFile = new File(fileName);
      
      if(propertyFile.isFile() && propertyFile.canRead())
      {
        properties.load(new FileInputStream(propertyFile));
        
        LOGGER.debug("Loaded {} properties from {}", () -> properties.size(), () -> fileName);
        
        if(LOGGER.isTraceEnabled())
        {
          for(String key : properties.stringPropertyNames())
          {
            LOGGER.trace(key + " => " + properties.getProperty(key));
          }
        }
      }
      else
      {
        LOGGER.warn("Property file '{}' not found.", () -> fileName);
      }
    }
    catch(Exception eX)
    { 
      LOGGER.warn("Failed to load properties from file '{}' ({})", () -> fileName, () -> eX);
    }
    
    return properties;
  }

  /**
   * Save the supplied properties to the given filename. 
   * 
   * @param properties
   * @param fileName
   */
  protected void saveProperties(Properties properties, String fileName)
  { 
    try
    {
      LOGGER.debug("Save {} properties to file {}", () -> properties.size(), () -> fileName);

      properties.store(new FileOutputStream(fileName), "Active configuration saved by Prova");
    }
    catch(Exception eX)
    {
      LOGGER.error(eX);
    }
  }
}
