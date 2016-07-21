package nl.dictu.prova.plugins.output;

import java.net.URL;
import java.util.Map;

public interface SoapOutputPlugin extends OutputPlugin {
	public Map<Object, Object> doProcessResponse (String responseMessage) throws Exception;
	
	public String doSetLogin (String user, String password) throws Exception;
	
	public String doSendMessage (Map<Object, Object> soapProperties) throws Exception;
	
	public URL doSetUrl (String url) throws Exception;
}