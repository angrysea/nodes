package org.adaptinet.node.xmltools.xmlconverter;

import java.util.Properties;

public class XmlBase {

	protected Properties properties;
	private boolean bMaxOne;
	private boolean bOptional;
	private int maxOccurs;
	
	public XmlBase() {
		properties = new Properties();
		bMaxOne = true;
		bOptional = false;
	}

	public final void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public final String getProperty(String key) {
		return properties.getProperty(key);
	}

	public final void putAll(XmlBase b) {
		properties.putAll(b.properties);
	}

	public final boolean getMaxOne() {
		return bMaxOne;
	}

	public final void setMaxOne(boolean newValue) {
		bMaxOne = newValue;
	}

	public final void setOptional(boolean newValue) {
		bOptional = newValue;
	}

	public final boolean getOptional() {
		return bOptional;
	}

	public final int getMaxOccurs() {
		return maxOccurs;
	}

	public final void setMaxOccurs(int newValue) {
		maxOccurs = newValue;
	}
}
