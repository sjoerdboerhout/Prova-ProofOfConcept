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
 * Contains all the common function needed to configure and start Prova. This class is extended by a runner.
 * 
 * @author Sjoerd Boerhout
 * @since 2016-04-16
 */
public abstract class ProvaRunner {
    protected final static Logger LOGGER = LogManager.getLogger();

    protected Prova prova;
    protected Properties provaProperties;
    protected String pathSeparator;

    /**
     * Constructor
     */
    protected ProvaRunner() {
        provaProperties = new Properties();
        provaProperties.putAll(System.getProperties());

        pathSeparator = provaProperties.getProperty(Config.PROVA_OS_FILE_SEPARATOR);
    }

    /**
     * Init Prova runner with: - Creates a Prova instance - Get Prova root path - Load the properties from file
     * 
     * @throws Exception
     */
    protected void init() throws Exception {
        LOGGER.trace("Create a new Prova instance");
        prova = new Prova();

        LOGGER.trace("Try to detect the Prova root directory");
        provaProperties.put(Config.PROVA_DIR, getProvaRootPath());

        // Load the default Prova settings from resource file
        LOGGER.trace("Load the hard coded Prova default properties");
        provaProperties.putAll(loadPropertiesFromResource("/config/prova-defaults.prop"));

        // Try to load the default properties file
        LOGGER.trace("Start loading default property files");
        provaProperties.putAll(loadDefaultPropertyFiles());

        // Check if a project name was supplied
        if (provaProperties.containsKey(Config.PROVA_PROJECT)) {
            // Read project property file(s)
            LOGGER.trace("Start loading project property files");
            provaProperties.putAll(loadProjectPropertyFiles());
        }
    }

    /**
     * Update the log level of the current Prova instance
     * 
     * @param logger
     * @param name
     * @return
     */
    public String setDebugLevel(String logger, String name) {
        LogLevel logLevel = null;

        try {
            LOGGER.trace("Try to update loglevel of '{}' to '{}'", logger, name);

            String currLogLevel;
            logLevel = LogLevel.lookup(name);

            if (logLevel == null)
                throw new Exception("Invalid debug level '" + name + "'");

            // Log4j2 configuration uses the systems properties.
            System.setProperty(Config.PROVA_LOG_LEVEL, logLevel.name());

            // TODO add support for updating the log level per logger

            // Force a reconfiguration of Log4j to activate the settings immediately
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            currLogLevel = ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel().name();
            ctx.reconfigure();

            // Check if log level is changed
            if (!currLogLevel.equalsIgnoreCase(name)) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Log level changed to: {}",
                            () -> ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
                else
                    System.out
                            .println("Log level changed to: " + ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel());
            }

            return ctx.getLogger(LogManager.ROOT_LOGGER_NAME).getLevel().name();
        } catch (Exception eX) {
            LOGGER.error(eX);
        }

