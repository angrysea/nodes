package org.amg.node.xmltools.parser;

import java.util.Vector;
import java.util.Enumeration;

//@SuppressWarnings("unchecked")
public class Attributes {

	private Vector<AttributeEntry> attributes = new Vector<AttributeEntry>();

	public Attributes() {
	}

	public void addAttribute(String uri, String localName, String qName,
			String type, String value) {
		AttributeEntry entry = new AttributeEntry();
		entry.uri = uri;
		entry.localName = localName;
		entry.qName = qName;
		entry.type = type;
		entry.value = value;
		attributes.add(entry);
	}

	public void clear() {
		attributes.clear();
	}

	public int getIndex(String qName) {
		int index = -1;
		int count = 0;
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if (qName.equals(entry.qName)) {
				index = count;
				break;
			}
			count++;
		}
		return index;
	}

	public int getIndex(String uri, String localName) {
		int index = -1;
		int count = 0;
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if ((localName.equals(entry.localName))
					&& ((uri != null && entry.uri != null && uri
							.equals(entry.uri)) || (uri == null && entry.uri == null))) {
				index = count;
				break;
			}
			count++;
		}
		return index;
	}

	public int getLength() {
		return attributes.size();
	}

	public String getLocalName(int index) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		return entry.localName;
	}

	public String getQName(int index) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		return entry.qName;
	}

	public String getType(int index) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		return entry.type;
	}

	public String getType(String qName) {
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if (qName.equals(entry.qName)) {
				return entry.type;
			}
		}
		return null;
	}

	public String getType(String uri, String localName) {
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if ((localName.equals(entry.localName))
					&& ((uri != null && entry.uri != null && uri
							.equals(entry.uri)) || (uri == null && entry.uri == null))) {
				return entry.type;
			}
		}
		return null;
	}

	public String getURI(int index) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		return entry.uri;
	}

	public String getValue(int index) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		return entry.value;
	}

	public String getValue(String qName) {
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if (qName.equals(entry.qName)) {
				return entry.value;
			}
		}
		return null;
	}

	public String getValue(String uri, String localName) {
		AttributeEntry entry = null;
		Enumeration<AttributeEntry> e = attributes.elements();
		while (e.hasMoreElements()) {
			entry = (AttributeEntry) e.nextElement();
			if ((localName.equals(entry.localName))
					&& ((uri != null && entry.uri != null && uri
							.equals(entry.uri)) || (uri == null && entry.uri == null))) {
				return entry.value;
			}
		}
		return null;
	}

	public void removeAttribute(int index) {
	}

	public void setAttribute(int index, String uri, String localName,
			String qName, String type, String value) {
		AttributeEntry entry = new AttributeEntry();
		entry.uri = uri;
		entry.localName = localName;
		entry.qName = qName;
		entry.type = type;
		entry.value = value;
		attributes.insertElementAt(entry, index);
	}

	public void setAttributes(Attributes atts) {
	}

	public void setLocalName(int index, String localName) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		entry.localName = localName;
	}

	public void setQName(int index, String qName) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		entry.qName = qName;
	}

	public void setType(int index, String type) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		entry.type = type;
	}

	public void setURI(int index, String uri) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		entry.uri = uri;
	}

	public void setValue(int index, String value) {
		AttributeEntry entry = (AttributeEntry) attributes.elementAt(index);
		entry.value = value;
	}

	class AttributeEntry {
		String uri = null;
		String localName = null;
		String qName = null;
		String type = null;
		String value = null;
	}

}
