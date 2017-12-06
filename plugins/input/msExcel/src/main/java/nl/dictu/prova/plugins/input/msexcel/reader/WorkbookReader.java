/**
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * <p>
 * http://ec.europa.eu/idabc/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * <p>
 * Date:      18-12-2016
 * Author(s): Sjoerd Boerhout, Robert Bralts & Coos van der Galiën
 * <p>
 */
package nl.dictu.prova.plugins.input.msexcel.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Hielke de Haan
 */
public class WorkbookReader extends CellReader {
    public WorkbookReader(Workbook workbook) {
        super(workbook);
    }

    /**
     * Reads a property value from the current row, given a cell containing the property key. It assumes the value is
     * located in the cell right next to the cell containing the key.
     *
     * @param row
     *            Row containing property key and value
     * @param propertyKeyCell
     *            Cell containing the property key
     * @param dateFormat
     *            String containing the format for possible dates
     * @return Property value
     * @throws Exception
     *             when property value not found
     */
    public String readProperty(Row row, Cell propertyKeyCell, String dateFormat) throws Exception {
        Cell propertyValueCell = row.getCell(propertyKeyCell.getColumnIndex() + 1);
        if (dateFormat == null) {
            return readProperty(row, propertyKeyCell);
        }
        if (propertyValueCell != null) {
            return evaluateCellContent(propertyValueCell, dateFormat);
        } else {
            throw new Exception(
                    "Value for property " + evaluateCellContent(propertyKeyCell, dateFormat) + " not found");
        }
    }

    /**
     * Reads a property value from the current row, given a cell containing the property key. It assumes the value is
     * located in the cell right next to the cell containing the key.
     *
     * @param row
     *            Row containing property key and value
     * @param propertyKeyCell
     *            Cell containing the property key
     * @return Property value
     * @throws Exception
     *             when property value not found
     */
    public String readProperty(Row row, Cell propertyKeyCell) throws Exception {
        Cell propertyValueCell = row.getCell(propertyKeyCell.getColumnIndex() + 1);
        if (propertyValueCell != null) {
            return evaluateCellContent(propertyValueCell);
        } else {
            throw new Exception("Value for property " + evaluateCellContent(propertyKeyCell) + " not found");
        }
    }

    /**
     * Reads a property value from the current row, given a cell containing the property key. It assumes the value is
     * located in the cell right next to the cell containing the key. Do not throw Exception when not found. Return null
     * when not found.
     *
     * @param row
     *            Row containing property key and value
     * @param propertyKeyCell
     *            Cell containing the property key
     * @return Property value of null if not found.
     */
    public String fetchProperty(Row row, Cell propertyKeyCell) throws Exception {
        String result = null;
        Cell propertyValueCell = row.getCell(propertyKeyCell.getColumnIndex() + 1);
        if (propertyValueCell != null) {
            result = evaluateCellContent(propertyValueCell);
        }
        return result;
    }
}
