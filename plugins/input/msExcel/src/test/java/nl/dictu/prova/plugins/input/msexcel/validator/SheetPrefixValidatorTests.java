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
public class SheetPrefixValidatorTests {
    private Workbook workbook;

    @Before
    public void setUp() throws IOException {
        workbook = new XSSFWorkbook(
                this.getClass().getResourceAsStream("../tests/functional/projectSubsidies/verlening/AVBH/AVBH.xlsm"));
    }

    @Test
    public void testSheetPrefixes() {
        assertFalse(new SheetPrefixValidator(workbook.getSheetAt(1)).validate());
        assertTrue(new SheetPrefixValidator(workbook.getSheetAt(2)).validate());
    }
}
