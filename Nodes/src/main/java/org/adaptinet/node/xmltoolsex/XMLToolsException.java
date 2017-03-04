package org.adaptinet.node.xmltoolsex;

public class XMLToolsException extends BaseException {

	private static final long serialVersionUID = 7806704261180684620L;

	public XMLToolsException(int hr) {
		super(hr);
	}

	public XMLToolsException(int sev, int fac, int code) {
		super(sev, fac, code);
	}

	public XMLToolsException(int sev, int fac, int code, String msg) {
		super(sev, fac, code, msg);
	}

	public XMLToolsException(int sev, int code) {
		super(sev, code);
	}

	public void logMessage(String msg) {
		try {
			if (bVerbose == true) {
				System.out.println("LogEntry:");
				System.out.println("\tseverity="
						+ XMLToolsException.getSeverityText(getSeverity()));
				System.out.println("\tfacility="
						+ XMLToolsException.getFacilityText(getFacility()));
				System.out.println("\terrorCode=" + getCode());
				System.out.println("\terrorMessage=" + getMessage());
				System.out.println("\textraText=" + msg);
				System.out.println("\tentryTime=" + exceptionDate.toString());
				System.out.println("");
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
