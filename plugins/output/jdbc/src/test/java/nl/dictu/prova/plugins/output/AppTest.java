package nl.dictu.prova.plugins.output;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.sqlite.JDBC;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
{
  public static void main( String args[] ) throws SQLException
  {
    Connection c = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite::memory:");
      System.out.println("Opened database successfully");
      PreparedStatement st1 = c.prepareStatement("ATTACH DATABASE 'file::memory:' AS db;");
      st1.executeUpdate();
      System.out.println("Succesfully attached database");
      PreparedStatement st2 = c.prepareStatement("CREATE TABLE IF NOT EXISTS db.customers (" +
                                                  "CustomerName varchar(255)," +
                                                  "ContactName varchar(255)," +
                                                  "Address varchar(255)," +
                                                  "City varchar(255)," +
                                                  "PostalCode varchar(255)," +
                                                  "Country varchar(255)" +
                                                ");");
      st2.executeUpdate();
      System.out.println("Succesfully added table 'customers'");
      int rows = 0;
      
      for(String[] str : getData()){
        PreparedStatement st3 = c.prepareStatement("INSERT INTO customers (CustomerName, ContactName, Address, City, PostalCode, Country)\n " +
                        "VALUES ('" + str[0] + "','" + str[1] + "','" + str[2] + "','" + str[3] + "','" + str[4] + "','" + str[5] + "');");
        st3.executeUpdate();
        rows++;
      }
      
      System.out.println("Succesfully added " + rows + " rows");
      Statement query = c.createStatement();
      ResultSet rs = query.executeQuery("SELECT * FROM customers;");
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      e.printStackTrace();
      System.exit(0);
    }
  }

  private static List<String[]> getData()
  {
    ArrayList<String[]> list = new ArrayList<>();
    for(int i = 1; i < 21; i++){
      String[] str = {"Cardinal","Tom B. Erichsen","Skagen 21","Stavanger","4006","Norway"};
      list.add(str);
    }
    return list;
  }
}