package org.adaptinet.node.xmltools.xmlutils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.adaptinet.node.xmltools.parser.XMLAttr;
import org.adaptinet.node.xmltools.parser.XMLDOMWriter;
import org.adaptinet.node.xmltools.parser.XMLDocument;
import org.adaptinet.node.xmltools.parser.XMLNode;

public class XMLElementDeserializer extends AliasProcessor {

	private XMLDocument domDocument;
	private XMLNode root = null;

	public String get(Object o, String pi, boolean bXMLPI, String agentPIAttribs)
			throws Exception {

		String strReturn = null;
		domDocument = new XMLDocument();
		if (bXMLPI) {
			XMLNode xmlPI = (XMLNode) domDocument.createProcessingInstruction(
					"xml", pi);
			domDocument.appendChild(xmlPI);
		}

		if (agentPIAttribs != null && agentPIAttribs.length() > 0) {
			XMLNode agentPI = (XMLNode) domDocument
					.createProcessingInstruction("xmlagent", agentPIAttribs);
			domDocument.appendChild(agentPI);
		}

		Class<?> c = o.getClass();
		String name = null;
		name = c.getName();
		name = name.substring(name.lastIndexOf('.') + 1, name.length());
		name = NameMangler.decode(name);
		unwindProperties(o, name, c.getName());

		if (root != null) {
			domDocument.appendChild(root);
			XMLDOMWriter writer = new XMLDOMWriter(true);
			strReturn = writer.writeXML(domDocument);
		}
		return strReturn;
	}

	XMLNode unwindProperties(Object o, String name, String parentType)
			throws Exception {

		if (o == null) {
			return null;
		}

		boolean bSet = false;
		XMLNode element = null;
		XMLAttr attribute = null;

		Class<?> c = o.getClass();
		if (c != null) {

			element = (XMLNode) domDocument.createElement(name);
			if (root == null) {
				root = element;
			}
			Field[] fields = c.getDeclaredFields();

			int size = fields.length;
			for (int i = 0; i < size; i++) {
				Object prop = null;
				fields[i].setAccessible(true);
				String fieldName = fields[i].getName();
				if (fieldName.equalsIgnoreCase("this$0")) {
					continue;
				}

				if (!fields[i].equals(null)) {
					prop = fields[i].get(o);
				}

				if (prop == null) {
					continue;
				}

				Class<?> type = fields[i].getType();
				fieldName = NameMangler.decode(fieldName);

				if (type.isPrimitive()) {
					String szBuffer = getPrimitive(fields[i], type, o);
					if (szBuffer != null) {
						bSet = true;
						XMLNode text = (XMLNode) domDocument
								.createTextNode(szBuffer);

						if (fieldName.equals("contentData")) {
							element.appendChild(text);
						} else {
							attribute = createAttribute(parentType, fieldName);
							attribute.appendChild(text);
							element.setAttributeNode(attribute);
						}
					}
					// } else if(checkNumberType(type)) {
				} else if (checkLangType(type)) {
					String szBuffer = prop.toString();
					if (szBuffer != null) {
						bSet = true;
						XMLNode text = (XMLNode) domDocument
								.createTextNode(szBuffer);

						if (fieldName.equals("contentData")) {
							element.appendChild(text);
						} else {
							XMLNode childElement = createElement(parentType,
									fieldName);
							childElement.appendChild(text);
							element.appendChild(childElement);
						}
					}
				} else if (prop instanceof java.util.Date) {
					Date date = (Date) prop;
					String szBuffer = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss").format(date);
					if (szBuffer != null) {
						bSet = true;
						XMLNode text = (XMLNode) domDocument
								.createTextNode(szBuffer);

						if (fieldName.equals("contentData")) {
							element.appendChild(text);
						} else {
							XMLNode childElement = createElement(parentType,
									fieldName);
							childElement.appendChild(text);
							element.appendChild(childElement);
						}
					}
				} else if (type.isArray()) {
					Class<?> cc = type.getComponentType();
					Object array = fields[i].get(o);
					if (array != null) {
						bSet = true;
						int len = Array.getLength(array);
						if (cc.isPrimitive() == true) {
							String arrayClassName = cc.getName();
							String szBuffer = getPrimitive(arrayClassName,
									array, len);
							XMLNode text = (XMLNode) domDocument
									.createTextNode(szBuffer);
							attribute = createAttribute(parentType, fieldName);
							attribute.appendChild(text);
							element.setAttributeNode(attribute);
						} else {
							int count = 0;
							for (int idx = 0; idx < len; idx++) {
								prop = Array.get(array, idx);
								if (prop == null)
									continue;
								count++;
								Class<?> propClass = prop.getClass();
								if (checkLangType(propClass) == true) {
									XMLNode arrayElement = createElement(
											parentType, fieldName);
									String elementText = prop.toString();
									if (elementText != null
											&& elementText.length() > 0) {
										XMLNode text = (XMLNode) domDocument
												.createTextNode(elementText);
										arrayElement.appendChild(text);
									}
									element.appendChild(arrayElement);
								} else if (prop instanceof java.util.Date) {
									XMLNode arrayElement = createElement(
											parentType, fieldName);
									String elementText = new SimpleDateFormat(
											"yyyy-MM-dd'T'HH:mm:ss")
											.format(prop);
									if (elementText != null
											&& elementText.length() > 0) {
										XMLNode text = (XMLNode) domDocument
												.createTextNode(elementText);
										arrayElement.appendChild(text);
									}
									element.appendChild(arrayElement);
								} else {
									XMLNode child = unwindProperties(prop,
											fieldName, propClass.getName());
									if (child != null) {
										element.appendChild(child);
									}
								}
							}
							if (count == 0) {
								element.appendChild(createElement(parentType,
										fieldName));
							}
						}
					}
				} else {
					if (fields[i].get(o) instanceof java.util.List) {

						List<?> elementList = (List<?>) fields[i].get(o);
						Iterator<?> it = elementList.iterator();
						while (it.hasNext()) {
							Object elm = it.next();
							Class<?> ec = elm.getClass();
							if (checkLangType(ec) == true) {
								XMLNode arrayElement = createElement(
										parentType, fieldName);
								String elementText = elm.toString();
								if (elementText != null
										&& elementText.length() > 0) {
									XMLNode text = (XMLNode) domDocument
											.createTextNode(elementText);
									arrayElement.appendChild(text);
								}
								element.appendChild(arrayElement);
								bSet = true;
							}
							if (elm instanceof java.util.Date) {
								XMLNode arrayElement = createElement(
										parentType, fieldName);
								String elementText = new SimpleDateFormat(
										"yyyy-MM-dd'T'HH:mm:ss").format(elm);
								if (elementText != null
										&& elementText.length() > 0) {
									XMLNode text = (XMLNode) domDocument
											.createTextNode(elementText);
									arrayElement.appendChild(text);
								}
								element.appendChild(arrayElement);
								bSet = true;
							}

							else {
								XMLNode e = unwindProperties(elm, fieldName,
										ec.getName());
								if (e != null) {
									bSet = true;
									element.appendChild(e);
								}
							}
						}
					} else {
						XMLNode child = unwindProperties(prop, fieldName,
								type.getName());
						if (child != null) {
							bSet = true;
							element.appendChild(child);
						}
					}
				}
			}
		}
		if (bSet == false) {
			element = null;
		}
		return element;
	}

