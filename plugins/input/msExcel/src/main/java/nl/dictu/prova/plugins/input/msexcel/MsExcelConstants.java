package nl.dictu.prova.plugins.input.msexcel;

import java.util.ArrayList;
import java.util.List;

public class MsExcelConstants {

	public static final List<String> reservedParameters = new ArrayList<>();
	static {
		reservedParameters.add("package");
		reservedParameters.add("parameter");
		reservedParameters.add("opmerking");
		reservedParameters.add("keys");
		reservedParameters.add("actie");
		reservedParameters.add("idx");
		reservedParameters.add("locator");
		reservedParameters.add("text");
		reservedParameters.add("test");
	}

}
