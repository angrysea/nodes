package org.amg.node.logging.logserver;

import java.io.PrintStream;

import org.amg.node.exception.LoggerException;
import org.amg.node.logging.loggerutils.BroadcastMessage;
import org.amg.node.logging.loggerutils.LogEntry;
import org.amg.node.logging.loggerutils.ServerMessage;

public class LogServer {
	private String logFileName;
	
	public PrintStream getConsoleStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void initServer(String logFile) throws LoggerException {
		if(logFile.isEmpty()) {
			logFileName = "amgmg.log";
		}
		logFileName = logFile;
	}

	public void killServer() {
		// TODO Auto-generated method stub
		
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public void RemoveReader(Reader reader) {
		// TODO Auto-generated method stub
		
	}

	public void writeToLog(LogEntry le) {
		// TODO Auto-generated method stub
		
	}

	public void updateBroadcastList(BroadcastMessage o) {
		// TODO Auto-generated method stub
		
	}

	public void processServerMessage(ServerMessage o) {
		// TODO Auto-generated method stub
		
	}
}
