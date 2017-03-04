package org.adaptinet.node.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SimpleProperties extends ServerProperties {

	private static final long serialVersionUID = 6812589245811341796L;

	private String name;

	public SimpleProperties() {
		this("lastrun.properties");
	}

	public SimpleProperties(String name) {
		try {
			load(new FileInputStream(name));
			this.name = name;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public void bookMark() {
		try {
			store(new FileOutputStream(name), "Server Current Properties");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
}