package nl.dictu.prova.plugins.output;

public interface WebserviceOutputPlugin extends OutputPlugin {
	public void doProcessResponse (String responseMessage) throws Exception;
	
	public void doSetLogin (String user, String password) throws Exception;
	
	public void doSendMessage (String requestMessage) throws Exception;
	
	public void doSetUrl (String url) throws Exception;
}
