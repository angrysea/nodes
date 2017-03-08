package org.amg.node.xmltools.xmlconverter;

import java.util.*;

public class XMLElementSerializer extends XmlBase {
	private Vector<XMLElementSerializer> childElements;
	private String name;
	private char data[];
	private int len;

	public XMLElementSerializer(String name, XMLElementSerializer parent) {
		childElements = null;
		this.name = null;
		data = null;
		len = 0;
		this.name = name;
		if (parent != null)
			parent.addChild(this);
	}

	private final void addChild(XMLElementSerializer child) {
		if (childElements == null)
			childElements = new Vector<XMLElementSerializer>();
		childElements.addElement(child);
	}

	final void setData(char buf[], int offset, int len) {
		this.len = len;
		data = new char[len];
		System.arraycopy(buf, offset, data, 0, len);
	}

	final void setData(String buf) {
		len = buf.length();
		data = new char[len];
		buf.getChars(0, len, data, 0);
	}

	final void update(HashMap<String, XMLElementSerializer> map, String type) {
		update(map, type, false);
	}

	final void update(HashMap<String, XMLElementSerializer> map, String type,
			boolean namerequired) {
		try {
			if (name.equals(type)) {
				String n = getProperty("name");
				if (n != null || !namerequired)
					map.put(n, this);
			}
			if (childElements != null) {
				for (Enumeration<XMLElementSerializer> e = childElements
						.elements(); e.hasMoreElements(); ((XMLElementSerializer) e
						.nextElement()).update(map, type))
					;
			}
		} catch (Exception e) {
			System.err.println("Exception thrown Update error ".concat(String
					.valueOf(String.valueOf(e.getMessage()))));
			e.printStackTrace();
		}
	}

	final Enumeration<XMLElementSerializer> children() {
		if (childElements != null)
			return childElements.elements();
		else
			return null;
	}

	final String getName() {
		return name;
	}
}
