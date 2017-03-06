package org.adaptinet.node.exception;

public class LoggerException extends AdaptinetException {
	private static final long serialVersionUID = 1L;

	public LoggerException(int sev, int code) {
		super(sev, FACILITY_LOGGER, code);
	}

	public LoggerException(int sev, int code, String xT) {
		super(sev, FACILITY_LOGGER, code);
		setExtraText(xT);
	}

	public LoggerException(int severityError, int logSqlerr, LoggerException ex) {
		super(severityError, FACILITY_LOGGER, logSqlerr);
		setExtraText(ex.getErrorCode() + ": " + ex.getMessage());
	}

	@Override
	public final String getMessageInternal(int e) {
		String errorMessage = new String("[LOG]");

		switch (e) {
		case MONITOR_ACCESSDENIED:
			errorMessage += "Access Denied for Monitor";
			break;

		case LOG_INFOMSG:
			errorMessage += "Informational message being logged";
			break;

		case LOG_SQLERR:
			errorMessage += "SQL Error caught in Logger";
			break;

		default:
			errorMessage += "Unknown Logger error code";
			break;
		}

		return errorMessage;
	}

	// Public static members defining Error codes
	public final static int LOG_BASE = 0;
	public final static int LOG_INFOMSG = LOG_BASE + 1;
	public final static int LOG_SQLERR = LOG_BASE + 2;
	public final static int MONITOR_ACCESSDENIED = LOG_BASE + 3;
}