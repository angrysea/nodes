package org.adaptinet.node.xmltools.xmlconverter;

public class Element extends XmlBase {

	public Element() {
		elementType = null;
	}

	public Element(String e) {
		elementType = e;
	}

	public final void setElement(String e) {
		elementType = e;
	}

	public final String getElementType() {
		return elementType;
	}

	private String elementType;
}
