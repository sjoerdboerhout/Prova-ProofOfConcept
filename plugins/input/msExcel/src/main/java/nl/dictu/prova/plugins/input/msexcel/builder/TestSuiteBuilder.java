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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.framework.TestSuite;
import nl.dictu.prova.plugins.input.msexcel.reader.WorkbookReader;
import nl.dictu.prova.plugins.input.msexcel.validator.SheetPrefixValidator;

/**
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class TestSuiteBuilder
{
  private static final Logger LOGGER = LogManager.getLogger();
  private static final FileFilter directoryFilter = file -> file.isDirectory() && !file.getName().equals("testdata");
  private static final FileFilter excelFlowFileFilter = file -> !file.isDirectory() && !file.getName().startsWith("~") && Arrays.asList("xlsm").contains(FilenameUtils.getExtension(file.getName()));
  //private static final FileFilter excelDataFileFilter = file -> !file.isDirectory() && !file.getName().startsWith("~") && Arrays.asList("xlsx").contains(FilenameUtils.getExtension(file.getName()));

  public TestSuite buildTestSuite(File rootDirectory, TestRunner testRunner) throws Exception
  {
    LOGGER.trace("Directory: {}", rootDirectory::getPath);
    TestSuite testSuite = new TestSuite(rootDirectory.getPath());
    testSuite.setTestRunner(testRunner);
    testSuite = addTestCases(testSuite);

    File[] directories = rootDirectory.listFiles(directoryFilter);
    for (File directory : directories)
    {
      testSuite.addTestSuite(buildTestSuite(directory, testRunner));
    }

    return testSuite;
  }

	private TestSuite addTestCases(TestSuite testSuite) throws Exception {
		File[] excelFiles = new File(testSuite.getId()).listFiles(excelFlowFileFilter);

		for (File excelFile : excelFiles) {
			LOGGER.trace("File: {}", excelFile);
			Workbook workbook = new XSSFWorkbook(excelFile);
			WorkbookReader workbookReader = new WorkbookReader(workbook);
			LinkedList<String> testDataSets = null;
			TestCase testCase = null;

			for (Sheet sheet : workbook) {
				LOGGER.trace("Sheet: {}", sheet::getSheetName);
				boolean firstTagIsTcid = false;
				if (new SheetPrefixValidator(sheet).validate()) {
					for (Row row : sheet) {
						if (row != null) {
							Cell cell = row.getCell(0);
							if (cell != null) {
								String cellContent = workbookReader.evaluateCellContent(cell);
								String identifier;

								if (workbookReader.isTag(cellContent)) {
									// examine the first tag that is encountered and check if it is a [TCID] tag
									// if it is, add a new test case to the test suite
									// if it isn't, ignore the sheet
									String tagName = workbookReader.getTagName(cellContent);
									LOGGER.trace("Found tag: {}", tagName);
									
									if (!firstTagIsTcid) {
										if (!tagName.equals("tcid")) {
											break; // ignore sheet
										}
										firstTagIsTcid = true;
									}
									
									if ("tcid".equals(tagName)) {
										testDataSets = collectDataSets(excelFile.getParentFile().getPath(),
												getFileNameWithoutExtension(excelFile.getName()),
												workbookReader.readProperty(row, cell), "testdata");

										if (testDataSets.size() > 0 & new SheetPrefixValidator(sheet).validate("WEB")) {
											for (int i = 0; i < testDataSets.size(); i++) {
												identifier = excelFile.getPath() + File.separator
														+ workbookReader.readProperty(row, cell) + File.separator
														+ File.separator + testDataSets.get(i);

												LOGGER.debug("Create TestCase: '{}' (with data file)", identifier);

												testCase = new TestCase(identifier);
												testCase.setTestRunner(testSuite.getTestRunner());
												testSuite.addTestCase(testCase);
											}
										} else {
											// No data file found. Just create one test case.
											identifier = excelFile.getPath() + File.separator
													+ workbookReader.readProperty(row, cell);

											LOGGER.debug("Create TestCase: '{}' (no data file)", identifier);
											testCase = new TestCase(identifier);
											testCase.setTestRunner(testSuite.getTestRunner());
											testSuite.addTestCase(testCase);
										}
									} 
								}
							}
						}
					}
				}
			}
		}
		return testSuite;
	}

  /**
   * Scan for data files for the given test case.
   * 
   * @param flowFilePath
   * @param testCaseName
   */
  private LinkedList<String> collectDataSets(String flowFilePath, String flowName, String testCaseName, String testDataDir)
  { 
    LinkedList<String> dataSetsList = new LinkedList<String>();
    
    FileFilter dataDirFilter =  dir -> dir.isDirectory() && 
                                !dir.getName().startsWith("~") && 
                                dir.getName().endsWith(flowName);
    
    FileFilter dataFileFilter = file -> !file.isDirectory() && 
                                !file.getName().startsWith("~") &&
                                file.getName().contains(flowName + "_" + testCaseName) &&
                                Arrays.asList("xlsx").contains(FilenameUtils.getExtension(file.getName()));
    
    try
    {
      String startDir = flowFilePath + File.separator + testDataDir;
      
      LOGGER.trace("Get data sets in dir: '{}' for file '{}' for flow '{}'", startDir, flowName, testCaseName);

      // Locate all data files in the test data directory
      if(!new File(startDir).isDirectory())
        return dataSetsList;
      
      File[] allDataFiles = new File(startDir).listFiles(dataFileFilter);

      for(File excelFile : allDataFiles)
      {
        LOGGER.trace("> DATA File: '{}'", excelFile);
        dataSetsList.addAll(getDataSets(excelFile));
      }

      // Locate all flow directories
      File[] allDataDirs = new File(startDir).listFiles(dataDirFilter);
      
      for(File dataDir : allDataDirs)
      {
        LOGGER.trace("> DATA Dir: '{}'", dataDir);
        
        allDataFiles = dataDir.listFiles(dataFileFilter);
        
        for(File dataFile : allDataFiles)
        {
          LOGGER.trace("> DATA File: '{}'", dataFile);
          dataSetsList.addAll(getDataSets(dataFile));
        }
      }
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception occured while retrieving the data sets for TC '{}'/'{}'", flowFilePath,testCaseName, eX);
    }
    finally
    {
      if(LOGGER.isTraceEnabled())
      {
        LOGGER.trace("List of collected data set names: (count={})", () -> dataSetsList.size());
        for(int i=0; i<dataSetsList.size(); i++)
        {
          LOGGER.trace("> " + dataSetsList.get(i));
        }
      }
    }
    
    return dataSetsList;
  }
  
  
  /**
   * 
   * @param flowFilePath
   * @param testCaseName
   */
  private LinkedList<String> getDataSets(File dataFile)
  { 
    LinkedList<String> dataSetsList = new LinkedList<String>();
    
    try
    {
      String path = null;
      LOGGER.trace("Count data sets in file: '{}'", dataFile.getAbsolutePath());
      
      dataSetsList.addAll(new TestDataBuilder().getTestDataSetNames(dataFile));
      
      for(int i=0; i<dataSetsList.size(); i++)
      {
        path = stripFilePathToDir(dataFile,"testdata") + File.separator + dataSetsList.get(i);
        LOGGER.trace("> " + path);
        
        dataSetsList.set(i, path);
      }
      
    }
    catch(Exception eX)
    {
      LOGGER.error("Exception occured while retrieving the data sets in data file '{}''", dataFile, eX);
    }
    
    return dataSetsList;
  }

  /**
   * Strip the file extension from a given filename
   * 
   * @param fileName
   * @return
   */
  private String getFileNameWithoutExtension(String fileName)
  {
    int pos = fileName.lastIndexOf(".");
   
    LOGGER.trace("Strip file extension from '{}' (Pos:{})", fileName, pos);
    
    if(pos > 0)
    {
      return fileName.substring(0, pos);
    }
    else
    {
      return fileName;
    }
  }


  /**
   * Strip the file path until a given directory
   * 
   * @param file
   * @param dirName
   * @return
   */
  private String stripFilePathToDir(File file, String dirName)
  { 
    try
    {
      String fileSeparator = File.separator;
      
      // If separator is a single \ then we have to double it to prevent regex failure!
      if(fileSeparator.equals("\\"))
      {
    	  fileSeparator = File.separator + File.separator;
      }
      
      LOGGER.trace("Split '{}' on '{}' (File separator: '{}')", file.getAbsolutePath(), dirName, fileSeparator);
      
      String[] tokens = file.getAbsolutePath().split(fileSeparator + dirName + fileSeparator);
    
      LOGGER.trace("Splitted '{}' on '{}' ({})", file.getAbsolutePath(), dirName, tokens.length);
     
      return(tokens.length > 1 ? tokens[1] : tokens[0]);
    }
    catch(Exception eX)
    {
      LOGGER.warn(eX);
      return file.getAbsolutePath();
    }
  }
}
