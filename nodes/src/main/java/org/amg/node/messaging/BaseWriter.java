package org.amg.node.messaging;

import static org.amg.node.exception.LambdaExceptionUtil.rethrowConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Vector;

import org.amg.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.amg.node.xmltools.xmlutils.XMLSerializerFactory;

public abstract class BaseWriter {

	protected OutputStream ostream = null;

	static String convertType(String name) {
		if (name.equals("Vector") || name.startsWith("["))
			return "Array";
		if (name.equalsIgnoreCase("String"))
			return "string";
		if (name.equalsIgnoreCase("Boolean"))
			return "boolean";
		if (name.equalsIgnoreCase("Float"))
			return "float";
		if (name.equalsIgnoreCase("Double"))
			return "double";
		if (name.equalsIgnoreCase("Integer"))
			return "integer";
		if (name.equalsIgnoreCase("Long"))
			return "long";
		if (name.equalsIgnoreCase("Short"))
			return "short";
		if (name.equalsIgnoreCase("Byte"))
			return "byte";
		if (name.equalsIgnoreCase("Char"))
			return "char";

		return "Struct";
	}

	public BaseWriter() {
	}

	public BaseWriter(OutputStream ostream) {
		this.ostream = ostream;
		try {
			writeString("<?xml version=\"1.0\"?>");
		} catch (Exception e) {
		}
	}

	public BaseWriter(OutputStream ostream, boolean bHeader) {
		this.ostream = ostream;
		if (bHeader) {
			try {
				writeString("<?xml version=\"1.0\"?>");
			} catch (Exception e) {
			}
		}
	}

	protected boolean checkLangType(String className) {
		return className.startsWith("java.lang");
	}

	protected boolean checkUtilType(String className) {
		return className.startsWith("java.util");
	}

	protected final void convertToXML(Object o) throws Exception {
		if (o == null)
			return;

		try {
			Class<?> c = o.getClass();
			if (c != null) {
				String type = null;
				String fulltype = null;

				fulltype = c.getName();
				type = fulltype.substring(fulltype.lastIndexOf('.') + 1, fulltype.length());
				String xtype = null;

				if (isRawType(type) == true || checkLangType(fulltype) == true) {
					xtype = convertType(type);
					writeString("<");
					writeString(xtype);
					writeString(">");
					writeString(o.toString());
				} else if (c.isArray()) {
					writeString("<");
					xtype = "Array";
					write(xtype);
					Class<?> cc = c.getComponentType();
					String arraytype = cc.getName();
					arraytype = arraytype.substring(arraytype.lastIndexOf('.') + 1, arraytype.length());
					writeString(" type=\"");
					writeString(convertType(arraytype));
					writeString("\">");
					int size = Array.getLength(o);
					if (arraytype.compareToIgnoreCase("byte") == 0) {
						writeString((byte[]) o);
					} else if (arraytype.compareToIgnoreCase("char") == 0) {
						writeString((char[]) o);
					} else {
						for (int i = 0; i < size; i++)
							convertToXML(Array.get(o, i));
					}
				} else if (checkUtilType(fulltype) == true) {
					@SuppressWarnings("unchecked")
					Vector<? extends Object> v = (Vector<? extends Object>) o;

					if (v.size() > 0) {
						Object element = v.get(0);
						String elementType = element.getClass().getName();
						elementType.substring(elementType.lastIndexOf('.') + 1, elementType.length());
						writeString(" type=\"");
						writeString(convertType(elementType));
						writeString("\">");
					}

					v.stream().forEach(rethrowConsumer(e -> convertToXML(e)));
				} else {
					try {
						IXMLOutputSerializer serializer = XMLSerializerFactory.getOutputSerializer();
						writeString(">");
						writeString(serializer.get(o));
					} catch (Exception e) {
					}

				}
				writeString("</");
				writeString(xtype);
				writeString(">");
			}
		} catch (NullPointerException npe) {
			// continue processing...not unexpected...
		} catch (Exception err) {
			err.printStackTrace(System.err);
			throw err;
		}
	}

