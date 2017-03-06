package org.adaptinet.node.scripting.scriptcompiler;

import java.util.Vector;
import java.util.Enumeration;
import java.io.FileOutputStream;

public class Statement implements Parent, Element {

	private Vector<Element> elements = new Vector<Element>();
	private boolean inMultiLineComment = false;

	public Statement(String data) {
		parseStatement(data);
	}

	public void parseStatement(String line) {
		if (line == null) {
			return;
		} else if (line.equals("") == false) {
			multiLineCommentFilter(line);
		}
	}

	private void multiLineCommentFilter(String line) {
		int index;
		if (inMultiLineComment && (index = line.indexOf("*/")) > -1
				&& !isInsideString(line, index)) {
			inMultiLineComment = false;
			index += 2;
			if (line.length() > index) {
				inlineCommentFilter(line.substring(index + 2));
			}
		} else if (inMultiLineComment) {
			// Do nothing
		} else if ((index = line.indexOf("/*")) > -1
				&& !isInsideString(line, index)) {
			inMultiLineComment = true;
			inlineCommentFilter(line.substring(0, index));
			index += 2;
			multiLineCommentFilter(line.substring(index));
		} else {
			inlineCommentFilter(line);
		}
	}

	private void inlineCommentFilter(String line) {
		int index;
		if ((index = line.indexOf("//")) > -1 && !isInsideString(line, index)) {
		} else {
			stringFilter(line);
		}
	}

	private void stringFilter(String line) {
		int start = 0;
		if ((start = line.indexOf("\"")) < 0) {
			parseGrammar(line);
			return;
		}

		if (start > 0) {
			parseGrammar(line.substring(0, start));
		}

		int index;
		boolean status = true;
		int next = start + 1;
		while (status) {
			if ((index = line.indexOf("\"", next)) > -1) {
				if (line.charAt(index - 1) == '\\') {
					next++;
					continue;
				}
				status = false;
				index++;
				elements.add(new Literal(line.substring(start, index)));
				if (line.length() > index) {
					stringFilter(line.substring(index));
				}
			}
		}
	}

	void parseGrammar(String statement) {

		int len = statement.length();
		char buff[] = statement.toCharArray();
		int start = -1;
		for (int i = 0; i < len; i++) {
			char ch = buff[i];
			// System.out.println(ch);
			if ((ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90)
					|| (ch >= 97 && ch <= 122)) {
				if (start < 0) {
					start = i;
				}
			} else {
				if (start > -1) {
					String word = statement.substring(start, i);
					if (Keyword.isKeyWord(word)) {
						elements.add(new Keyword(word));
					} else if (Literal.isLiteral(word)) {
						elements.add(new Literal(word));
					} else {
						elements.add(new Variable(word));
					}
					start = -1;
				}
				if (Operator.isOperator(ch)) {
					elements.add(new Operator(ch));
				}
			}
		}
		if (start > 0 && start < len) {
			String word = statement.substring(start, len);
			if (Keyword.isKeyWord(word)) {
				elements.add(new Keyword(word));
			} else if (Literal.isLiteral(word)) {
				elements.add(new Literal(word));
			} else {
				elements.add(new Variable(word));
			}
		}
	}

	private boolean isInsideString(String line, int position) {
		if (line.indexOf("\"") < 0) {
			return false;
		}
		int index;
		String left = line.substring(0, position);
		String right = line.substring(position);
		int leftCount = 0;
		int rightCount = 0;
		while ((index = left.indexOf("\"")) > -1) {
			leftCount++;
			left = left.substring(index + 1);
		}

		while ((index = right.indexOf("\"")) > -1) {
			rightCount++;
			right = right.substring(index + 1);
		}

		if (rightCount % 2 != 0 && leftCount % 2 != 0) {
			return true;
		} else {
			return false;
		}
	}

	public void insertChild(Element child) {
		elements.add(child);
	}

	public Enumeration<Element> children() {
		return elements.elements();
	}

	public boolean isBlock() {
		return false;
	}

	public void compile(FileOutputStream o) {
		try {
			o.write("<Statement>\n".getBytes());
			Enumeration<Element> e = elements.elements();
			while (e.hasMoreElements()) {
				Element element = e.nextElement();
				element.compile(o);
			}
			o.write("</Statement>\n".getBytes());
		} catch (Exception e) {
		}
	}

	public void dump() {
		System.out.println("\tStart Statement");
		Enumeration<Element> e = elements.elements();
		while (e.hasMoreElements()) {
			Element element = e.nextElement();
			element.dump();
		}
		System.out.println("\tEnd Statement");
	}
}