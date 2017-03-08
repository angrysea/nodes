package org.amg.node.xmltools.xmlconverter;

public class ElementData extends XmlBase {

	public ElementData() {
		elementTagType = null;
		elementType = null;
		elementTagType = null;
	}

	public ElementData(String e) {
		elementTagType = null;
		elementType = e;
		elementTagType = e;
	}

	public ElementData(String name, String tag) {
		elementTagType = null;
		elementType = name;
		elementTagType = tag;
	}

	public final void setElement(String e) {
		elementType = e;
	}

	public final String getElementType() {
		return elementType;
	}

	public final void setElementTagType(String e) {
		elementTagType = e;
	}

	public final String getElementTypeTag() {
		return elementTagType;
	}

	private String elementType;
	private String elementTagType;
}
