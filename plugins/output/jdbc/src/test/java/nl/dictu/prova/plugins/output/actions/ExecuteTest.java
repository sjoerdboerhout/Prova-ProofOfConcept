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
package nl.dictu.prova.plugins.output.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nl.dictu.prova.framework.TestStatus;
import nl.dictu.prova.plugins.output.JDBC;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Coos van der Galiën
 */
public class ExecuteTest
{
  Connection c = null;
  
  public ExecuteTest()
  {
  }
  
  @BeforeClass
  public static void setUpClass()
  {
  }
  
  @AfterClass
  public static void tearDownClass()
  {
  }
  
  @Before
  public void setUp()
  {
    
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      PreparedStatement st1 = c.prepareStatement("ATTACH DATABASE 'file:test.db' AS db;");
      st1.executeUpdate();
      PreparedStatement st2 = c.prepareStatement("DROP TABLE IF EXISTS db.customers;");
      st2.executeUpdate();
      PreparedStatement st3 = c.prepareStatement("CREATE TABLE IF NOT EXISTS db.customers (" +
                                                  "CustomerName varchar(255)," +
                                                  "ContactName varchar(255)," +
                                                  "Address varchar(255)," +
                                                  "City varchar(255)," +
                                                  "PostalCode varchar(255)," +
                                                  "Country varchar(255)" +
                                                 ");");
      st3.executeUpdate();
      
      int rows = 0;
      
      for(String[] str : getData()){
        PreparedStatement st4 = c.prepareStatement("INSERT INTO customers (CustomerName, ContactName, Address, City, PostalCode, Country)\n " +
                        "VALUES ('" + str[0] + "','" + str[1] + "','" + str[2] + "','" + str[3] + "','" + str[4] + "','" + str[5] + "');");
        st4.executeUpdate();
        rows++;
      }
      System.out.println("Succesfully setup database.");
    } 
    catch (Exception e) 
    {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      e.printStackTrace();
      System.exit(0);
    }
  }
  
  @After
  public void tearDown() throws SQLException
  {
    c.close();
  }

  /**
   * Test of execute method, of class Execute.
   */
  @Test
  public void testExecuteInsert()
  {
    System.out.println("execute");
    try
    {
      Execute instance = new Execute(new JDBC());
      TestStatus expResult = TestStatus.PASSED;
      
      instance.setAttribute("ADDRESS", "jdbc:sqlite:test.db");
      instance.setAttribute("PREFIX", "UnitTest");
      
      instance.setAttribute("QUERY",  "ATTACH DATABASE 'file:test.db' AS db;" + 
                                      "INSERT INTO db.customers (CustomerName, ContactName, Address, City, PostalCode, Country) " +
                                      "VALUES ('Apple Inc.', 'Steve Jobs', 'One Infinite Loop', 'Cupertino', '11111', 'United States');" +
                                      "SELECT * FROM db.customers WHERE ContactName = 'Steve Jobs';"
                                      );
      TestStatus result = instance.execute();
      assertEquals(expResult, result);
      
      System.out.println("ATTACH, INSERT and SELECT statement succesfully executed.");
    }
    catch(Exception ex)
    {
      fail("Exception during execution : " + ex.getMessage());
    }
  }
  
  private static List<String[]> getData()
  {
    ArrayList<String[]> list = new ArrayList<>();
    
    //CustomerName, ContactName, Address, City, PostalCode, Country
    String[] str1 = {"Cardinal","Tom B. Erichsen","Skagen 21","Stavanger","4006","Norway"};
    String[] str2 = {"Wartian Herkku",	"Pirkko Koskitalo",	 "Torikatu 38",	"Oulu",	"90110", "Finland"};
    String[] str3 = {"Wellington Importadora",	"Paula Parente", 	"Rua do Mercado 12",	"Resende",	"08737-363",	"Brazil"};
    String[] str4 = {"White Clover Markets",	"Karl Jablonski", "14th Ave. S. Suite 3B",	"Seattle",	"98128",	"USA"};
    String[] str5 = {"Wilman Kala",	"Matti Karttunen",	"Keskuskatu 45",	"Helsinki",	"21240", "Finland"};
    String[] str6 = {"Wolski",	"Zbyszek	ul.", "Filtrowa 68",	"Walla",	"01-012",	"Poland"};
    
    list.add(str1);list.add(str2);list.add(str3);list.add(str4);list.add(str5);list.add(str6);
    
    return list;
  }
  
}
