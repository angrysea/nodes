package org.amg.node.mimehandlers.model.entities;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Destination implements Serializable {

	public void setId(String _id) {
		this._id = _id;
	}
	public String getId() {
		return _id;
	}

	public void setProperty(Property _property) {
		this._property = _property;
	}
	public Property getProperty() {
		return _property;
	}

	private Property _property = null;
	private String _id = new String();
}
