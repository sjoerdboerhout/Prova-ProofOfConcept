package nl.dictu.prova.plugins.output.db.jdbc;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.DbOutputPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import oracle.jdbc.connector.OracleConnectionManager;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Driver for controlling Jdbc Webdriver
 * 
 * @author Sjoerd Boerhout
 * @since  2016-05-11
 *
 */
public class Jdbc implements DbOutputPlugin
{
  final static Logger LOGGER = LogManager.getLogger();
  
  private TestRunner                testRunner          = null;
  private TestCase                  testCase            = null;
  private OracleConnectionManager   connectionManager   = null;
  private Connection                connection          = null;
  private Statement                 statement           = null;
  
  private String                    currentAdress       = null;
  private String                    currentUser         = null;
  private String                    currentPassword     = null;
  private String                    currentPrefix       = null;
  private String                    currentQuery        = null;
  private Boolean                   currentRollback     = true;
  
  
  @Override
  public String getName()
  {
    return "Jdbc database functionality";
  }

  
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: output plugin Jdbc!");
    
    this.testRunner = testRunner;
    
  }
  
  
  @Override
  public void doSetDbProperties(String adress, String user, String password, Boolean rollback)
  {
      LOGGER.trace("Setting properties in output plugin Jdbc.");
      this.currentAdress    = adress;
      this.currentUser      = user;
      this.currentPassword  = password;
      this.currentRollback   = rollback;
  }
  

  @Override
  public void doSetQuery(String query)
  {
      LOGGER.trace("Setting query in output plugin Jdbc.");
      this.currentQuery = query;
  }
  

  @Override
  public Properties doProcessDbResponse() throws Exception
  {
    Properties sqlProperties = new Properties();
    LOGGER.trace("Executing and processing query in output plugin Jdbc.");
      
    if(!isValid())
        throw new Exception("Properties not properly set!");
      
    connection = DriverManager.getConnection(currentAdress, currentUser, currentPassword);
    
    try
    {
      statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(currentQuery);
      
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      
      resultSet.afterLast();
      int rowCount = resultSet.getRow() - 1;
      LOGGER.info(rowCount + " rows retrieved by query");
      
      for(int i = 1; i < resultSetMetaData.getColumnCount(); i++)
      {
          String name = resultSetMetaData.getColumnName(i);
          resultSet.first();
          String key = currentPrefix + "_" + name.toLowerCase() + i;
          String value = resultSet.getString(name);
          sqlProperties.put(key, value);
          resultSet.next();
      }
    }
    catch(SQLException e)
    {
        LOGGER.error("SQLException occured! : " + e.getMessage());
    }
    catch(Exception e)
    {
        LOGGER.error("Exception occured! : " + e.getMessage());
    }
    return sqlProperties;
  }
  

  @Override
  public void shutDown()
  {
      LOGGER.trace("Shutting down output plugin Jdbc");
      try {
          if(currentRollback){
            connection.rollback();
          } else {
            connection.commit();
          }
          connection.close();
      } catch (SQLException ex) {
          LOGGER.error("Unable to close database connection!");
          ex.printStackTrace();
      }
  }


  @Override
  public void setUp(TestCase testCase) throws Exception
  {
      LOGGER.trace("Setting up output plugin Jdbc");
      this.testCase = testCase; 
  }


  @Override
  public void doSleep(long waitTime) throws Exception
  {
    LOGGER.debug(">> Sleep '{}' ms", waitTime);
    
    try
    {
      Thread.sleep(waitTime);
    }
    catch(Exception eX)
    {
      LOGGER.debug("Exception while waiting '{}' ms: {}", 
                    waitTime, eX.getMessage());
          
          throw eX;
    }    
  }

  
  private boolean isValid() {
      if(currentAdress == null) return false;
      if(currentUser == null) return false;
      if(currentPassword == null) return false;
      return true;
  }

    @Override
    public void doSetTests(Properties prprts) throws Exception {
        LOGGER.trace("Setting tests in output plugin Jdbc");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
