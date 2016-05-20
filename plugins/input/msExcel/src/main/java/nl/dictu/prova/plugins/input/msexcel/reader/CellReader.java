package nl.dictu.prova.plugins.input.msexcel.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.format.CellDateFormatter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods for evaluating the contents of a spreadsheet cell.
 *
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class CellReader
{
  private static final Logger LOGGER = LogManager.getLogger();
  private Workbook workbook;
  private FormulaEvaluator formulaEvaluator;

  public CellReader(Workbook workbook)
  {
    this.workbook = workbook;
    formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
  }

  /**
   * Evaluates contents of a spreadsheet cell and returns a string representation of said content
   *
   * @param cell Spreadsheet cell to be evaluated
   * @return String representation of spreadsheet cell value
   * @throws Exception
   */
  public String evaluateCellContent(Cell cell) throws Exception
  {
    final String LOG_PREFIX = getLogPrefix(cell);
    LOGGER.trace(LOG_PREFIX + "evaluating content '{}'", () -> cell);

    String result;

    switch (cell.getCellType())
    {
      case Cell.CELL_TYPE_BOOLEAN:
        result = getBooleanString(cell);
        break;
      case Cell.CELL_TYPE_STRING:
        result = cell.getStringCellValue();
        break;
      case Cell.CELL_TYPE_NUMERIC:
        if (DateUtil.isCellDateFormatted(cell))
          result = getDateString(cell);
        else
          result = getNumericString(cell);
        break;
      case Cell.CELL_TYPE_FORMULA:
        result = evaluateFormula(cell);
        break;
      case Cell.CELL_TYPE_BLANK:
        result = "";
        break;
      default:
        throw new Exception(LOG_PREFIX + "unknown cell type " + cell.getCellType());
    }
    return result;
  }

  /**
   * Assuming the given cell contains a formula, this method tries to evaluate the formula.
   * When evaluation fails, it tries to return the cached result from the last time the formula was evaluated.
   *
   * @param cell Cell containing the formula
   * @return String value of the formula result
   * @throws Exception
   */
  private String evaluateFormula(Cell cell) throws Exception
  {
    final String LOG_PREFIX = getLogPrefix(cell);
    LOGGER.trace(LOG_PREFIX + "evaluating formula");

    try
    {
      CellValue cellValue = formulaEvaluator.evaluate(cell);

      switch (cellValue.getCellType())
      {
        case Cell.CELL_TYPE_BOOLEAN:
          return getBooleanString(cellValue);
        case Cell.CELL_TYPE_STRING:
          return cellValue.getStringValue();
        case Cell.CELL_TYPE_NUMERIC:
          return getNumericString(cellValue);
        case Cell.CELL_TYPE_BLANK:
          return "";
        case Cell.CELL_TYPE_ERROR:
          LOGGER.warn(LOG_PREFIX + "formula couldn't not be evaluated. Returning cached result. ({})", () -> getErrorString(cell));
          return evaluateCachedFormulaResult(cell);
        default:
          throw new Exception("Unknown formula result type: " + cellValue.getCellType());
      }
    } 
    catch (Exception e)
    {
      // Little hack to prevent known warning for macro SheetName()
      if(!LOG_PREFIX.contains("!B2"))
        LOGGER.warn(LOG_PREFIX + "Exception while evaluating formula: {}", e::getMessage);
      
      return evaluateCachedFormulaResult(cell);
    }
  }

  /**
   * Assuming the given cell contains a formula, this method returns the cached result of the formula from the last time
   * it was evaluated.
   *
   * @param cell Cell containing the formula
   * @return String value of the formula result
   * @throws Exception
   */
  private String evaluateCachedFormulaResult(Cell cell) throws Exception
  {
    final String LOG_PREFIX = getLogPrefix(cell);
    LOGGER.trace(LOG_PREFIX + "evaluating cached formula result");

    switch (cell.getCachedFormulaResultType())
    {
      case Cell.CELL_TYPE_BOOLEAN:
        return getBooleanString(cell);
      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();
      case Cell.CELL_TYPE_NUMERIC:
        return getNumericString(cell);
      case Cell.CELL_TYPE_ERROR:
        throw new Exception(LOG_PREFIX + "error in cached formula cell result: " + getErrorString(cell));
      default:
        throw new Exception(LOG_PREFIX + "unknown cached formula result type: " + cell.getCellType());
    }
  }

  /**
   * Returns the string value of the boolean cell value
   *
   * @param cell Cell containing the boolean value
   * @return String value of the boolean cell value
   */
  private String getBooleanString(Cell cell)
  {
    return getBooleanString(cell.getBooleanCellValue());
  }

  /**
   * Returns the string value of the boolean cell value
   *
   * @param cellValue CellValue containing the boolean value
   * @return String value of the boolean cell value
   */
  private String getBooleanString(CellValue cellValue)
  {
    return getBooleanString(cellValue.getBooleanValue());
  }

  /**
   * Returns the string value of a boolean
   *
   * @param b Boolean value
   * @return String value of the boolean
   */
  private String getBooleanString(boolean b)
  {
    return b ? "true" : "false";
  }

  /**
   * Returns the string value of the numeric cell value
   *
   * @param cell Cell containing the numeric value
   * @return String value of the numeric cell value
   */
  private String getNumericString(Cell cell)
  {
    return new DataFormatter().formatCellValue(cell).replace(",", ".");
  }

  /**
   * Returns the string value of the numeric cell value
   *
   * @param cellValue Cell containing the numeric value
   * @return String value of the numeric cell value
   */
  private String getNumericString(CellValue cellValue)
  {
    return getNumericString(cellValue.getNumberValue());
  }

  /**
   * Returns the string value of the numeric value
   *
   * @param d Double representing the numeric value
   * @return String value of the numeric cell value
   */
  private String getNumericString(double d)
  {
    return String.valueOf(d).replace(",", ".");
  }

  /**
   * Returns the string value of the date cell value
   *
   * @param cell Cell containing the date value
   * @return String value of the date cell value
   */
  private String getDateString(Cell cell)
  {
    return new CellDateFormatter(cell.getCellStyle().getDataFormatString()).format(cell.getDateCellValue());
  }

  /**
   * Returns the string value of the error cell value
   *
   * @param cell Cell containing the error value
   * @return String value of the error cell value
   */
  private String getErrorString(Cell cell)
  {
    return FormulaError.forInt(cell.getErrorCellValue()).getString();
  }

  /**
   * Checks if the given cell contains a tag value.
   * A value is a tag if it is surrounded by square brackets.
   *
   * @param cell Cell which might contain a tag value
   * @return Whether or not the cell contains a tag value
   */
  public boolean isTag(Cell cell)
  {
    String content = "";
    try
    {
      content = evaluateCellContent(cell);
    } catch (Exception e)
    {
      // if an exception is thrown it surely wasn't a tag
    }
    return isTag(content);
  }

  /**
   * Checks if the given string contains a tag value.
   * A value is a tag if it is surrounded by square brackets.
   *
   * @param cellContent String which might contain a tag value
   * @return Whether or not the string contains a tag value
   */
  public boolean isTag(String cellContent)
  {
    return cellContent.trim().matches("^\\[[A-Za-z0-9]+\\]$");
  }

  /**
   * Extracts the tag name from a cell.
   * For example if the cell contains "[TAG]", "TAG" is returned.
   *
   * @param cell Cell containing the tag value
   * @return Tag name
   */
  public String getTagName(Cell cell) throws Exception
  {
    return getTagName(evaluateCellContent(cell));
  }

  /**
   * Extracts the tag name from a string.
   * For example if the string contains "[TAG]", "TAG" is returned.
   *
   * @param cellContent String containing the tag value
   * @return Tag name
   */
  public String getTagName(String cellContent)
  {
    Pattern pattern = Pattern.compile("^\\[([A-Za-z0-9]+)\\]$");
    Matcher matcher = pattern.matcher(cellContent.toLowerCase().trim());
    if (matcher.find())
      return matcher.group(1);
    else
      return null;
  }

  public Cell getByCellReference(int sheetIndex, String reference) throws Exception
  {
    Sheet sheet = workbook.getSheetAt(sheetIndex);
    if (sheet != null)
    {
      CellReference cellReference = new CellReference(reference);
      Row row = sheet.getRow(cellReference.getRow());
      if (row != null)
      {
        Cell cell = row.getCell(cellReference.getCol());
        if (cell != null)
        {
          return cell;
        } else
        {
          throw new Exception("Cell " + cellReference.getRow() + ":" + cellReference.getCol() + " not found");
        }
      } else
      {
        throw new Exception("Row " + cellReference.getRow() + " not found");
      }
    } else
    {
      throw new Exception("Sheet " + sheetIndex + " not found");
    }
  }

  /**
   * Builds a log prefix using information from the given cell.
   *
   * @param cell Cell
   * @return Log prefix
   */
  private String getLogPrefix(Cell cell)
  {
    return cell.getSheet().getSheetName() + "!" + cell.getAddress() + " - ";
  }
}
