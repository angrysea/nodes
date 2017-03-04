package org.adaptinet.node.logging.logserver;

import java.io.PrintStream;

import org.adaptinet.node.exception.LoggerException;

public class LogServer {
	private String logFileName;
	public void initServer(String logFile) throws LoggerException {
		if(logFile.isEmpty()) {
			logFileName = "adaptinet.log";
		}
		logFileName = logFile;
	}

	public void killServer() {
		// TODO Auto-generated method stub
		
	}

	public PrintStream getConsoleStream() {
		// TODO Auto-generated method stub
		return null;
	}

}
