package org.adaptinet.node.scripting;

public class ScriptMethod extends ScriptElement {

	private String name = null;
	
	public ScriptMethod(String name) {
		super(ScriptEngine.METHOD);
		this.name = name;
	}

	final public String getName() {
		return name;
	}

	final public void setName(String name) {
		this.name = name;
	}

	final public void setObject(Object value) {
	}

	final public Object getObject() {
		return null;
	}

	final public String toString() {
		return name.toString();
	}

	public void dump(StringBuilder sb) {
		sb.append("\t\tvariable " + name);
	}

}