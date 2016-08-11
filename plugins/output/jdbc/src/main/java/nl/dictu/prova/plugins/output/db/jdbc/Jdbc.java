package nl.dictu.prova.plugins.output.db.jdbc;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.DbOutputPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import oracle.jdbc.connector.OracleConnectionManager;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Driver for controlling Jdbc Webdriver
 *
 * @author Sjoerd Boerhout
 * @since 2016-05-11
 *
 */
public class Jdbc implements DbOutputPlugin {

    final static Logger LOGGER = LogManager.getLogger();

    private TestRunner testRunner = null;
    private TestCase testCase = null;
    private OracleConnectionManager connectionManager = null;
    private Connection connection = null;
    private Statement statement = null;

    private String currentAdress = null;
    private String currentUser = null;
    private String currentPassword = null;
    private String currentPrefix = null;
    private String currentQuery = null;
    private Boolean currentRollback = true;
    private Integer row = 0;

    @Override
    public String getName() {
        return "Jdbc database functionality";
    }

    @Override
    public void init(TestRunner testRunner) throws Exception {
        LOGGER.debug("Init: output plugin Jdbc!");
        this.testRunner = testRunner;
    }

    @Override
    public void doSetDbProperties(String adress, String user, String password, String prefix, Boolean rollback) {
        LOGGER.debug("Setting properties in output plugin Jdbc.");
        this.currentAdress = adress;
        this.currentUser = user;
        this.currentPassword = password;
        this.currentPrefix = prefix;
        this.currentRollback = rollback;
    }

    @Override
    public void doSetQuery(String query) {
        LOGGER.debug("Setting query in output plugin Jdbc.");
        this.currentQuery = query;
    }

    @Override
    public Properties doProcessDbResponse() throws Exception {
        Properties sqlProperties = new Properties();
        LOGGER.debug("Executing and processing query in output plugin Jdbc.");

        if (!isValid()) {
            throw new Exception("Properties not properly set!");
        }
        
        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            connection = DriverManager.getConnection(currentAdress, currentUser, currentPassword);
            row = 0;
            
            if(getQueryType() == StatementType.SELECT){
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(currentQuery);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

                while (resultSet.next()) {
                    for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                        String name = resultSetMetaData.getColumnName(i);
                        String key = currentPrefix + "_" + name.toLowerCase() + resultSet.getRow();
                        String value = resultSet.getString(name);
                        if(key == null) break;
                        if(value == null) value = "null";
                        sqlProperties.put(key, value);
                    }
                    row = resultSet.getRow();
                }
                resultSet.close();
                LOGGER.info(row + " rows returned.");
            } else if (getQueryType() == StatementType.DELETE | getQueryType() == StatementType.INSERT | getQueryType() == StatementType.UPDATE){
                PreparedStatement preparedStatement = connection.prepareStatement(currentQuery);
                row = preparedStatement.executeUpdate();
                preparedStatement.close();
                LOGGER.info(row + " rows affected.");
                if (currentRollback) {
                    connection.rollback();
                    LOGGER.debug("Statement rolled back");
                } else {
                    connection.commit();
                    LOGGER.debug("Statement committed");
                }   
            } else {
                throw new Exception("The provided query '" + currentQuery.substring(0, 30) + "...' is not supported! See documentation.");
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException occured! : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception occured! : " + e.getMessage());
            e.printStackTrace();
        }
        return sqlProperties;
    }
    
    public enum StatementType{
        SELECT, DELETE, INSERT, UPDATE, UNSUPPORTED;
    }
    
    public StatementType getQueryType (){
        String[] splitQuery = currentQuery.split(" ", 2);
        switch(splitQuery[0].trim().toUpperCase()){
            case "SELECT": return StatementType.SELECT;
            case "UPDATE": return StatementType.UPDATE;
            case "DELETE": return StatementType.DELETE;
            case "INSERT": return StatementType.INSERT;
            default: return StatementType.UNSUPPORTED;
        }
    }

    @Override
    public void shutDown() {
        LOGGER.debug("Shutting down output plugin Jdbc");
        try {
            connection.close();
        } catch (SQLException ex) {
            LOGGER.error("Unable to close database connection!");
            ex.printStackTrace();
        }
    }

    @Override
    public void setUp(TestCase testCase) throws Exception {
        LOGGER.debug("Setting up output plugin Jdbc");
        this.testCase = testCase;
    }

    @Override
    public void doSleep(long waitTime) throws Exception {
        LOGGER.debug(">> Sleep '{}' ms", waitTime);

        try {
            Thread.sleep(waitTime);
        } catch (Exception eX) {
            LOGGER.debug("Exception while waiting '{}' ms: {}", waitTime, eX.getMessage());
            throw eX;
        }
    }

    private boolean isValid() {
        if (currentAdress == null) return false;
        if (currentUser == null) return false;
        if (currentPassword == null) return false;
        return true;
    }

    @Override
    public boolean doTest(String property, String test) throws Exception {
        LOGGER.trace("Setting tests in output plugin Jdbc");
        
        if(test.equalsIgnoreCase("{null}")){
            if(testRunner.hasPropertyValue(property) | testRunner.getPropertyValue(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
                return false;
            } else {
                return true;
            }
        }
        
        if(testRunner.hasPropertyValue(property) | testRunner.getPropertyValue(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
            String propertyValue = testRunner.getPropertyValue(property).trim();
            String testValue = test.trim();
            if(propertyValue.equalsIgnoreCase(testValue))
                return true;
            return false;
        } else {
            return false;
        }
                
    }

    @Override
    public void doSelectDropdown(String string, String string1) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doSendKeys(String string, String string1) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doSetText(String string, String string1, Boolean bln) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doValidateElement(String string, Boolean bln, double d) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doValidateText(String string, String string1, Boolean bln, double d) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doSwitchFrame(String string, Boolean bln, Boolean bln1) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
