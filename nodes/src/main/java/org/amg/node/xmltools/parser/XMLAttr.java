package org.amg.node.xmltools.parser;

public class XMLAttr {

	public XMLAttr(String name) {
		this.name = name;
	}

	public final String getNodeName() {
		return name;
	}

	public final String getNodeValue() {
		return value.getNodeValue();
	}

	public final void appendChild(XMLNode value) {
		this.value = value;
	}

	private String name = null;
	private XMLNode value = null;
}
