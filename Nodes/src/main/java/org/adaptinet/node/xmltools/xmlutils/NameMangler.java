package org.adaptinet.node.xmltools.xmlutils;

import java.lang.StringBuffer;

public class NameMangler {
	public NameMangler() {
	}

	public static String encode(String name) {
		if (name == null) {
			return null;
		}

		StringBuffer buff = null;
		int pos = -1;

		try {
			for (int i = 0; i < disallowedChars.length; i++) {
				while ((pos = name.indexOf(disallowedChars[i], pos + 1)) != -1) {
					if (buff == null) {
						buff = new StringBuffer(name.length() + 20);
						buff.append(name);
						buff.append(MANGLERCHAR);
					}

					buff.setCharAt(pos, REPLACECHAR);
					appendCharCode(buff, pos, name.charAt(pos));
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println(e.getMessage());
			}
		}

		if (buff != null) {
			buff.append(MANGLERCHAR);
			return buff.toString();
		} else {
			return name;
		}
	}

	public static String decode(String name) {
		if (name == null) {
			return null;
		}

		try {
			if (name.charAt(0)=='_') {
				name = name.substring(1, name.length());
			}
			
			if (name.charAt(name.length() - 1) == MANGLERCHAR) {
				int end = name.length() - 2;
				int pos = name.lastIndexOf(MANGLERCHAR, end);
				if (pos == -1) {
					throw new Exception("Illegal encoding: missing opening $");
				}

				String code = name.substring(pos + 1, end + 1);
				char[] outChars = name.substring(0, pos).toCharArray();

				pos = 0;
				int begin = 0;
				end = code.length();
				int state = 0;
				int encodePos = 0;
				char decodeChar;
				char ch;
				while (pos < end) {
					ch = code.charAt(pos);
					switch (state) {
					case 0: // First digit of encode position
						if (Character.isDigit(ch)) {
							begin = pos;
							state = 1;
							pos++;
						} else {
							throw new Exception(
									"Illegal encoding: expected digit");
						}
						break;

					case 1: // n digit of encode position
						if (Character.isDigit(ch)) {
							state = 1;
							pos++;
						} else {
							// end of encode position digits
							encodePos = Integer.parseInt(code.substring(begin,
									pos));
							state = 2;
						}
						break;

					case 2: // encode character code
						decodeChar = getDecodedChar(ch);
						if (decodeChar != 0) {
							outChars[encodePos] = decodeChar;
							state = 0;
							pos++;
						} else {
							throw new Exception(
									"Illegal encoding: expected character code");
						}
						break;

					default:
						throw new Exception("Unrecognized state");
					}
				}

				if (state != 0) {
					throw new Exception("Illegal encoding");
				}

				return new String(outChars);
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println(e.getMessage());
			}
		}

		return name;
	}

	private static void appendCharCode(StringBuffer buff, int pos, char ch) {
		switch (ch) {
		case '-':
			buff.append(pos);
			buff.append(DASHCODE);
			break;
		case '.':
			buff.append(pos);
			buff.append(PERIODCODE);
			break;
		case ':':
			buff.append(pos);
			buff.append(COLONCODE);
			break;
		default:
			break;
		}
	}

	private static char getDecodedChar(char ch) {
		char chOut = 0;
		switch (ch) {
		case DASHCODE:
			chOut = '-';
			break;
		case PERIODCODE:
			chOut = '.';
			break;
		case COLONCODE:
			chOut = ':';
			break;
		default:
			break;
		}

		return chOut;
	}

	public static void main(String args[]) {
		String[] vals = { "-.-..:", "Tag-1", "T-a:g.1", "Tag_1D$",
				"A1234356544334--x" };
		String[] vals2 = { "Tag_1$003C$", "Tag_1", "Tag-1", "Tag$$", "Tag$1$",
				"Tag$1E$" };

		String str;
		String result;
		String result2;

		System.out.println("*** Encode Test ***");
		for (int i = 0; i < vals.length; i++) {
			str = vals[i];
			result = encode(str);
			System.out.println(str + " ==> " + result);
			result2 = decode(result);
			System.out.println(result + " ==> " + result2);
		}

		System.out.println("*** Decode Test ***");
		for (int i = 0; i < vals2.length; i++) {
			str = vals2[i];
			result = decode(str);
			System.out.println(str + " ==> " + result);
		}

		result = "Tagname-1.2";
		result = encode(result);
		System.out.println(result);
	}

	private static final char[] disallowedChars = { '-', '.', ':' };
	private static final char REPLACECHAR = '_';
	private static final char MANGLERCHAR = '$';
	private static final char DASHCODE = 'D';
	private static final char PERIODCODE = 'P';
	private static final char COLONCODE = 'C';
	private static final boolean DEBUG = false;
}
