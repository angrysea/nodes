package org.amg.node.scripting;

public abstract class ScriptElement {
	
	public ScriptElement(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	abstract public void dump(StringBuilder sb);

	private int type = 0;
}