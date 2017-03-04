package org.adaptinet.node.server;

import java.util.Properties;

public class ServerInfo {
	static public ServerProperties properties;

	static public boolean bVerbose = false;

	static public boolean bAutoReload = false;

	public static final String VERSION = "Adaptinet Server version 1.0";

	static public Properties getServerProperty() {
		return properties;
	}

	static public String getServerProperty(String name) {
		return properties.getProperty(name);
	}

}
