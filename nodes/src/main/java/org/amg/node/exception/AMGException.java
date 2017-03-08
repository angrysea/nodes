package org.amg.node.exception;

public class AMGException extends BaseException {

	private static final long serialVersionUID = -1400441403453456334L;

	public AMGException(final int hr) {
		super(hr);
	}

	public AMGException(final int sev, final int fac, final int code) {
		super(sev, fac, code);
	}

	public AMGException(final int sev, final int fac, final int code, final String msg) {
		super(sev, fac, code, msg);
	}

	public AMGException(final int sev, final int code) {
		super(sev, code);
	}

	@Override
	public void logMessage(final String msg) {
		try {
			if (bVerbose == true) {
				System.out.println("LogEntry:");
				System.out.println("\tseverity="
						+ BaseException.getSeverityText(getSeverity()));
				System.out.println("\tfacility="
						+ BaseException.getFacilityText(getFacility()));
				System.out.println("\terrorCode=" + getCode());
				System.out.println("\terrorMessage=" + getMessage());
				System.out.println("\textraText=" + msg);
				System.out.println("\tentryTime=" + exceptionDate.toString());
				System.out.println("");
			}
		} catch (final Exception e) {
			System.err.println(e);
		}
	}

}
