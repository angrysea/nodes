package org.adaptinet.node.logging.logger;

import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public interface ILogger {
	public int getLastMessageHandle();
	public LogEntry getLogEntryHandle(int messageHandle) throws LoggerException;
	public int logMessage(LogEntry le) throws LoggerException;
}
