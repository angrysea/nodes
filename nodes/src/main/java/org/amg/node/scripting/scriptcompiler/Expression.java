package org.amg.node.scripting.scriptcompiler;

import java.util.StringTokenizer;
import java.io.FileOutputStream;

public class Expression implements Element {

	private transient boolean inMultiLineComment = false;
	private Expression left = null;
	private Expression right = null;
	private Element element = null;

	public Expression() {
	}

	public Expression(Element e) {
		element = e;
	}

	public void parse(StringTokenizer tokenizer) {
		if (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token != null)
				multiLineCommentFilter(token);
		}

		if (tokenizer.hasMoreTokens()) {
			Expression exp = new Expression();
			right = exp;
			exp.parse(tokenizer);
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
			// Do nothing
		} else
			stringFilter(line);
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
				insertChild(new Expression(new Literal(line.substring(start,
						index))));
				if (line.length() > index) {
					stringFilter(line.substring(index));
				}
			}
		}
	}

	private void parseGrammar(String statement) {
		char[] buff = statement.toCharArray();
		int len = buff.length;
		int start = -1;
		Expression expression = null;

		for (int i = 0; i < len; i++) {
			char ch = buff[i];
			// System.out.println(ch);
			if ((ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90)
					|| (ch >= 97 && ch <= 122)) {
				if (start < 0)
					start = i;
			} else {
				if (start > -1) {
					String word = statement.substring(start, i);
					expression = createExpression(word);
					insertChild(expression);
					start = -1;
				}
				if (Operator.isOperator(ch)) {
					try {
						expression = new Expression(new Operator(ch));
						insertChild(expression);
					} catch (ClassCastException e) {
					}
				}
			}
		}

		if (start > 0 && start < len) {
			String word = statement.substring(start, len);
			expression = createExpression(word);
			insertChild(expression);
		}
	}

	private Expression createExpression(String word) {
		Expression expression = null;
		if (Keyword.isKeyWord(word)) {
			expression = new Expression(new Keyword(word));
		} else if (Literal.isLiteral(word)) {
			expression = new Expression(new Literal(word));
		} else {
			expression = new Expression(new Variable(word));
		}
		return expression;
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

	private void insertChild(Expression child) {
		if (left == null) {
			left = child;
		} else {
			left.insertExpression(child);
		}
	}

	private void insertExpression(Expression exp) {
		if (left == null) {
			left = exp;
		} else if (right == null) {
			right = exp;
		} else if (left.element == null) {
			left.insertExpression(exp);
		} else if (right.element == null) {
			right.insertExpression(exp);
		}
	}

	public boolean isElement() {
		return element != null;
	}

	public void compile(FileOutputStream o) {
		try {
			if (left != null || element != null || right != null) {
				o.write("<Expression>\n".getBytes());
				if (element != null)
					element.compile(o);
				else if (left != null)
					left.compile(o);
				if (right != null)
					right.compile(o);
				o.write("</Expression>\n".getBytes());
			}
		} catch (Exception e) {
		}
	}

	public void dump() {
		try {
			if (left != null && right != null) {
				System.out.println("\tStart Expression");
				if (left != null)
					left.dump();
				if (right != null)
					right.dump();
				System.out.println("\tEnd Expression");
			}
		} catch (Exception e) {
		}
	}

}