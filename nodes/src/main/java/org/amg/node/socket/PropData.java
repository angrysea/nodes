package org.amg.node.socket;

public class PropData {

	String name, id, state;

	public PropData(String name, String id, String state) {
		this.name = name;
		this.id = id;
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

}