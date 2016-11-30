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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.dictu.prova.Config;
import nl.dictu.prova.plugins.reporting.ReportingPlugin;

/**
 * Driver for controlling Jdbc Webdriver
 *
 * @author Sjoerd Boerhout
 * @since 2016-05-11
 *
 */
public class Jdbc implements DbOutputPlugin
{

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
  private String currentResult = null;
  private Integer currentRetries = null;
  private Integer currentWaittime = null;
  private Boolean currentRollback = true;
  private Boolean exceptionOnTest = null;
  private Integer row = 0;

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
  public void doSetDbPollProperties(Integer retries, Integer waittime, String result)
  {
    LOGGER.debug("Setting poll properties in output plugin Jdbc.");
    this.currentRetries = retries;
    this.currentWaittime = waittime;
    this.currentResult = result;
  }

  @Override
  public void doSetDbProperties(String adress, String user, String password, String prefix, Boolean rollback)
  {
    LOGGER.debug("Setting properties in output plugin Jdbc.");
    this.currentAdress = adress;
    this.currentUser = user;
    this.currentPassword = password;
    this.currentPrefix = prefix;
    this.currentRollback = rollback;
  }

  @Override
  public void doSetQuery(String query)
  {
    LOGGER.debug("Setting query in output plugin Jdbc.");
    this.currentQuery = query;
  }

  @Override
  public Properties doProcessDbResponse() throws Exception
  {
    Properties sqlProperties = new Properties();
    LOGGER.debug("Processing query in output plugin Jdbc with prefix '{}'.", currentPrefix);

    if (!isValid())
    {
      throw new Exception("Properties not properly set!");
    }
    
    while(containsKeywords(currentQuery))
    {
      LOGGER.trace("Found keyword in SOAP message, replacing it with corresponding value.");
      String editedString = replaceKeywords(currentQuery);
      if(editedString == null){
        break;
      }
      else
      {
        currentQuery = editedString;
      }
    }

    try
    {
      row = 0;

      switch(getQueryType())
      {
        case SELECT:
      
          for(ReportingPlugin plugin : this.testRunner.getReportingPlugins()){
            plugin.storeToTxt("" + currentQuery, currentPrefix);
          }

          ResultSet resultSet = executeSelectQuery();
          ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

          LOGGER.debug("Query executed. Processing data...");

          while (resultSet.next())
          {
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
            {
              String name = resultSetMetaData.getColumnName(i);
              String key = currentPrefix + "_" + name.toLowerCase() + resultSet.getRow();
              String value = resultSet.getString(name);
              if (key == null)
              {
                break;
              }
              if (value == null)
              {
                value = "null";
              }
              sqlProperties.put(key, value);
            }
            row = resultSet.getRow();
          }
          resultSet.close();
          LOGGER.info(row + " rows returned.");
          break;
        case DELETE:
        case INSERT:
        case UPDATE:
        case DECLARE:
        case BEGIN:
          for(ReportingPlugin plugin : this.testRunner.getReportingPlugins()){
            plugin.storeToTxt("" + currentQuery, currentPrefix);
          }

          DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
          connection = DriverManager.getConnection(currentAdress, currentUser, currentPassword);

          try (PreparedStatement preparedStatement = connection.prepareStatement(currentQuery)) {
            row = preparedStatement.executeUpdate();
          }
          LOGGER.info(row + " rows affected.");

          if (currentRollback)
          {
            connection.rollback();
            LOGGER.debug("Statement rolled back");
          }
          else
          {
            connection.commit();
            LOGGER.debug("Statement committed");
          }
          break;
        case UNSUPPORTED:
          throw new Exception("The provided query '" + currentQuery.substring(0, currentQuery.length() < 120 ? currentQuery.length() : 120) + "...' is not supported! See documentation.");
      }
    }
    catch (SQLException e)
    {
      LOGGER.error("SQLException occured! : " + e.getMessage());
    }
    catch (Exception e)
    {
      LOGGER.error("Exception occured! : " + e.getMessage());
      e.printStackTrace();
    }
    return sqlProperties;
  }

  @Override
  public void doPollForDbResult() throws Exception
  {
    if (getQueryType() == StatementType.SELECT)
    {        
      for(int i = 1; i <= currentRetries; i++)
      {
        ResultSet resultSet = executeSelectQuery();
        resultSet.next();
      
        String result = resultSet.getString(1);

        if (result == null)
        {
          LOGGER.debug("No result available yet on retry " + i);
        }
        else if(result.trim().equalsIgnoreCase(currentResult))
        {
          LOGGER.debug("Result '{}' is equal to desired result '{}'", result, currentResult);
          break;          
        }
        else
        {
          LOGGER.debug("Result '{}' is not equal to desired result '{}'", result, currentResult);
        }
        
        if(i == currentRetries) resultSet.close();
      }      
    }
  }
  
