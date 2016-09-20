package nl.dictu.prova.plugins.input.msexcel.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Hielke de Haan
 */
public class WorkbookReader extends CellReader
{
  public WorkbookReader(Workbook workbook)
  {
    super(workbook);
  }
  
  /**
   * Reads a property value from the current row, given a cell containing the property key.
   * It assumes the value is located in the cell right next to the cell containing the key.
   *
   * @param row             Row containing property key and value
   * @param propertyKeyCell Cell containing the property key
   * @param dateFormat      String containing the format for possible dates
   * @return Property value
   * @throws Exception when property value not found
   */
  public String readProperty(Row row, Cell propertyKeyCell, String dateFormat) throws Exception
  {
    Cell propertyValueCell = row.getCell(propertyKeyCell.getColumnIndex() + 1);
    if (dateFormat == null)
    {
      return readProperty(row, propertyKeyCell);
    }
    if (propertyValueCell != null)
    {
      return evaluateCellContent(propertyValueCell, dateFormat);
    } else
    {
      throw new Exception("Value for property " + evaluateCellContent(propertyKeyCell, dateFormat) + " not found");
    }
  }

  /**
   * Reads a property value from the current row, given a cell containing the property key.
   * It assumes the value is located in the cell right next to the cell containing the key.
   *
   * @param row             Row containing property key and value
   * @param propertyKeyCell Cell containing the property key
   * @return Property value
   * @throws Exception when property value not found
   */
  public String readProperty(Row row, Cell propertyKeyCell) throws Exception
  {
    Cell propertyValueCell = row.getCell(propertyKeyCell.getColumnIndex() + 1);
    if (propertyValueCell != null)
    {
      return evaluateCellContent(propertyValueCell);
    } else
    {
      throw new Exception("Value for property " + evaluateCellContent(propertyKeyCell) + " not found");
    }
  }
}
