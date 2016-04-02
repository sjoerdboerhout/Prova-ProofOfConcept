package nl.dictu.prova.plugins.reporting.simplereport;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

public class SimpleReportTest
{
  private Collection<String> collection;

  /*
   *  One-time initialization code
   */
  @BeforeClass 
  public static void oneTimeSetUp()
  {
    System.out.println("@BeforeClass - oneTimeSetUp");
  }

  /*
   *  One-time cleanup code
   */
  @AfterClass 
  public static void oneTimeTearDown()
  {
    System.out.println("@AfterClass - oneTimeTearDown");
  }

  /*
   *  Before each test
   */
  @Before 
  public void setUp()
  {
    collection = new ArrayList<String>();
    System.out.println("@Before - setUp");
  }

  /*
   *  After each test
   */
  @After 
  public void tearDown()
  {
    collection.clear();
    System.out.println("@After - tearDown");
  }

  /*
   * Issue ID:    PROVA-X
   * Requirement: ...
   * 
   * Test description
   */
  @Test 
  public void testEmptyCollection()
  {
    assertTrue(collection.isEmpty());
    System.out.println("@Test - testEmptyCollection");
  }

  /*
   * Issue ID:    PROVA-Y
   * Requirement: ...
   * 
   * Test description
   */
  @Test 
  public void testOneItemCollection()
  {
    collection.add("itemA");
    assertEquals(1, collection.size());
    System.out.println("@Test - testOneItemCollection");
  }

  /*
   * Issue ID:    PROVA-Z
   * Requirement: ...
   * 
   * Test description
   */
  @Test
  public void testTrue() 
  {
    assertTrue(true);
    System.out.println("@Test - testTrue");
  } 
}
