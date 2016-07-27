package nl.dictu.prova.plugins.output.db.jdbc;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.OutputPlugin;
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
public abstract class Jdbc implements OutputPlugin
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
  private String                    query               = null;
  private Boolean                   rollback            = true;
  
  @Override
  public String getName()
  {
    return "Selenium Webdriver";
  }

  
  @Override
  public void init(TestRunner testRunner) throws Exception
  {
    LOGGER.debug("Init: output plugin Jdbc!");
    
    this.testRunner = testRunner;
    
  }
  
  
  public void doSetProperties(String adress, String user, String password, Boolean rollback)
  {
      this.currentAdress    = adress;
      this.currentUser      = user;
      this.currentPassword  = password;
      this.rollback         = rollback;
  }
  

  public void doSetQuery(String query)
  {
      this.query = query;
  }
  

  public Properties doProcessResponse() throws Exception
  {
    Properties sqlProperties = new Properties();
      
    if(!isValid())
        throw new Exception("Properties not properly set!");
      
    connection = DriverManager.getConnection(currentAdress, currentUser, currentPassword);
    try
    {
      statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(query);
      
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
      try {
          if(rollback){
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
}
