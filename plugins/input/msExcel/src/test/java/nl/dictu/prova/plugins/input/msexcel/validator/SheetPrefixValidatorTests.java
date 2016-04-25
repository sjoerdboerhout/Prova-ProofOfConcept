package nl.dictu.prova.plugins.input.msexcel.validator;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for SheetPrefixValidator class
 *
 * @author Hielke de Haan
 */
public class SheetPrefixValidatorTests
{
  private Workbook workbook;

  @Before
  public void setUp() throws IOException
  {
    workbook = new XSSFWorkbook(this.getClass().getResourceAsStream("../tests/functional/projectSubsidies/verlening/AVBH/AVBH.xlsm"));
  }

  @Test
  public void testSheetPrefixes()
  {
    assertFalse(new SheetPrefixValidator(workbook.getSheetAt(1)).validate());
    assertTrue(new SheetPrefixValidator(workbook.getSheetAt(2)).validate());
  }
}
