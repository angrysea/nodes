package org.amg.node.server;

import java.util.Properties;

public abstract class ServerProperties extends Properties {

	private static final long serialVersionUID = 821655720798197367L;

	static public ServerProperties getInstance(String name) {
		ServerProperties o = null;
		try {
			Class<?> c = Class.forName(name);
			o = (ServerProperties)c.newInstance();

		} catch (Exception e) {
			o = null;
		}
		return o;
	}
}