	protected boolean isRawType(String className) {
		boolean bRet = false;
		if (className.compareToIgnoreCase("boolean") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("byte") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("char") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("double") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("float") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("int") == 0) {
			bRet = true;
		} else if (className.compareToIgnoreCase("long") == 0) {
			bRet = true;
		}
		if (className.compareToIgnoreCase("short") == 0) {
			bRet = true;
		}
		return bRet;
	}

	public void setStream(OutputStream os) {
		try {
			ostream = os;
			writeString("<?xml version=\"1.0\"?>");
		} catch (Exception e) {
		}
	}

	protected final void write(byte bytes[]) throws IOException {
		int len = bytes.length;
		byte[] bLen = String.format("%1$-" + 10 + "s", Integer.toString(len)).getBytes();
		ostream.write(bLen, 0, 10);
		ostream.write(bytes, 0, len);
	}

	protected final void write(byte b) throws IOException {
		switch (b) {
		case '<':
			ostream.write('&');
			ostream.write('l');
			ostream.write('t');
			ostream.write(';');
			break;

		case '>':
			ostream.write('&');
			ostream.write('g');
			ostream.write('t');
			ostream.write(';');
			break;

		case '&':
			ostream.write('&');
			ostream.write('a');
			ostream.write('m');
			ostream.write('p');
			break;

		case '"':
			ostream.write('&');
			ostream.write('q');
			ostream.write('u');
			ostream.write('o');
			ostream.write('t');
			ostream.write(';');
			break;

		case '\r':
		case '\n':
			ostream.write('&');
			ostream.write('#');
			ostream.write(b);
			ostream.write(';');
			break;

		default:
			ostream.write(b);
		}
	}

	protected final void write(char c) throws IOException {
		switch (c) {
		case '<':
			writeChar('&');
			writeChar('l');
			writeChar('t');
			writeChar(';');
			break;

		case '>':
			writeChar('&');
			writeChar('g');
			writeChar('t');
			writeChar(';');
			break;

		case '&':
			writeChar('&');
			writeChar('a');
			writeChar('m');
			writeChar('p');
			break;

		case '"':
			writeChar('&');
			writeChar('q');
			writeChar('u');
			writeChar('o');
			writeChar('t');
			writeChar(';');
			break;

		case '\r':
		case '\n':
			writeChar('&');
			writeChar('#');
			writeChar(c);
			writeChar(';');
			break;

		default:
			writeChar(c);
		}
	}

	protected final void write(char[] chars) throws IOException {
		int len = chars.length;
		for (int i = 0; i < len; i++)
			write(chars[i]);
	}

	protected final void write(long data) throws IOException {
		write(Long.toString(data));
	}

	protected final void write(Object data) throws IOException {
		write(data.toString());
	}

	protected final void write(String data) throws IOException {
		write(data.getBytes());
	}

	protected final void writeChar(char c) throws IOException {
		ostream.write((c >>> 8) & 0xFF);
		ostream.write((c >>> 0) & 0xFF);
	}

	protected final void writeString(byte[] bytes) throws IOException {
		try {
			int len = bytes.length;
			for (int i = 0; i < len; i++)
				ostream.write(bytes[i]);
		} catch (NullPointerException e) {
		}
	}

	protected final void writeString(char[] chars) throws IOException {
		try {
			int len = chars.length;
			for (int i = 0; i < len; i++)
				ostream.write(chars[i]);
		} catch (NullPointerException e) {
		}
	}

	protected final void writeString(String data) throws IOException {
		try {
			byte[] bytes = data.getBytes();
			int len = bytes.length;
			for (int i = 0; i < len; i++)
				ostream.write(bytes[i]);
		} catch (NullPointerException e) {
		}
	}
}