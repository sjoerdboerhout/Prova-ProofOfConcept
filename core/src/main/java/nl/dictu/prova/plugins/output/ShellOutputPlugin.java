package nl.dictu.prova.plugins.output;

public interface ShellOutputPlugin extends OutputPlugin {

	void doCaptureScreen(String fileName) throws Exception;

	void doClick(String xPath, Boolean rightClick, Boolean waitUntilPageLoaded) throws Exception;

	void doDownloadFile(String url, String saveAs) throws Exception;

	void doSelect(String xPath, Boolean select) throws Exception;

	void doSendKeys(String keys) throws Exception;

	void doSetText(String xPath, String text) throws Exception;

	void doSleep(long waitTime) throws Exception;

	void doValidateElement(String xPath, Boolean exists, double timeOut) throws Exception;

	void doValidateText(String xPath, String value, Boolean exists, double timeOut) throws Exception;

}
