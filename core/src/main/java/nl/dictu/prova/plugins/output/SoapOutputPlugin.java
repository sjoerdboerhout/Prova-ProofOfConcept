package nl.dictu.prova.plugins.output;

import java.util.Properties;

public interface SoapOutputPlugin extends OutputPlugin {
	public Properties doProcessResponse () throws Exception;
	
	public void doSetProperties (String user, String password, String url, String prefix) throws Exception;
	
	public void doSetMessage (String message) throws Exception;
	
	public boolean doTest (String key, String value) throws Exception;
        
  public String doGetCurrentPrefix () throws Exception;
}