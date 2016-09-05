/*
 *  
 *  Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 *  the European Commission - subsequent versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  
 *  http://ec.europa.eu/idabc/eupl
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence.
 *  
 *  Date:      02-09-2016
 *  Author(s): Coos van der Galiën
 *  
 */
package nl.dictu.prova.plugins.output.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestAction;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.plugins.output.JDBC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Coos van der Galiën
 */
public class Execute extends TestAction
{
  protected final static Logger LOGGER = LogManager.getLogger(Execute.class.
          getName());
  
  public static final String ATTR_ADDRESS   = "ADDRESS";
  public static final String ATTR_USER      = "USER";
  public static final String ATTR_PASSWORD  = "PASSWORD";
  public static final String ATTR_PREFIX    = "PREFIX";
  public static final String ATTR_QUERY     = "QUERY";
  public static final String ATTR_ROLLBACK  = "ROLLBACK";
  
  private TestRunner testRunner = null;
  private JDBC jdbc = null;
  private Connection connection = null;
  private Statement statement = null;

  private String address = null;
  private String user = null;
  private String password = null;
  private String prefix = null;
  private String query = null;
  private Boolean rollback = true;
  private Integer row = 0;

  public Execute(JDBC jdbc)
  {
    super(LOGGER);
    
    this.jdbc = jdbc;
  }

  @Override
  public TestStatus execute()
  {
    Properties sqlProperties = new Properties();
    LOGGER.debug("Executing and processing query in output plugin Jdbc.");

    if (!isValid())
    {
      LOGGER.error("Action is not validated!");
      return TestStatus.FAILED;
    }

    try
    {
      String[] splitAddress = address.split(":");
      String driver = splitAddress[1];
      
      switch(driver){
        case "oracle" : DriverManager.registerDriver(new oracle.jdbc.OracleDriver()); break;
        case "sqlite" : DriverManager.registerDriver(new org.sqlite.JDBC()); break;
      }
      
      connection = DriverManager.getConnection(address, user, password);
      row = 0;

      switch(jdbc.getQueryType(query))
      {
        case SELECT:
          try
          {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            while (resultSet.next())
            {
              for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
              {
                String name = resultSetMetaData.getColumnName(i);
                String key = prefix + "_" + name.toLowerCase() + resultSet.getRow();
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
            return TestStatus.PASSED;
          }
          catch(Exception ex)
          {
            LOGGER.error("Exception during SELECT statement query : " + ex.getMessage());
            ex.printStackTrace();
            return TestStatus.FAILED;
          }
        case DELETE:
        case INSERT:
        case UPDATE:
          try
          {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            row = preparedStatement.executeUpdate();
            preparedStatement.close();
            LOGGER.info(row + " rows affected.");
            if (rollback)
            {
              connection.rollback();
              LOGGER.debug("Statement rolled back");
            }
            else
            {
              connection.commit();
              LOGGER.debug("Statement committed");
            }
          }
          catch(Exception ex)
          {
            LOGGER.error("Exception during INSERT/DELETE/UPDATE statement query : " + ex.getMessage());
            ex.printStackTrace();
            return TestStatus.FAILED;
          }
          break;
        default:
          LOGGER.error("The provided query '" + query.substring(0, 40) + "...' is not supported! See documentation.");
          return TestStatus.NOTRUN;
      }
    }
    catch (SQLException e)
    {
      LOGGER.error("SQLException occured! : " + e.getMessage());
      return TestStatus.FAILED;
    }
    catch (Exception e)
    {
      LOGGER.error("Exception occured! : " + e.getMessage());
      e.printStackTrace();
      return TestStatus.FAILED;
    }
    return TestStatus.PASSED;
  }

  @Override
  public boolean isValid()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String toString()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setAttribute(String key, String value)
  {
    LOGGER.trace("Request to set attribute '{}' to value '{}'.", () -> key, () -> value);
    
    switch(key.toUpperCase())
    {
      case ATTR_QUERY: this.query = value; break;
      case ATTR_USER:  this.user = value; break;
      case ATTR_PASSWORD: this.password = value; break;
      case ATTR_PREFIX: this.prefix = value; break;
      case ATTR_ROLLBACK: this.rollback = value.equalsIgnoreCase("true"); break;
      case ATTR_ADDRESS: this.address = value; break;
      
      default: LOGGER.debug("Unrecognized key '{}'.", () -> key);
    }
  }
}
