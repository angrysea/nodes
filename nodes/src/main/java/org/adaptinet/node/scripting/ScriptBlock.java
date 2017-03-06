package org.adaptinet.node.scripting;

import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class ScriptBlock extends ScriptElement implements ScriptParent {

	private Vector<ScriptElement> elements = new Vector<ScriptElement>();

	public ScriptBlock() {
		super(ScriptEngine.BLOCK);
	}

	public void insertStatements(String data) {
		StringTokenizer tokenizer = new StringTokenizer(data, ";");
		while (tokenizer.hasMoreTokens()) {
			elements.add(new ScriptStatement(tokenizer.nextToken()));
		}
	}

	public void insertChild(ScriptElement child) {
		elements.add(child);
	}

	public Enumeration<ScriptElement> children() {
		return elements.elements();
	}

	public boolean isBlock() {
		return true;
	}

	public void dump(StringBuilder sb) {
		sb.append("Start Block");
		Enumeration<ScriptElement> e = elements.elements();
		while (e.hasMoreElements()) {
			ScriptElement element = (ScriptElement) e.nextElement();
			element.dump(sb);
		}
		sb.append("End Block");
	}

}