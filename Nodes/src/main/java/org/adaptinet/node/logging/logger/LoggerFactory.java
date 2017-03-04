package org.adaptinet.node.logging.logger;

import java.util.HashMap;
import java.util.Map;

import org.adaptinet.node.exception.LoggerException;

public class LoggerFactory {
	static public Map loggers = new HashMap<String, Logger>();

	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}
	
	public static Logger getLogger(String loggerName) {
		Logger aLogger = (Logger) loggers.get(loggerName);
		if(aLogger==null) {
			try {
				aLogger = new Logger(" ");
			} catch (LoggerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return aLogger;
	}
}
