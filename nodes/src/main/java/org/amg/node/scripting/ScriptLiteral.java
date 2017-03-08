package org.amg.node.scripting;

public class ScriptLiteral extends ScriptElement implements ScriptValue {

	public void setName(String name) {
	}

	public String getName() {
		return "literal";
	}

	public ScriptLiteral(String value) {
		super(ScriptEngine.LITERAL);
		setObject(value);
	}

	final public boolean equals(ScriptValue s) {
		return this.value.equals(s.getObject());
	}

	final public int compareTo(Object s) {
		int ret = 0;
		String str = s.toString();
		if (value != null && str != null) {
			ret = value.compareTo(s.toString());
		} else if (value != null && str == null) {
			ret = 1;
		} else if (value == null && str != null) {
			ret = -1;
		}
		return ret;
	}

	public Object getObject() {
		return this.value;
	}

	public void setObject(Object value) {
		try {
			String s = (String) value;
			int len = s.length();
			if (len > 2 && s.indexOf("\"") > -1) {
				this.value = s.substring(1, s.length() - 1);
			} else {
				this.value = s;
			}
		} catch (Exception e) {
		}
	}

	final public String toString() {
		return value;
	}

	private String value;

	public static boolean isLiteral(String word) {
		boolean results = false;
		if ((word.charAt(0) == '"' && word.charAt(word.length() - 1) == '"')
				|| isNumeric(word)) {
			results = true;
		}

		return results;
	}

	public static boolean isNumeric(String word) {
		int len = word.length();
		char buff[] = word.toCharArray();
		for (int i = 0; i < len; i++) {
			char ch = buff[i];
			// System.out.println(ch);
			if (ch < 48 || ch > 57)
				return false;
		}
		return true;
	}

	public void dump(StringBuilder sb) {
		sb.append("\t\tLiteral " + value);
	}
}