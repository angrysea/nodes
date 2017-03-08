package org.amg.node.logging.logger;

import org.amg.node.exception.LoggerException;
import org.amg.node.logging.loggerutils.LogEntry;

public interface ILogger {
	public int getLastMessageHandle();
	public LogEntry getLogEntryHandle(int messageHandle) throws LoggerException;
	public int logMessage(LogEntry le) throws LoggerException;
}
