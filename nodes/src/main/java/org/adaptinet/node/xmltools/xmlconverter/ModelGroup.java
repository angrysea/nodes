package org.adaptinet.node.xmltools.xmlconverter;

import java.util.ArrayList;

public class ModelGroup extends XmlBase {

	public ModelGroup() {
		elems = new ArrayList<ElementData>();
	}

	public final void putElement(ElementData data) {
		elems.add(data);
	}

	public final ArrayList<ElementData> getElments() {
		return elems;
	}

	private ArrayList<ElementData> elems;
}
