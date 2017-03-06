package org.adaptinet.node.logging.logger;

import static org.adaptinet.node.exception.LambdaExceptionUtil.rethrowConsumer;

import java.util.HashMap;
import java.util.Map;

import org.adaptinet.node.exception.BaseException;
import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class Logger {
	static final private String InvMsgHnd = "[LOG]Invalid Message Handle";
	static final private String NoLastMsg = "[LOG]No Last Message Available";
	private boolean bCached = false;
	private LogEntry lastEntry;
	private int lastMessageHandle = -1;
	private Map<Integer, LogEntry> logEntries = new HashMap<Integer, LogEntry>();
	private String logFile;
	private ILogger logger;
	//private int m_maxCachedEntries = 0;
	//private int m_minCachedEntries = 0;
	//private long m_storedWallclockTime = 0;

	public Logger(String strLogFile) throws LoggerException {
		logFile = strLogFile;
		try {
			logger = new LoggerFile(logFile);
			lastMessageHandle = getLastMessageHandle();
		} catch (Exception e) {
			throw new LoggerException(BaseException.SEVERITY_ERROR, BaseException.FACILITY_LOGGER, e.getMessage());
		}
	}

	public Logger(String strDSN, String strLogin, String strPassword, String strDB) throws LoggerException {
		try {
			logger = new LoggerDB(strLogin, strPassword, strDSN, strDB);
			lastMessageHandle = getLastMessageHandle();
		} catch (LoggerException e) {
			throw e;
		}
	}

	private final int cacheLogEntry(LogEntry anEntry) {
		int messageHandle;

		messageHandle = anEntry.messageHandle;
		logEntries.put(new Integer(messageHandle), anEntry);
		putLastLogEntry(anEntry);

		return messageHandle;
	}

	public void debug(String message) {
		try {
			logMessage(BaseException.SEVERITY_SUCCESS, // Severity=SUCCESS
					BaseException.FACILITY_LOGGER, // Facility=LOGGER
					LoggerException.LOG_INFOMSG, // ErrorCode=LOG_INFOMSG
					message, null);
		} catch (LoggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void info(String message) {
		try {
			logMessage(BaseException.SEVERITY_SUCCESS, // Severity=SUCCESS
					BaseException.FACILITY_LOGGER, // Facility=LOGGER
					LoggerException.LOG_INFOMSG, // ErrorCode=LOG_INFOMSG
					message, null);
		} catch (LoggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private final void flushCache() throws LoggerException {
		logEntries.values().stream().forEach(rethrowConsumer((e -> {
			try {
				logger.logMessage(e);
			} catch (LoggerException ex) {
				throw new LoggerException(BaseException.SEVERITY_ERROR, LoggerException.LOG_SQLERR, ex);
			}
		})));
	}

	private final LogEntry getLastLogEntry() {
		return lastEntry;
	}

	public String getLastMessage() throws LoggerException {
		String theMessage = NoLastMsg;
		LogEntry anEntry = getLastLogEntry();

		if (anEntry != null) {
			theMessage = getMessage(anEntry.messageHandle);
		}

		return theMessage;
	}

	public final int getLastMessageHandle() {
		if (lastMessageHandle == -1) {
			try {
				lastMessageHandle = logger.getLastMessageHandle();
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		return lastMessageHandle;
	}

	private final LogEntry getLogEntry(int messageHandle) throws LoggerException {
		LogEntry tmpEntry = null;

		if (bCached) {
			tmpEntry = (LogEntry) logEntries.get(new Integer(messageHandle));
		}

		if (tmpEntry == null) {
			try {
				tmpEntry = logger.getLogEntryHandle(messageHandle);
			} catch (LoggerException e) {
				throw e;
			}
		}
		return tmpEntry;
	}

	public String getMessage(int messageHandle) throws LoggerException {
		String theMessage = InvMsgHnd;

		LogEntry anEntry = getLogEntry(messageHandle);
		if (anEntry != null) {
			theMessage = toString(anEntry);
		}

		return theMessage;
	}

	public int logMessage(int severity, int facility, int errorCode, String errorMessage, String extraText)
			throws LoggerException {
		LogEntry anEntry = new LogEntry();

		anEntry.messageHandle = ++lastMessageHandle;
		anEntry.severity = severity;
		anEntry.facility = facility;
		anEntry.errorCode = errorCode;
		anEntry.errorMessage = errorMessage;
		anEntry.extraText = extraText;

		anEntry.println();

		try {
			if (bCached) {
				cacheLogEntry(anEntry);
			} else {
				writeLogEntry(anEntry);
			}
		} catch (LoggerException e) {
			throw e;
		}

		return anEntry.messageHandle;
	}

	public int logMessage(LogEntry anEntry) throws LoggerException {
		anEntry.messageHandle = ++lastMessageHandle;
		// anEntry.println();

		try {
			if (bCached) {
				cacheLogEntry(anEntry);
			} else {
				writeLogEntry(anEntry);
			}
		} catch (LoggerException e) {
			throw e;
		}
		return anEntry.messageHandle;
	}

	public int logMessage(String message) throws LoggerException {
		try {
			return logMessage(BaseException.SEVERITY_SUCCESS, // Severity=SUCCESS
					BaseException.FACILITY_LOGGER, // Facility=LOGGER
					LoggerException.LOG_INFOMSG, // ErrorCode=LOG_INFOMSG
					message, null);
		} catch (LoggerException e) {
			throw e;
		}
	}

	private final void putLastLogEntry(LogEntry anEntry) {
		lastEntry = anEntry;
	}

	@SuppressWarnings("unused")
	private final void removeLastLogEntry() {
		lastEntry = null;
	}

	public void SetCache(boolean bToggle) {
		bCached = bToggle;
	}

	private final String toString(LogEntry anEntry) {
		String theMessage;
		theMessage = BaseException.getSeverityText(anEntry.severity);
		theMessage += BaseException.getFacilityText(anEntry.facility);
		theMessage += "[" + new java.util.Date(anEntry.entryTime).toString() + "]";
		theMessage += anEntry.errorMessage;
		if (anEntry.extraText != null) {
			theMessage += " <" + anEntry.extraText + ">";
		}
		return theMessage;
	}

	private final int writeLogEntry(LogEntry anEntry) throws LoggerException {
		int iHandle;

		try {
			iHandle = logger.logMessage(anEntry);
			putLastLogEntry(anEntry);
		} catch (LoggerException LE) {
			throw LE;
		}

		return iHandle;
	}
};