	boolean checkLangType(Class<?> c) {
		String className = c.getName();
		return className.startsWith("java.lang")
				|| className.startsWith("java.math");
		// return className.startsWith("java.lang") &&
		// !className.contentEquals("java.lang.Object");
	}

	boolean checkNumberType(Class<?> c) {
		String className = c.getName();
		return className.startsWith("java.math");
	}

	public String getPrimitive(Field field, Class<?> type, Object o) {
		String data = null;
		String className = type.getName();

		try {
			try {
				field.setAccessible(true);
			} catch (NoSuchMethodError e) {
			}

			if (className.compareTo("boolean") == 0) {
				boolean z = field.getBoolean(o);
				data = new Boolean(z).toString();
			} else if (className.compareTo("byte") == 0) {
				byte b = field.getByte(o);
				data = new Byte(b).toString();
			} else if (className.compareTo("char") == 0) {
				char c = field.getChar(o);
				data = new Character(c).toString();
			} else if (className.compareTo("double") == 0) {
				double d = field.getDouble(o);
				data = new Double(d).toString();
			} else if (className.compareTo("float") == 0) {
				float f = field.getFloat(o);
				data = new Float(f).toString();
			} else if (className.compareTo("int") == 0) {
				int i = field.getInt(o);
				data = new Integer(i).toString();
			} else if (className.compareTo("long") == 0) {
				long l = field.getLong(o);
				data = new Long(l).toString();
			}
			if (className.compareTo("short") == 0) {
				short s = field.getShort(o);
				data = new Short(s).toString();
			}
		} catch (SecurityException e) {
			e.printStackTrace(System.err);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
		return data;
	}

	public String getPrimitive(String className, Object o, int i) {

		String data = null;
		try {
			if (className.compareTo("boolean") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Boolean(Array.getBoolean(o, idx)).toString();
				}
			} else if (className.compareTo("byte") == 0) {
				byte b[] = new byte[i];
				for (int idx = 0; idx < i; idx++) {
					b[idx] = Array.getByte(o, idx);
				}
				data = new String(b);
			} else if (className.compareTo("char") == 0) {
				char c[] = new char[i];
				for (int idx = 0; idx < i; idx++) {
					c[idx] = Array.getChar(o, idx);
				}
				data = new String(c);
			} else if (className.compareTo("double") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Double(Array.getDouble(o, idx)).toString();
				}
			} else if (className.compareTo("float") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Float(Array.getFloat(o, idx)).toString();
				}
			} else if (className.compareTo("int") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Integer(Array.getInt(o, idx)).toString();
				}
			} else if (className.compareTo("long") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Long(Array.getLong(o, idx)).toString();
				}
			}
			if (className.compareTo("short") == 0) {
				data = new String();
				for (int idx = 0; idx < i; idx++) {
					data += new Short(Array.getShort(o, idx)).toString();
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace(System.err);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
		return data;
	}

	public XMLNode createElement(String className, String fieldName) {
		XMLNode element = null;
		fieldName = getAlias(className, fieldName);
		element = (XMLNode) domDocument.createElement(fieldName);
		return element;
	}

	public XMLAttr createAttribute(String className, String fieldName) {
		XMLAttr attribute = null;
		fieldName = getAlias(className, fieldName);
		attribute = XMLDocument.createAttribute(fieldName);
		return attribute;
	}
}
