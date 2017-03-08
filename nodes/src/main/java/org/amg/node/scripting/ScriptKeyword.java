package org.amg.node.scripting;

import java.util.HashMap;

public class ScriptKeyword extends ScriptElement {

	static private HashMap<String, String> reservedWords = new HashMap<String, String>();
	private String value;

	public ScriptKeyword(String value) {
		super(ScriptEngine.KEYWORD);
		this.value = value;
	}

	public void dump(StringBuilder sb) {
		sb.append("\t\tKeyword " + value);
	}

	public String getValue() {
		return value;
	}

	public static boolean isKeyWord(String word) {
		return reservedWords.containsKey(word);
	}

	static public int getKeywordType(String word) {
		return Integer.parseInt((String) reservedWords.get(word));
	}

	private static void loadHash() {
		reservedWords.put("if", "201");
		reservedWords.put("else", "202");
		reservedWords.put("switch", "203");
		reservedWords.put("case", "204");
		reservedWords.put("break", "205");
		reservedWords.put("return", "206");
		reservedWords.put("continue", "207");
		reservedWords.put("exit", "208");
		reservedWords.put("do", "209");
		reservedWords.put("while", "210");
		reservedWords.put("goto", "211");
		reservedWords.put("execute", "212");
		reservedWords.put("element", "213");
		reservedWords.put("attribute", "214");
		reservedWords.put("var", "215");
		reservedWords.put("content", "216");
		reservedWords.put("for", "217");
		reservedWords.put("default", "218");
		reservedWords.put("new", "219");
	}

	static {
		loadHash();
	}

}