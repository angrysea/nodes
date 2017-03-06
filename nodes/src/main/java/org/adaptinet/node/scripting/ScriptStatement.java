package org.adaptinet.node.scripting;

import java.util.Vector;
import java.util.Enumeration;

//@SuppressWarnings("unchecked")
public class ScriptStatement extends ScriptElement implements ScriptParent {

	private Vector<ScriptElement> elements = new Vector<ScriptElement>();
	private boolean inMultiLineComment = false;
	
	public ScriptStatement(String data) {
		super(ScriptEngine.STATEMENT);
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
				elements.add(new ScriptLiteral(line.substring(start, index)));
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
					if (ScriptKeyword.isKeyWord(word)) {
						elements.add(new ScriptKeyword(word));
					} else if (ScriptLiteral.isLiteral(word)) {
						elements.add(new ScriptLiteral(word));
					} else {
						if(i < len && ch==40) {
							elements.add(new ScriptMethod(word));
						}
						else {
							elements.add(new ScriptVariable(word));
						}
					}

					start = -1;
				}
				if (ScriptOperator.isOperator(ch)) {
					elements.add(new ScriptOperator(ch));
				}
			}
		}
		if (start > 0 && start < len) {
			String word = statement.substring(start, len);
			if (ScriptKeyword.isKeyWord(word)) {
				elements.add(new ScriptKeyword(word));
			} else if (ScriptLiteral.isLiteral(word)) {
				elements.add(new ScriptLiteral(word));
			} else {
				elements.add(new ScriptVariable(word));
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

	public void insertChild(ScriptElement child) {
		elements.add(child);
	}

	public Enumeration<ScriptElement> children() {
		return elements.elements();
	}

	public boolean isBlock() {
		return false;
	}

	public void dump(StringBuilder sb) {
		sb.append("\tStart Statement");
		Enumeration<ScriptElement> e = elements.elements();
		while (e.hasMoreElements()) {
			ScriptElement element = e.nextElement();
			element.dump(sb);
		}
		sb.append("\tEnd Statement");
	}
}