        return logLevel.name();
    }

    /**
     * Update the location to save the current Prova log file
     * 
     * @param logFile
     */
    protected void setLogfile(String logFile) {
        try {
            LOGGER.trace("Update loglevel file to '{}'", logFile);

            // Log4j2 configuration uses the systems properties.
            System.setProperty(Config.PROVA_LOG_FILENAME, logFile);

            // Force a reconfiguration of Log4j to activate the settings immediately
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();
        } catch (Exception eX) {
            LOGGER.error(eX);
        }
    }

    /**
     * Override the log pattern with a new pattern. The pattern is set for both console and file logging Note: This
     * function assumes that a valid Log4j2 pattern is supplied!
     * 
     * @param newPattern
     */
    protected void setLogPatternConsole(String newPattern) {
        try {
            LOGGER.trace("Update log pattern for console to: '{}'", newPattern);

            // Log4j2 configuration uses the systems properties.
            System.setProperty(Config.PROVA_LOG_PATTERN_CONS, newPattern);

            // Force a reconfiguration of Log4j to activate the settings immediately
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();
        } catch (Exception eX) {
            LOGGER.error(eX);
        }
    }

    /**
     * Override the log pattern with a new pattern. The pattern is set for both console and file logging Note: This
     * function assumes that a valid Log4j2 pattern is supplied!
     * 
     * @param newPattern
     */
    protected void setLogPatternFile(String newPattern) {
        try {
            LOGGER.trace("Update log pattern for file to: '{}'", newPattern);

            // Log4j2 configuration uses the systems properties.
            System.setProperty(Config.PROVA_LOG_PATTERN_FILE, newPattern);

            // Force a reconfiguration of Log4j to activate the settings immediately
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();
        } catch (Exception eX) {
            LOGGER.error(eX);
        }
    }

    /**
     * Retrieve the Prova root path based on the location of the current JAR-file Assuming it is placed in the defined
     * directory structure the root-dir is 2 levels up.
     *
     * @throws Exception
     */
    private String getProvaRootPath() throws Exception {
        String sRootPath = "";
        File fRootPath;

        try {
            LOGGER.trace("Find PROVA rootpath. File separator = '{}'", pathSeparator);

            // Get the root path of the Prova installation
            sRootPath = Prova.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            sRootPath = URLDecoder.decode(sRootPath, "utf-8");
            sRootPath = sRootPath.substring(1, sRootPath.lastIndexOf('/'));
            fRootPath = new File(pathSeparator + pathSeparator + sRootPath).getParentFile().getParentFile()
                    .getAbsoluteFile();

            LOGGER.info("Root location of Prova: '{}'", fRootPath.getAbsolutePath());
        } catch (Exception eX) {
            LOGGER.trace("Exception while retrieving Prova rootpath", eX);
            throw eX;
        }

        return (fRootPath.getAbsolutePath());
    }

    /**
     * Search for the default property files and load all properties Searches for: - prova_defaults.prop -
     * prova_defaults-test.prop
     */
    protected Properties loadDefaultPropertyFiles() {
        Properties properties = new Properties();
        String fileName = "";

        try {
            LOGGER.trace("Load default property files for Prova");

            // <rootPath>\config\prova_defaults.properties
            fileName = provaProperties.getProperty(Config.PROVA_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_PFX)
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_DEF) + "."
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT);

            LOGGER.trace("Try to load default property file '{}'", fileName);
            properties.putAll(loadPropertiesFromFile(fileName));

            // <rootPath>\config\prova_defaults-test.properties
            fileName = provaProperties.getProperty(Config.PROVA_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_PFX)
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_DEF)
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_TEST) + "."
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT);

            LOGGER.trace("Try to load project property test file '{}'", fileName);
            properties.putAll(loadPropertiesFromFile(fileName));
        } catch (Exception eX) {
            LOGGER.warn("Failed to load default property file '{}' ({})", fileName, eX);
        }

        return properties;
    }

    /**
     * Search for the default property files and load all properties Searches for: - prova_<projectName>.prop -
     * prova_<projectName>-test.prop
     */
    protected Properties loadProjectPropertyFiles() {
        Properties properties = new Properties();
        String fileName = "";

        try {
            LOGGER.trace("Load project property files for Prova");

            // <rootPath>\config\prova-<projectName>.prop
            fileName = provaProperties.getProperty(Config.PROVA_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_PFX)
                    + provaProperties.getProperty(Config.PROVA_PROJECT).toLowerCase() + "."
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT);

            LOGGER.trace("Try to load project property file '{}'", fileName);
            properties.putAll(loadPropertiesFromFile(fileName));

            // <rootPath>\config\prova_<projectName>-test.prop
            fileName = provaProperties.getProperty(Config.PROVA_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_DIR) + pathSeparator
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_PFX)
                    + provaProperties.getProperty(Config.PROVA_PROJECT).toLowerCase()
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_TEST) + "."
                    + provaProperties.getProperty(Config.PROVA_CONF_FILE_EXT);

            LOGGER.trace("Try to load project property file '{}'", fileName);
            properties.putAll(loadPropertiesFromFile(fileName));
        } catch (Exception eX) {
            LOGGER.warn("Failed to load project property file '{}' ({})", fileName, eX);
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
    protected Properties loadPropertiesFromResource(String fileName) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = null;

        try {
            inputStream = this.getClass().getResourceAsStream(fileName);

            properties.load(inputStream);

            LOGGER.debug("Loaded {} properties from resource '{}'", properties.size(), fileName);

            if (LOGGER.isTraceEnabled()) {
                for (String key : properties.stringPropertyNames()) {
                    LOGGER.trace("> " + key + " => " + properties.getProperty(key));
                }
            }
        } catch (Exception eX) {
            LOGGER.trace("Failed to load hard coded default property file", eX.getMessage());
            throw eX;
        } finally {
            if (inputStream != null)
                inputStream.close();
        }

        return properties;
    }

    /**
     * Load a set of properties from given file
     * 
     * @param fileName
     * @return
     * @throws Exception
     */
    protected Properties loadPropertiesFromFile(String fileName) throws Exception {
        File propertyFile = null;
        Properties properties = new Properties();

        try {
            LOGGER.trace("Loading properties from file: {}", () -> fileName);

            propertyFile = new File(fileName);

            if (propertyFile.isFile() && propertyFile.canRead()) {
                properties.load(new FileInputStream(propertyFile));

                LOGGER.debug("Loaded {} properties from {}", () -> properties.size(), () -> fileName);

                if (LOGGER.isTraceEnabled()) {
                    for (String key : properties.stringPropertyNames()) {
                        LOGGER.trace("> " + key + " => " + properties.getProperty(key));
                    }
                }
            } else {
                LOGGER.warn("Property file '{}' not found", () -> fileName);
            }
        } catch (Exception eX) {
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
    protected void saveProperties(Properties properties, String fileName) {
        try {
            LOGGER.debug("Save {} properties to file {}", () -> properties.size(), () -> fileName);

            properties.store(new FileOutputStream(fileName), "Active configuration saved by Prova");
        } catch (Exception eX) {
            LOGGER.error(eX);
        }
    }

    /**
     * Start the execution of Prova and wait until it's finished
     *
     * @throws Exception
     */
    protected void run() throws Exception {
        try {
            // Start Prova execution (in it's own thread)
            LOGGER.trace("Start Prova");
            prova.start();

            // Wait until Prova thread finished executing
            LOGGER.trace("Wait until Prova thread exists");
            prova.join();
        } catch (Exception eX) {
            LOGGER.trace("Exception: '{}'", eX.getMessage());
            throw eX;
        }
    }
}
