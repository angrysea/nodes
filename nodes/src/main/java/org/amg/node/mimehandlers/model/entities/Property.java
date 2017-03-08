package org.amg.node.mimehandlers.model.entities;

public class Property {

	public Source getSource() { 
		return _source;
	}
	public void setSource(Source newValue) { 
		_source = newValue;
	}
	private Source _source = new Source();
}
