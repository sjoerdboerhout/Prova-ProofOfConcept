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
package nl.dictu.prova.plugins.input.msexcel.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks if a sheet has a name with a valid prefix to be considered as the starting point of a new test case.
 * All characters up to but not including the first underscore are assumed to be the prefix of the sheet name.
 * <p>
 * Currently allowed prefixes:
 * - WEB: web test
 * - SH:  shell test
 * - SOAP: testing on SOAP
 *
 * @author Hielke de Haan
 * @since 0.0.1
 */
public class SheetPrefixValidator
{
  private final static Logger LOGGER = LogManager.getLogger();
  private final static List<String> ALLOWED_PREFIXES = Arrays.asList("WEB", "SH", "SOAP", "DB");
  private Sheet sheet;

  /**
   * Constructor with reference to a workbook sheet
   * @param sheet
   */
  public SheetPrefixValidator(Sheet sheet)
  {
    this.sheet = sheet;
  }

  /**
   * 
   * 
   * @return
   */
  public boolean validate()
  {
    String sheetNamePrefix = getSheetNamePrefix(sheet);
    boolean allowed = ALLOWED_PREFIXES.contains(sheetNamePrefix);
    LOGGER.trace("Prefix = {}: {}", sheetNamePrefix, allowed ? "process" : "ignore");
    return allowed;
  }
  
  /**
   * See if provided prefix matches the current sheet prefix.
   * 
   * @param prefix
   * @return
   */
  public boolean validate(String prefix)
  {
    String sheetNamePrefix = getSheetNamePrefix(sheet);
    boolean allowed = prefix.trim().toUpperCase().equals(sheetNamePrefix.toUpperCase());
    return allowed;
  }

  /**
   * Check if the name of the given sheet contains a prefix.
   * If a prefix is found return it. Otherwise return NULL.
   * 
   * @param sheet
   * @return
   */
  private String getSheetNamePrefix(Sheet sheet)
  {
    Pattern pattern = Pattern.compile("^([A-Za-z0-9]+)_[A-Za-z0-9_-]+$");
    Matcher matcher = pattern.matcher(sheet.getSheetName());
    String prefix;

    if (matcher.find())
      prefix = matcher.group(1);
    else
      prefix = null;

    return prefix;
  }
}
