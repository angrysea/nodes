package org.adaptinet.node.logging.logserver;

import java.io.PrintStream;

import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.BroadcastMessage;
import org.adaptinet.node.logging.loggerutils.LogEntry;
import org.adaptinet.node.logging.loggerutils.ServerMessage;

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
			logFileName = "adaptinet.log";
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
