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
package nl.dictu.prova.plugins.input.msexcel.builder;

import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.reader.CellReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.dictu.prova.Config;
import nl.dictu.prova.TestRunner;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestDataBuilder {
    private final static Logger LOGGER = LogManager.getLogger();
    private WorkbookReader workbookReader;

    private TestRunner testRunner;
    private String dateFormat = null;
    private final String DATA = "DATA";
    private final String TEST = "TEST";

    public TestDataBuilder() {
    }

    public TestDataBuilder(TestRunner testRunner) {
        this.testRunner = testRunner;

        try {
            // Argument for evaluateCellContent method
            this.dateFormat = testRunner.getPropertyValue(Config.PROVA_PLUGINS_INPUT_DATEFORMAT);
        } catch (Exception ex) {
        }
    }

    /**
     * Builds a set of test data from sheets on the given path
     * 
     * @param path
     * @return
     * @throws Exception
     */
    public Properties buildTestData(String path, String keySet) throws Exception {
        LOGGER.trace("Build testdata for: {}", path);

        Workbook workbook = new XSSFWorkbook(new File(path));
        workbookReader = new WorkbookReader(workbook);
        LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();

        for (Sheet sheet : workbook) {
            LOGGER.trace("Sheet: {}", sheet::getSheetName);
            readWebSheet(sheet).forEach(testData::put);
        }

        LOGGER.trace("Number of found testData sets: {}", testData.size());

        return getAsProperties(testData, keySet);
    }

    /**
     * Builds a list containing sets of testdata and tests for DB and SOAP sheets
     * 
     * @param path
     * @param sheet
     * @return
     * @throws Exception
     */
    public ArrayList<List<Properties>> buildTestDataAndTests(String path, String sheetname) throws Exception {
        LOGGER.trace("Build testdata and tests for: {}", sheetname);
        ArrayList<List<Properties>> testDatasets = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(new File(path));
        workbookReader = new WorkbookReader(workbook);
        Sheet sheet = null;

        for (Sheet sheetInWorkbook : workbook) {
            if (sheetInWorkbook.getSheetName().trim().contentEquals(sheetname.trim())) {
                sheet = sheetInWorkbook;
                break;
            }
        }

        if (sheet == null) {
            LOGGER.error("No sheet was found with sheetname " + sheetname.trim());
            return testDatasets;
        }
        return processColumns(sheet, testDatasets);
    }

    private ArrayList<List<Properties>> processColumns(Sheet sheet, ArrayList<List<Properties>> testDatasets)
            throws Exception {
        Boolean horizontalColumns = false;
        // getPreparedHeaderCollection gets headercollection regardless of horizontalcolumns or not
        SortedMap<Integer, String> headers = getPreparedHeaderCollection(sheet);
        List<Properties> dataset = new ArrayList<>();

        // Optional horizontal testdata template reader
        if (testRunner.hasPropertyValue("prova.plugins.in.horizontalcolumns")) {
            if (testRunner.getPropertyValue("prova.plugins.in.horizontalcolumns").equalsIgnoreCase("true")) {
                horizontalColumns = true;
            }
        }

        LOGGER.trace("Processing columns for sheet : '{}'", sheet.getSheetName());

        // Loop through all prepared columns and add all of its value to dataset.
        // When dataset is properly filled (has a data and
        for (Entry header : headers.entrySet()) {
            String headertext = (String) header.getValue();

            // Read column and add it to dataset at set position (0 = data, 1 = test)
            if (headertext.equalsIgnoreCase(TEST)) {
                LOGGER.trace("Processing Test column no. {} with header {}", header.getKey(), header.getValue());
                if (dataset.size() == 1) {
                    dataset.add(1,
                            readPreparedColumn(horizontalColumns, sheet, (Integer) header.getKey(), workbookReader));
                } else {
                    LOGGER.warn("No data column added yet, skipping current column.");
                    continue;
                }

                if (dataset.size() == 2) {
                    LOGGER.trace("Adding a dataset to testdatasets.");
                    testDatasets.add(dataset);
                    dataset = new ArrayList<>();
                } else {
                    LOGGER.warn("The dataset for column number {} is incomplete.", header.getKey());
                    dataset = new ArrayList<>();
                }
            } else if (headertext.equalsIgnoreCase(DATA)) {
                if (dataset.size() == 0) {
                    LOGGER.trace("Processing Data column no. {} with header {}", header.getKey(), header.getValue());
                    dataset.add(0,
                            readPreparedColumn(horizontalColumns, sheet, (Integer) header.getKey(), workbookReader));
                } else {
                    LOGGER.trace("dataset not empty, creating new one and processing Data column no. {}",
                            header.getKey());
                    dataset = new ArrayList<>();
                    dataset.add(0,
                            readPreparedColumn(horizontalColumns, sheet, (Integer) header.getKey(), workbookReader));
                }
            }
        }
        return testDatasets;
    }

    public Properties readPreparedColumn(boolean horizontal, Sheet sheet, Integer column, WorkbookReader workbookReader)
            throws Exception {
        Properties columnData = new Properties();

        // Horizontal columns
        if (horizontal) {
            Row selectedColumn = sheet.getRow(column);
            Row keyRow = sheet.getRow(0);

            for (Cell cell : keyRow) {
                if (cell.getColumnIndex() == 0)
                    continue;

                String key = workbookReader.evaluateCellContent(cell);
                if (key != null) {
                    if (key.length() > 0) {
                        Cell valuecell = selectedColumn.getCell(cell.getColumnIndex());
                        String value = "";
                        if (valuecell != null)
                            value = keywordCheckAndFetch(workbookReader.evaluateCellContent(valuecell));

                        if (value.length() > 0) {
                            columnData.put(key, value);
                        } else {
                            LOGGER.warn("Value for keyword '{}' has no length or is null, please check your data!",
                                    key);
                        }
                    }
                }
            }
        }
        // Regular columns
        else {
            SortedMap<Integer, String> keyColumn = readFirstColumnKeys(sheet);

            for (Entry keyname : keyColumn.entrySet()) {
                Row currentRow = sheet.getRow((int) keyname.getKey());
                String key = (String) keyname.getValue();
                Cell valuecell = currentRow.getCell(column);
                String value = "";
                if (valuecell != null)
                    value = keywordCheckAndFetch(workbookReader.evaluateCellContent(valuecell));

                if (value.length() > 0) {
                    columnData.put(key, value);
                } else {
                    LOGGER.warn("Value for keyword '{}' has no length or is null, please check your data!", key);
                }
            }
        }

        LOGGER.trace("Returning {} properties from column {}", columnData.size(), column);

        return columnData;
    }

    private String keywordCheckAndFetch(String input) {
        if (input == null)
            return "";
        String processedInput = input;
        try {
            if (CellReader.isKey(input)) {
                if (testRunner.hasPropertyValue(input.substring(1, (input.length() - 1)))) {
                    processedInput = testRunner.getPropertyValue(input.substring(1, input.length() - 1));
                }
            }
        } catch (Exception ex) {
        }
        return processedInput;
    }

    public SortedMap<Integer, String> getPreparedHeaderCollection(Sheet sheet) throws Exception {
        Boolean horizontalColumns = false;
        if (testRunner.hasPropertyValue("prova.plugins.in.horizontalcolumns")) {
            if (testRunner.getPropertyValue("prova.plugins.in.horizontalcolumns").equalsIgnoreCase("true")) {
                horizontalColumns = true;
            }
        }

        LOGGER.trace("Gathering a prepared headercollection for sheet '{}'", sheet.getSheetName());

        // Regular vertical testdata template reader
        Iterator<Row> rowIterator = sheet.rowIterator();
        SortedMap<Integer, String> columns = new TreeMap<>();
        Map<Integer, String> headers = new HashMap<>();

        if (rowIterator.hasNext()) {
            try {
                if (horizontalColumns) {
                    LOGGER.trace("Getting horizontal sheet keys");
                    headers = readFirstColumnKeys(sheet);
                } else {
                    LOGGER.trace("Getting regular sheet keys");
                    headers = readRow(rowIterator.next());
                }

                for (Entry header : headers.entrySet()) {
                    String headertext = (String) header.getValue();
                    String lastHeader = null;

                    if (!columns.isEmpty())
                        lastHeader = columns.get(columns.lastKey());

                    if (headertext.toUpperCase().startsWith(TEST)) {
                        // Check if last one was a Data column
                        if (lastHeader != null) {
                            if (lastHeader.toUpperCase().startsWith(DATA)) {
                                columns.put((Integer) header.getKey(), (String) header.getValue());
                            } else {
                                LOGGER.warn("Last column was not of type 'Data', skipping set.");
                            }
                        } else {
                            columns.put((Integer) header.getKey(), (String) header.getValue());
                        }
                    } else if (headertext.toUpperCase().startsWith(DATA)) {
                        // Check if last one was a Data column
                        if (lastHeader != null) {
                            if (lastHeader.toUpperCase().startsWith(TEST)) {
                                columns.put((Integer) header.getKey(), (String) header.getValue());
                            } else {
                                LOGGER.warn("Last column was not of type 'Test', skipping set.");
                            }
                        } else {
                            columns.put((Integer) header.getKey(), (String) header.getValue());
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Exception while processing testdata sheet headers, sheet: '{}', message: {}",
                        sheet.getSheetName(), ex.getMessage());
                LOGGER.error(ex);
            }
        }
        return columns;
    }

    // public Properties readHorizontalColumn(Sheet sheet, Integer selectedRow, WorkbookReader workbookReader) throws
    // Exception
    // {
    //
    // Properties rowData = new Properties();
    //
    // Map<Integer, String> headers = readHeaderRow(sheet.getRow(0));
    // //Remove first header "Keywords"
    // headers.remove(0);
    //
    // Row row = sheet.getRow(selectedRow);
    //
    // if (row == null)
    // {
    // LOGGER.trace("Row {} is null, skipping.", selectedRow);
    // return rowData;
    // }
    //
    // for(Entry header : headers.entrySet())
    // {
    // Cell keyCell = sheet.getRow(0).getCell((Integer) header.getKey());
    //
    // if (keyCell != null)
    // {
    // String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
    // LOGGER.trace("Found key: '{}' on column '{}'", key, header.getKey());
    //
    // String rowType = selectedRow % 2 == 0 ? "data" : "tests";
    //
    // if (!key.isEmpty())
    // {
    // Cell cell = row.getCell((Integer) header.getKey());
    // if (cell != null)
    // {
    // String value = workbookReader.evaluateCellContent(cell, dateFormat);
    // if(!CellReader.isKey(value))
    // {
    // if(value.length() > 0)
    // {
    // LOGGER.trace("Found value '{}' for key '{}' in {} 0 based row '{}'", value, key, rowType, selectedRow);
    // rowData.put(key, value);
    // }
    // else
    // {
    // LOGGER.trace("Found no value for key '{}' in {} 0 based row '{}'", key, rowType, selectedRow);
    // }
    // }
    // //Value is a tag, searching value.
    // else
    // {
    // value = workbookReader.getTagName(value);
    //
    // LOGGER.trace("Property value is a property, retrieving value from collection.");
    // if(testRunner.hasPropertyValue(value))
    // {
    // value = testRunner.getPropertyValue(value);
    // if(value.length() > 0)
    // {
    // LOGGER.trace("Found property value '{}' for key '{}' in {} 0 based row '{}'", value, key, rowType, selectedRow);
    // rowData.put(key, value);
    // }
    // else
    // {
    // LOGGER.trace("Found no property value for key '{}' in {} 0 based row '{}'", key, rowType, selectedRow);
    // }
    // }
    // else
    // {
    // throw new Exception("No value available for " + rowType + " tag " + value);
    // }
    // }
    // }
    // }
    // else
    // {
    // LOGGER.debug("Row {} is empty; skipping row", selectedRow);
    // }
    // if((Integer) header.getKey() == headers.size() - 1)
    // {
    // LOGGER.trace("Finished reading horizontal column");
    // }
    // }
    // }
    // String message = selectedRow % 2 == 0 ? "Added " + rowData.size() + " testvalidation properties to dataset." :
    // "Added " + rowData.size() + " testdata properties to dataset.";
    // LOGGER.trace(message);
    // return rowData;
    // }
    //
    public Properties readColumn(Sheet sheet, Integer column, Integer start, Integer end, WorkbookReader workbookReader)
            throws Exception {

        Properties columnData = new Properties();
        String columnType = column % 2 == 0 ? "tests" : "data";

        for (int rowNum = start; rowNum <= end; rowNum++) {
            Row row = sheet.getRow(rowNum);
            Cell keyCell = row.getCell(0);
            if (keyCell != null) {
                String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
                LOGGER.trace("Found key: '{}'", key);

                if (!key.isEmpty()) {
                    Cell cell = row.getCell(column);
                    if (cell != null) {
                        String value = workbookReader.evaluateCellContent(cell, dateFormat);
                        if (!CellReader.isKey(value)) {
                            if (value.length() > 0) {
                                LOGGER.trace("Found value '{}' for key '{}' in {} column '{}'", value, key, columnType,
                                        column);
                                columnData.put(key, value);
                            } else {
                                LOGGER.trace("Found no value for key '{}' in {} column '{}'", key, columnType, column);
                            }
                        }
                        // Value is a tag, searching value.
                        else {
                            value = value.substring(1, value.length() - 1);
                            LOGGER.trace("Property value is a property, retrieving value from collection.");
                            if (testRunner.hasPropertyValue(value)) {
                                value = testRunner.getPropertyValue(value);
                                if (value.length() > 0) {
                                    LOGGER.trace("Found value '{}' for key '{}' in {} column '{}'", value, key,
                                            columnType, column);
                                    columnData.put(key, value);
                                } else {
                                    LOGGER.trace("Found no property value for key '{}' in {} column '{}'", key,
                                            columnType, column);
                                }
                            } else {
                                throw new Exception("No value available for tag " + value);
                            }
                        }
                    } else {
                        LOGGER.trace("Found no value for key '{}' in {} column '{}'", key, columnType, column);
                    }
                } else {
                    LOGGER.trace("Row {} is empty; skipping row", row.getRowNum());
                }
            } else {
                LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
            }
            if (rowNum == end) {
                LOGGER.trace("Finished reading column");
            }
        }
        String message = column % 2 == 0 ? "Added " + columnData.size() + " testdata properties to dataset."
                : "Added " + columnData.size() + " testvalidation properties to dataset.";
        LOGGER.trace(message);
        return columnData;
    }

    /**
     * 
     * @param sheet
     * @return
     * @throws Exception
     */
    private LinkedHashMap<String, Map<String, String>> readWebSheet(Sheet sheet) throws Exception {
        LinkedHashMap<String, Map<String, String>> testData = new LinkedHashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();

        LOGGER.trace("TestData Builder readWebSheet: '{}'", sheet.getSheetName());

        if (rowIterator.hasNext()) {
            // read headers
            Map<Integer, String> headers = readRow(rowIterator.next());

            // initialize submaps in testData map for eacht header
            for (int colNum = 1; colNum < headers.size(); colNum++) {
                LOGGER.trace("Create a column for test set '{}'", headers.get(colNum));
                testData.put(headers.get(colNum), new HashMap<>());
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // column 0 contains key
                Cell keyCell = row.getCell(0);
                if (keyCell != null) {
                    String key = workbookReader.evaluateCellContent(keyCell, dateFormat);
                    LOGGER.trace("Found key: '{}'", key);

                    if (!key.isEmpty()) {
                        // columns 1 and on contain values
                        for (int colNum = 1; colNum < headers.size(); colNum++) {
                            Cell cell = row.getCell(colNum);
                            if (cell != null) {
                                String value = workbookReader.evaluateCellContent(cell, dateFormat);

                                // Empty values are only allowed in column 1
                                // or keep all columns empty.
                                if (value.isEmpty() && (colNum > 1)) {
                                    LOGGER.trace(
                                            "No value found for key '{}' in column '{}'; copying from column '{}' ({})",
                                            key, headers.get(colNum), headers.get(colNum - 1), colNum);
                                    testData.get(headers.get(colNum)).put(key,
                                            testData.get(headers.get(colNum - 1)).get(key));
                                } else {
                                    LOGGER.trace("Found value '{}' for key '{}' in column '{}'", value, key,
                                            headers.get(colNum));
                                    testData.get(headers.get(colNum)).put(key, value);
                                }
                            }
                        }
                    } else {
                        LOGGER.trace("Row {} is empty; skipping row", row.getRowNum());
                    }
                } else {
                    LOGGER.debug("Row {} is empty; skipping row", row.getRowNum());
                }
            }
        }
        return testData;
    }

    private SortedMap<Integer, String> readRow(Row row) throws Exception {
        LOGGER.trace("Gathering the keys from row '{}' for sheet '{}'", row.getRowNum(), row.getSheet().getSheetName());
        SortedMap<Integer, String> keys = new TreeMap<>();
        for (Cell cell : row) {
            if (cell != null) {
                String cellContent = workbookReader.evaluateCellContent(cell, dateFormat);
                if (!cellContent.isEmpty())
                    keys.put(cell.getColumnIndex(), cellContent);
            }
        }

        LOGGER.trace("Read row {}", keys);
        return keys;
    }

    private SortedMap<Integer, String> readFirstColumnKeys(Sheet sheet) throws Exception {
        LOGGER.trace("Gathering the keys from first column for sheet '{}'", sheet.getSheetName());

        SortedMap<Integer, String> headers = new TreeMap<>();

        // Starts at 1 because first row is keycell horizontal column (names of properties)
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();

            // Skip first row
            if (currentRow.getRowNum() == 0) {
                continue;
            }

            Cell headercell = currentRow.getCell(0);
            if (headercell != null) {
                String cellContent = workbookReader.evaluateCellContent(headercell, dateFormat);
                if (!cellContent.isEmpty())
                    headers.put(currentRow.getRowNum(), cellContent);
            }
        }

        LOGGER.trace("Read headers {}", headers);
        return headers;
    }

    /**
     * Collect all the sheets and columns containing test data in the given <file>. Used to create the correct number of
     * test cases for a test flow.
     * 
     * @param file
     * @return
     */
    public LinkedList<String> getTestDataSetNames(File file) {
        LinkedList<String> testDataSets = new LinkedList<String>();
        Workbook workbook = null;

        try {
            LOGGER.trace("Get testdata sets for: {}", file);

            workbook = new XSSFWorkbook(file);
            workbookReader = new WorkbookReader(workbook);

            for (Sheet sheet : workbook) {
                LOGGER.trace("Sheet: {}", sheet.getSheetName());

                Iterator<Row> rowIterator = sheet.rowIterator();

                if (rowIterator.hasNext()) {
                    // read headers ("Keywords", DataSet1, ...)
                    Map<Integer, String> headers = readRow(rowIterator.next());

                    for (Map.Entry<Integer, String> entry : headers.entrySet()) {
                        if (!entry.getValue().toLowerCase().equals("keywords")) {
                            LOGGER.debug("Add test data set '{}'",
                                    () -> sheet.getSheetName() + File.separator + entry.getValue());
                            testDataSets.add(sheet.getSheetName() + File.separator + entry.getValue());
                        }
                    }
                }
            }
        } catch (Exception eX) {
            LOGGER.error("Exception occured while reading the sheet names from file '{}''", file, eX);
        } finally {
            try {
                workbook.close();
            } catch (IOException eX) {
                LOGGER.warn(eX);
            }
        }
        return testDataSets;
    }

    /**
     * Temporary function until class is rewritten! Convert the requested map to a set of properties.
     * 
     * @param testData
     * @param keySet
     * @return
     */
    public Properties getAsProperties(LinkedHashMap<String, Map<String, String>> testData, String keySet) {
        Properties properties = new Properties();

        try {
            LOGGER.trace("Convert test data set '{}' to property", keySet);

            for (String key : testData.keySet()) {
                LOGGER.trace("Data set: '{}' (Hit:{})", key, key.equals(keySet));

                if (key.equals(keySet)) {
                    Map<String, String> map = testData.get(key);

                    for (Entry<String, String> entry : map.entrySet()) {
                        if (CellReader.isKey(entry.getValue())) {
                            String value = entry.getValue().substring(1, entry.getValue().length() - 1);
                            if (testRunner == null) {
                                LOGGER.error("testRunner is null, property " + entry.getValue() + " not stored.");
                                break;
                            }
                            LOGGER.trace("Property value is a property, retrieving value from collection.");
                            if (testRunner.hasPropertyValue(value)) {
                                LOGGER.trace("> {}: '{}'", entry.getKey(), testRunner.getPropertyValue(value));
                                properties.setProperty(entry.getKey(), testRunner.getPropertyValue(value));
                            } else {
                                throw new Exception("No property value found for property " + entry.getValue()
                                        + " and key " + entry.getKey());
                            }
                        } else {
                            LOGGER.trace("> {}: '{}'", entry.getKey(), entry.getValue());
                            properties.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                    break;
                }
            }
        } catch (Exception eX) {
            LOGGER.error("Error while converting Map '{}' to properties", keySet, eX);
        }

        return properties;
    }
    //
    // /**
    // * testDataVerifier is a method that takes the testdata supplied from buildTestDataAndTests,
    // * which is a method specifically for SOAP & DB testdatasheets, and verifies for content in
    // * either the testdata or testvalidations Properties object. Incase of no content, it is removed.
    // * @param testDatasets
    // * @return
    // */
    // private ArrayList<List<Properties>> testDataVerifier(ArrayList<List<Properties>> testDatasets)
    // {
    // Properties testdata;
    // Properties testvalidations;
    // ArrayList<List<Properties>> toBeDeletedDatasets = new ArrayList<>();
    // for(List<Properties> dataset : testDatasets)
    // {
    // testdata = dataset.get(0);
    // testvalidations = dataset.get(1);
    // if(testdata.size() == 0 & testvalidations.size() == 0)
    // {
    // toBeDeletedDatasets.add(dataset);
    // }
    // }
    // //ConcurrentModificationException occurs if dataset is removed while looping over testDatasets.
    // while(toBeDeletedDatasets.iterator().hasNext())
    // {
    // List<Properties> deleteDataset = toBeDeletedDatasets.iterator().next();
    // testDatasets.remove(deleteDataset);
    // toBeDeletedDatasets.remove(deleteDataset);
    // LOGGER.trace("Removing empty dataset, please check your testdatasheet for empty columns.");
    // }
    // return testDatasets;
    // }

}
