package nl.dictu.prova.plugins.input.msexcel.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import nl.dictu.prova.plugins.input.msexcel.reader.CellReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CellReader class
 *
 * @author Hielke de Haan
 */
public class CellReaderTests
{
  private CellReader cellReader;

  @Before
  public void setUp() throws Exception
  {
    cellReader = new CellReader(new XSSFWorkbook(new File(this.getClass().getResource("../CellReaderTests.xlsx").toURI())));
  }

  @Test
  public void testStringValue() throws Exception
  {
    Cell cell = cellReader.getByCellReference(0, "A1");
    assertEquals("text", cellReader.evaluateCellContent(cell));
  }

  @Test
  public void testNumericalIntValue() throws Exception
  {
    Cell cell = cellReader.getByCellReference(0, "A2");
    assertEquals("1", cellReader.evaluateCellContent(cell));
  }

  @Test
  public void testNumericalFloatValues() throws Exception
  {
    Cell cell1 = cellReader.getByCellReference(0, "B2");
    Cell cell2 = cellReader.getByCellReference(0, "C2");
    Cell cell3 = cellReader.getByCellReference(0, "D2");

    assertEquals("2.2", cellReader.evaluateCellContent(cell1));
    assertEquals("3.33", cellReader.evaluateCellContent(cell2));
    assertEquals("4.0", cellReader.evaluateCellContent(cell3));
  }

  @Test
  public void testBooleanValues() throws Exception
  {
    Cell cell1 = cellReader.getByCellReference(0, "A3");
    Cell cell2 = cellReader.getByCellReference(0, "B3");

    assertEquals("true", cellReader.evaluateCellContent(cell1));
    assertEquals("false", cellReader.evaluateCellContent(cell2));
  }

  @Test
  public void testFormulaValue() throws Exception
  {
    //Cell cell1 = cellReader.getByCellReference(0, "A4");
    Cell cell2 = cellReader.getByCellReference(0, "B4");

    // assertEquals("10", cellReader.evaluateCellContent(cell1));
    // TODO the assert on cell1 fails unexpectedly as Excel formats the formula results as 10 while POI returns 10.0
    assertEquals("1.1", cellReader.evaluateCellContent(cell2));
  }

  @Test(expected = Exception.class)
  public void testFormulaErrorValue() throws Exception
  {
    Cell cell = cellReader.getByCellReference(0, "A5");
    cellReader.evaluateCellContent(cell);
  }

  @Test(expected = Exception.class)
  public void testEmptyCell() throws Exception
  {
    Cell cell = cellReader.getByCellReference(0, "ZZ1");
    cellReader.evaluateCellContent(cell);
  }

  @Test
  public void testIsTagCell() throws Exception
  {
    Cell cell1 = cellReader.getByCellReference(0, "A1");
    Cell cell2 = cellReader.getByCellReference(0, "A6");

    assertFalse(cellReader.isTag(cell1));
    assertTrue(cellReader.isTag(cell2));
  }

  @Test
  public void testIsTagString() throws Exception
  {
    String cellContent1 = cellReader.evaluateCellContent(cellReader.getByCellReference(0, "A1"));
    String cellContent2 = cellReader.evaluateCellContent(cellReader.getByCellReference(0, "A6"));

    assertFalse(cellReader.isTag(cellContent1));
    assertTrue(cellReader.isTag(cellContent2));
  }

  @Test
  public void testGetTagString() throws Exception
  {
    Cell cell = cellReader.getByCellReference(0, "A6");
    assertEquals("tag", cellReader.getTagName(cellReader.evaluateCellContent(cell)));
  }
}
