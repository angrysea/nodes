package org.amg.node.scripting;

public class ScriptVariable extends ScriptElement implements ScriptValue {

	private String name = null;
	private Object value = null;
	
	public ScriptVariable(String name) {
		super(ScriptEngine.VARIABLE);
		this.name = name;
	}

	final public String getName() {
		return name;
	}

	final public void setName(String name) {
		this.name = name;
	}

	final public void setObject(Object value) {
		this.value = value;
	}

	final public Object getObject() {
		return this.value;
	}

	final public boolean equals(ScriptValue s) {
		return this.value.equals(s.getObject());
	}

	final public int compareTo(Object s) {
		int ret = 0;
		String str = s.toString();
		if (value != null && str != null) {
			ret = value.toString().compareTo(str);
		} else if (value != null && str == null) {
			ret = 1;
		} else if (value == null && str != null) {
			ret = -1;
		}
		return ret;
	}

	final public String toString() {
		return value.toString();
	}

	public void dump(StringBuilder sb) {
		sb.append("\t\tvariable " + name);
	}

}