  private ResultSet executeSelectQuery()
  {
    try
    {
      LOGGER.trace("Executing select query.");
      DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
      connection = DriverManager.getConnection(currentAdress, currentUser, currentPassword);
      
      statement = connection.createStatement();
      return statement.executeQuery(currentQuery);
    }
    catch (SQLException e)
    {
      LOGGER.error("SQLException occured! : " + e.getMessage());
    }
    catch (Exception e)
    {
      LOGGER.error("Exception occured! : " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public enum StatementType
  {
    SELECT, DELETE, INSERT, UPDATE, DECLARE, BEGIN, UNSUPPORTED;
  }

  public StatementType getQueryType()
  {
    String[] splitQuery = currentQuery.split(" ", 2);
    switch (splitQuery[0].trim().toUpperCase())
    {
      case "SELECT":        return StatementType.SELECT;
      case "UPDATE":        return StatementType.UPDATE;
      case "DELETE":        return StatementType.DELETE;
      case "INSERT":        return StatementType.INSERT;
      case "DECLARE":       return StatementType.DECLARE;
      case "BEGIN":         return StatementType.BEGIN;
      default:              return StatementType.UNSUPPORTED;
    }
  }

  @Override
  public void shutDown()
  {
    LOGGER.debug("Shutting down output plugin Jdbc");
    try
    {
      connection.close();
    }
    catch (SQLException ex)
    {
      LOGGER.error("Unable to close database connection!");
      ex.printStackTrace();
    }
  }

  @Override
  public void setUp(TestCase testCase) throws Exception
  {
    LOGGER.debug("Setting up output plugin Jdbc");
    this.testCase = testCase;
  }

  private boolean isValid()
  {
    if (currentAdress == null)
    {
      return false;
    }
    if (currentUser == null)
    {
      return false;
    }
    if (currentPassword == null)
    {
      return false;
    }
    return true;
  }
  
  private Boolean containsKeywords(String entry) throws Exception
  {
    Pattern pattern = Pattern.compile("\\{[A-Za-z0-9._]+\\}");
    Matcher matcher = pattern.matcher(entry);

    while (matcher.find())
    {
      return true;
    }
    return false;
  }
  
  private String replaceKeywords(String entry) throws Exception
  {
    Pattern pattern = Pattern.compile("\\{[A-Za-z0-9._]+\\}");
    Matcher matcher = pattern.matcher(entry);
    StringBuffer entryBuffer = new StringBuffer("");

    while (matcher.find())
    {
      String keyword = matcher.group(0).substring(1, matcher.group(0).length() - 1);
      
      LOGGER.trace("Found keyword " + matcher.group(0) + " in supplied string.");
      
      Boolean failOnNoTestdataKeywords = false;
      
      try
      {
        matcher.appendReplacement(entryBuffer, testRunner.getPropertyValue(keyword));

        try
        {
          failOnNoTestdataKeywords = Boolean.parseBoolean(this.testRunner.getPropertyValue(Config.PROVA_FLOW_FAILON_NOTESTDATAKEYWORD));
        }
        catch(Exception ex)
        {
          LOGGER.error("Error parsing property '{}', please check your property file.", Config.PROVA_FLOW_FAILON_NOTESTDATAKEYWORD);
        }
      }
      catch(Exception ex)
      {
        if(failOnNoTestdataKeywords)
        {
          throw new Exception("Keyword '" + keyword + "' in '" + currentPrefix + "' not defined with a value.");
        }
        else
        {
          matcher.appendReplacement(entryBuffer, keyword);
          LOGGER.error("Keyword '" + keyword + "' in '" + currentPrefix + "' not defined with a value.");
          return null;
        }
      }
    }
    matcher.appendTail(entryBuffer);

    return entryBuffer.toString();
  }

  @Override
  public boolean doTest(String property, String test) throws Exception
  {
    LOGGER.trace("Executing test for property '" + property + "' with validation '" + test + "'");
    
    if(exceptionOnTest == null)
    {
      if(testRunner.hasPropertyValue(Config.PROVA_FLOW_FAILON_TESTFAIL))
      {
        exceptionOnTest = Config.PROVA_FLOW_FAILON_TESTFAIL.equalsIgnoreCase("true");
      }
    }

    if (test.equalsIgnoreCase("{null}"))
    {
      if (testRunner.hasPropertyValue(property))
      {
        if(testRunner.getPropertyValue(property).trim().length() > 0)
        {
          if(exceptionOnTest)
          {
            throw new Exception("Test unsuccessful! Property is not null.");
          }
          else
          {
            LOGGER.info("Test unsuccessful! Property is not null.");
            return false;
          }
        }
        else
        {
          LOGGER.info("Test successful! Property is null.");
          return true;
        }
      }
    }

    if (testRunner.hasPropertyValue(property) & testRunner.getPropertyValue(property) != null & testRunner.getPropertyValue(property).trim().length() > 0)
    {
      String propertyValue = testRunner.getPropertyValue(property).trim();
      if (propertyValue.equalsIgnoreCase(test.trim()))
      {
        LOGGER.info("Test successful! Value: '{}', test: '{}'", propertyValue, test.trim());
        return true; 
      }
      if(exceptionOnTest)
      {
        throw new Exception("Test unsuccessful!  Value is '" + propertyValue + "' instead of '" + test.trim() + "'");
      }
      else
      {
        LOGGER.info("Test unsuccessful!  Value is '{}' instead of '{}'", propertyValue, test.trim());
        return false;
      }
    }
    else
    {
      if(exceptionOnTest)
      {
        throw new Exception("Test unsuccessful! Property doesn't exist.");
      }
      else
      {
        LOGGER.info("Test unsuccessful! Property doesn't exist.");
        return false;
      }
    }

  }
}
