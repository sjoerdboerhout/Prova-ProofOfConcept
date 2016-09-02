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
 *  Date:      DD-MM-YYYY
 *  Author(s): <full name author>
 *  
 */
package nl.dictu.prova.plugins.output;

import java.security.InvalidParameterException;
import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
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
import nl.dictu.prova.TestType;
import nl.dictu.prova.framework.TestAction;

/**
 * Driver for controlling Jdbc Webdriver
 *
 * @author Sjoerd Boerhout
 * @since 2016-05-11
 *
 */
public class JDBC implements OutputPlugin {

  private final static Logger LOGGER = LogManager.getLogger(JDBC.class.
      getName());

  private TestRunner testRunner     = null;
  private Connection connection     = null;
  private Statement statement       = null;

  private String currentAdress      = null;
  private String currentUser        = null;
  private String currentPassword    = null;
  private String currentPrefix      = null;
  private String currentQuery       = null;
  private Boolean currentRollback   = true;
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

  
  public void doSetDbProperties(String adress, String user, String password, String prefix, Boolean rollback) {
      LOGGER.debug("Setting properties in output plugin Jdbc.");
      this.currentAdress      = adress;
      this.currentUser        = user;
      this.currentPassword    = password;
      this.currentPrefix      = prefix;
      this.currentRollback    = rollback;
  }


  public void doSetQuery(String query) {
      LOGGER.debug("Setting query in output plugin Jdbc.");
      this.currentQuery = query;
  }

  
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

  
  public boolean doTest(String property, String test) throws Exception {
      LOGGER.trace("Executing test for property '" + property + "' with validation '" + test + "'");

      if(test.equalsIgnoreCase("{null}")){
          if(testRunner.getPropertyValue(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
              LOGGER.info("Test unsuccesful!");
              return false;
          } else {
              LOGGER.info("Test succesful!");
              return true;
          }
      }

      if(testRunner.hasPropertyValue(property) | testRunner.getPropertyValue(property) != null | testRunner.getPropertyValue(property).trim().length() > 0){
          String propertyValue = testRunner.getPropertyValue(property).trim();
          if(propertyValue.equalsIgnoreCase(test.trim())){
              LOGGER.info("Test succesful!");
              return true;
          }
          LOGGER.info("Test unsuccesful!");
          return false;
      } else {
          LOGGER.info("Test unsuccesful!");
          return false;
      }

  }

  @Override
  public TestType[] getTestType()
  {
    TestType[] testTypes = {TestType.DB};
    return testTypes;
  }

  @Override
  public void tearDown(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TestAction getTestAction(String string) throws InvalidParameterException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setUp(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    private boolean isValid() {
        if (currentAdress == null) return false;
        if (currentUser == null) return false;
        if (currentPassword == null) return false;
        return true;
    }
    
}
