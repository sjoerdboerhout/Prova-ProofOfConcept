package nl.dictu.prova.plugins.output.web.scriptprinter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.dictu.prova.TestRunner;
import nl.dictu.prova.framework.TestCase;
import nl.dictu.prova.plugins.output.ShellOutputPlugin;
import nl.dictu.prova.plugins.output.WebOutputPlugin;
import nl.dictu.prova.plugins.output.WebserviceOutputPlugin;

/**
 * Output plugin to print all actions to a file
 * 
 * @author Sjoerd Boerhout
 *
 */
public class ScriptPrinter implements ShellOutputPlugin, WebOutputPlugin, WebserviceOutputPlugin {
	final static Logger LOGGER = LogManager.getLogger();

	private TestRunner testRunner = null;

	/**
	 * Init the plug-in and check if a valid reference to a testRunner was given
	 */
	@Override
	public void init(TestRunner testRunner) throws Exception {
		LOGGER.debug("Init: output plugin ScriptPrinter!");

		if (testRunner == null)
			throw new Exception("No testRunner supplied!");

		this.testRunner = testRunner;
	}

	@Override
	public void setUp(TestCase testCase) throws Exception {
		LOGGER.debug("Setup: Test Case ID '{}'", () -> testCase.getId());
		// TODO start new file
		System.out.println("==================================================");
		System.out.println("Start of TC: '" + testCase.getId() + "'\n");
	}

	@Override
	public void tearDown(TestCase testCase) throws Exception {
		LOGGER.debug("TearDown: Test Case ID '{}'", () -> testCase.getId());
		// TODO Close file
		System.out.println("==================================================\n\n");
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		LOGGER.debug("Shutdown: output plugin ScriptPrinter!");
	}

	@Override
	public void doCaptureScreen(String fileName) throws Exception {
		LOGGER.trace("DoCaptureScreen '{}'", () -> fileName);

		// TODO Auto-generated method stub
		System.out.println("Save a screendump to file '" + fileName + "'");
	}

	@Override
	public void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception {
		LOGGER.trace("DoClick '{}' (right: {}, wait: {})", () -> xPath, () -> rightClick, () -> waitUntilPageLoaded);

		// TODO Auto-generated method stub
		System.out.println("Click on element '" + xPath + "' with " + (rightClick ? "right" : "left") + " click. "
				+ "Wait for page loaded: " + waitUntilPageLoaded);
	}

	@Override
	public void doDownloadFile(String url, String saveAs) throws Exception {
		LOGGER.trace("DoDownloadFile '{}' to '{}'", () -> url, () -> saveAs);

		// TODO Auto-generated method stub
		System.out.println("Save '" + url + "' as " + saveAs);
	}

	@Override
	public void doSelect(String xPath, Boolean select) throws Exception {
		LOGGER.trace("DoSelect '{}' ({})", () -> xPath, () -> select);

		// TODO Auto-generated method stub
		System.out.println((select ? "Select" : "Deselect") + " '" + xPath + "'");
	}

	@Override
	public void doSelectDropdown(String xPath, String select) throws Exception {
		LOGGER.trace("DoSelectDropdown '{}' ({})", () -> xPath, () -> select);

		// TODO Auto-generated method stub
		System.out.println("Selecting '" + select + "' in element: " + xPath + "'");

	}

	@Override
	public void doSendKeys(String keys) throws Exception {
		LOGGER.trace("DoSendKeys '{}'", () -> keys);

		System.out.println("Send keys '" + keys + "' to browser.");
	}

	@Override
	public void doSetText(String xPath, String text) throws Exception {
		LOGGER.trace("DoSetText '{}'", () -> text);

		// TODO Auto-generated method stub
		System.out.println("Set text of '" + xPath + "' to '" + text + "'");
	}

	@Override
	public void doSleep(long waitTime) throws Exception {
		LOGGER.trace("DoSleep for '{}' Ms", () -> waitTime);

		// TODO Auto-generated method stub
		System.out.println("Sleep for '" + waitTime + "' Ms");
	}

	@Override
	public void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception {
		LOGGER.trace("doValidateElement '{}' ({}, {})", () -> xPath, () -> exists, () -> timeOut);

		// TODO Implement function
		System.out.println("Validate that element '" + xPath + "' " + (exists ? "" : "doesn't ") + "exists. "
				+ "TimeOut: " + timeOut);
	}

	@Override
	public void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception {
		LOGGER.trace("doValidateText '{}' ({}, {}, {})", () -> value, () -> exists, () -> timeOut, () -> xPath);

		// TODO Implement function
		System.out.println("Validate that text '" + value + "' " + (exists ? "" : "doesn't ") + "exists. "
				+ (xPath.length() > 0 ? "Element: " + xPath + ". " : "") + "TimeOut: " + timeOut);
	}

	@Override
	public String getName() {
		return "ScriptPrinter";
	}

	@Override
	public void doSwitchFrame(String xPath, Boolean alert, Boolean accept) throws Exception {
		LOGGER.trace("doValidateText '{}' ({}, {}, {})", () -> alert, () -> accept, () -> xPath);

		// TODO Implement function
		System.out.println("Swithing to frame '" + xPath + "' frame is alert?: " + (alert ? "Yes" : "No")
				+ "' accept or dismiss?: " + (accept ? "Accept" : "Dismiss"));
		// TODO Auto-generated method stub

	}

	@Override
	public void doSwitchScreen() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void doNavigate(String url) {
		// TODO Auto-generated method stub
		
	}
}
