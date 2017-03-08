package org.amg.node.scripting.scriptcompiler;

import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.FileOutputStream;

public class Block implements Parent, Element {

	private Vector<Element> elements = new Vector<Element>();

	public Block() {
	}

	public void insertStatements(String data) {
		StringTokenizer tokenizer = new StringTokenizer(data, ";");
		while (tokenizer.hasMoreTokens()) {
			elements.add(new Statement(tokenizer.nextToken()));
		}
	}

	public void insertChild(Element child) {
		elements.add(child);
	}

	public Enumeration<Element> children() {
		return elements.elements();
	}

	public boolean isBlock() {
		return true;
	}

	public void compile(FileOutputStream o) {
		try {
			o.write("<Block>\n".getBytes());
			Enumeration<Element> e = elements.elements();
			while (e.hasMoreElements()) {
				Element element = (Element) e.nextElement();
				element.compile(o);
			}
			o.write("</Block>\n".getBytes());
		} catch (Exception e) {
		}
	}

	public void dump() {
		System.out.println("Start Block");
		Enumeration<Element> e = elements.elements();
		while (e.hasMoreElements()) {
			Element element = (Element) e.nextElement();
			element.dump();
		}
		System.out.println("End Block");
	}
}