package org.amg.node.scripting.scriptcompiler;

import java.util.HashSet;
import java.io.FileOutputStream;

public class Keyword implements Element {

	static HashSet<String> reservedWords = new HashSet<String>();

	public Keyword(String value) {
		this.value = value;
	}

	public void dump() {
		System.out.println("\t\tKeyword " + value);
	}

	public void compile(FileOutputStream o) {
		try {
			String line = new String("<Keyword name=\"");
			line += value;
			line += "\"/>\n";
			o.write(line.getBytes());
		} catch (Exception e) {
		}
	}

	public String getValue() {
		return value;
	}

	public static boolean isKeyWord(String word) {
		return reservedWords.contains(word);
	}

	private static void loadHash() {
		reservedWords.add("if");
		reservedWords.add("else");
		reservedWords.add("switch");
		reservedWords.add("case");
		reservedWords.add("break");
		reservedWords.add("return");
		reservedWords.add("continue");
		reservedWords.add("exit");
		reservedWords.add("do");
		reservedWords.add("while");
		reservedWords.add("goto");
		reservedWords.add("execute");
		reservedWords.add("element");
		reservedWords.add("attribute");
		reservedWords.add("var");
		reservedWords.add("content");
		reservedWords.add("for");
		reservedWords.add("default");
		reservedWords.add("new");
	}

	static {
		loadHash();
	}

	private String value;
}