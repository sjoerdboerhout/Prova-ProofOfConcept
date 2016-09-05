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
import nl.dictu.prova.plugins.output.actions.Execute;
import nl.dictu.prova.plugins.output.actions.RunTests;

/**
 * Driver for controlling Jdbc Webdriver
 *
 * @author Sjoerd Boerhout
 * @since 2016-05-11
 *
 */
public class JDBC implements OutputPlugin 
{

  private final static Logger LOGGER = LogManager.getLogger(JDBC.class.
      getName());
  
  public final static String ACTION_EXECUTE = "EXECUTE";
  public final static String ACTION_RUNTESTS = "RUNTESTS";

  private TestRunner testRunner     = null;
  private TestCase testCase         = null;

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
  public TestAction getTestAction(String name) throws InvalidParameterException
  {
    LOGGER.trace("Request to produce webaction '{}'", () -> name);

    switch (name.toUpperCase())
    {
      case ACTION_EXECUTE:            return new Execute(this);
      case ACTION_RUNTESTS:           return new RunTests(this);
    }
    
    throw new InvalidParameterException("Unknown action '" + name + "' requested");
  }

  
  @Override
  public void setUp(TestCase tc)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
    
  
  public StatementType getQueryType (String query){
    String[] splitQuery = query.split(" ", 2);
    switch(splitQuery[0].trim().toUpperCase()){
      case "SELECT": return StatementType.SELECT;
      case "UPDATE": return StatementType.UPDATE;
      case "DELETE": return StatementType.DELETE;
      case "INSERT": return StatementType.INSERT;
      case "ATTACH": return StatementType.ATTACH;
      default: return StatementType.UNSUPPORTED;
    }
  }

  
  @Override
  public void shutDown() {
      LOGGER.debug("Shutting down output plugin Jdbc");
  }
  
    
  public TestRunner getTestRunner()
  {
    LOGGER.trace("Request for testRunner");
    
    return testRunner;
  }
  
  
  public TestCase getTestCase()
  {
    LOGGER.trace("Request for testCase");
    
    return testCase;
  }
    
}
