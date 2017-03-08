package org.amg.node.exception;

public class LoggerException extends AMGException {
	private static final long serialVersionUID = 1L;
	public final static int LOG_BASE = 0;
	public final static int LOG_INFOMSG = LOG_BASE + 1;
	public final static int LOG_SQLERR = LOG_BASE + 2;
	public final static int MONITOR_ACCESSDENIED = LOG_BASE + 3;

	public LoggerException(final int sev, final int code) {
		super(sev, FACILITY_LOGGER, code);
	}

	public LoggerException(final int sev, final int code, final String xT) {
		super(sev, FACILITY_LOGGER, code);
		setExtraText(xT);
	}

	public LoggerException(final int severityError, final int logSqlerr, final LoggerException ex) {
		super(severityError, FACILITY_LOGGER, logSqlerr);
		setExtraText(ex.getErrorCode() + ": " + ex.getMessage());
	}

	@Override
	public final String getMessageInternal(final int e) {
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
}