package org.amg.node.scripting;

public class ScriptOperator extends ScriptElement {

	public static final char EQUAL = '=';
	public static final char INEQUALITY = '!';
	public static final char GREATER = '>';
	public static final char OR = '|';
	public static final char AND = '&';
	public static final char LESS = '<';
	public static final char OPENCURLY = '{';
	public static final char CLOSECURLY = '}';
	public static final char OPENPAREN = '(';
	public static final char CLOSEPAREN = ')';
	public static final char OPENBRACKET = '[';
	public static final char CLOSEBRACKET = ']';
	public static final char SEMICOLON = ';';
	public static final char COLON = ':';
	public static final char DOT = '.';
	public static final char PLUS = '+';
	public static final char MINUS = '-';
	public static final char MULTIPLY = '*';
	public static final char DIVIDE = '/';
	private static String operators = "=!>|&<{}()[];:.+-*/";

	public ScriptOperator(char value) {
		super(ScriptEngine.OPERATOR);
		this.value = value;
	}

	public char getValue() {
		return value;
	}

	private char value;

	public static int getType(char lit) {
		return operators.indexOf(lit) + 1;
	}

	public static boolean isOperator(char lit) {
		return operators.indexOf(lit) > -1;
	}

	public void dump(StringBuilder sb) {
		sb.append("\t\tOperator " + value);
	}

}