package org.adaptinet.node.mimehandlers.model.entities;

public class ControllerMethod {

	public void setName(String newValue) {
		_name = newValue!=null ? newValue.toCharArray() : null;
	}
	public String getName() {
		if(_name!=null) {
			return new String(_name);
		}
		else {
			return null;
		}
	}
	public void setRequestName(String newValue) {
		_requestName = newValue!=null ? newValue.toCharArray() : null;
	}
	public String getRequestName() {
		if(_requestName!=null) {
			return new String(_requestName);
		}
		else {
			return null;
		}
	}
	private char[] _name;
	private char[] _requestName;
}
