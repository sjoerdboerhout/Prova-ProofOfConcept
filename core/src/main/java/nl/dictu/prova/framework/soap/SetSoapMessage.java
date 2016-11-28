package nl.dictu.prova.framework.soap;

import nl.dictu.prova.framework.TestAction;

public class SetSoapMessage extends TestAction {
    
        private final String ATTR_MESSAGE = "QUERY";
        private String message = null;

	@Override
	public void setAttribute(String key, String value) throws Exception {
            switch(key){
                case ATTR_MESSAGE:          message = value;
            }
	}

	@Override
	public void execute() throws Exception {
            if(!isValid()){
                throw new Exception("Message or testRunner not properly set!");
            }
            
            this.testRunner.getSoapActionPlugin().doSetMessage(message);
	}

	@Override
	public boolean isValid() throws Exception {
            if (message == null) return false;
            if (testRunner == null) return false;
            return true;
	}
  
  /**
   * Return a string representation of the objects content
   *
   * @return
   */
  @Override
  public String toString()
  {
    int length = message.length() < 120 ? message.length() : 120;
    return ("'" + this.getClass().getSimpleName().toUpperCase() + "': " + message.substring(0, length) + "'");
  }
}
