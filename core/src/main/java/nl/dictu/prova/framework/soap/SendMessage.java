package nl.dictu.prova.framework.soap;

import java.net.URL;
import java.util.Properties;
import nl.dictu.prova.framework.TestAction;

public class SendMessage extends TestAction {
    
        private final String ATTR_URL = "URL";
        private final String ATTR_MESSAGE = "MESSAGE";
        private final String ATTR_AUTHORIZATION = "AUTHORIZATION";
    
        private URL url = null;
        private String authorization = null;
        private String message = null;
        Properties soapProperties = null;

	@Override
	public void setAttribute(String key, String value) throws Exception {
            switch(key){
                case ATTR_URL:              url = new URL(value);
                case ATTR_MESSAGE:          message = value;
                case ATTR_AUTHORIZATION:    authorization = value;
            }
	}

	@Override
	public void execute() throws Exception {
            soapProperties = new Properties();
            if(!isValid()){
                throw new Exception("Attributes url, message and authorization not properly set!");
            }
            soapProperties.put("url", url);
            soapProperties.put("authorization", authorization);
            soapProperties.put("message", message);
            String soapResponse = this.testRunner.getSoapActionPlugin().doSendMessage(soapProperties);
            Properties responseProperties = (Properties) this.testRunner.getSoapActionPlugin().doProcessResponse(soapResponse);
	}

	@Override
	public boolean isValid() throws Exception {
            if (url == null) return false;
            if (message == null) return false;
            if (authorization == null) return false;
            return true;
	}
}
