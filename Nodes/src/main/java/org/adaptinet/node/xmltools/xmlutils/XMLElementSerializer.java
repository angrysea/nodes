package org.adaptinet.node.xmltools.xmlutils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

//@SuppressWarnings("unchecked")
public class XMLElementSerializer extends AliasProcessor {

	static private final short PRIMITIVE = 1;
	static private final short STRING = 2;
	static private final short BOOLEAN = 3;
	static private final short SHORT = 4;
	static private final short INTEGER = 5;
	static private final short LONG = 6;
	static private final short FLOAT = 7;
	static private final short DOUBLE = 8;
	static private final short ARRAY = 9;
	static private final short LIST = 10;
	static private final short DATE = 11;
	static private final short USERTYPE = 12;
	static private final short BIGINTEGER = 13;
	static private final short BIGDECIMAL = 14;
	
	protected String name = null;
	protected String schemaURI;
	private boolean bIsElement;
	private char data[] = null;
	private int len = 0;
	@SuppressWarnings("rawtypes")
	private Hashtable<String, List> lists = null;
	private Class<?> elementClass = null;
	private Vector<XMLElementSerializer> childElements = null;
	
	public XMLElementSerializer(String name, String schemaURI, XMLElementSerializer parent) {
		this(name, schemaURI, parent, true);
	}

	public XMLElementSerializer(String name, String schemaURI, XMLElementSerializer parent,
			boolean bIsElement) {

		name = NameMangler.encode(name);
		this.name = name;
		this.schemaURI = schemaURI;
		if (parent != null) {
			parent.addChild(this);
		}
		this.bIsElement = bIsElement;
	}

	final protected void addChild(XMLElementSerializer child) {

		if (childElements == null) {
			childElements = new Vector<XMLElementSerializer>();
		}
		childElements.addElement(child);
	}

	public Enumeration<XMLElementSerializer> children() {

		if (childElements != null)
			return childElements.elements();
		else
			return null;
	}

	final public Object update(String packageName, ClassLoader classLoader) {

		Object o = null;
		try {
			// Set the element class
			setElementClass(classLoader, packageName);
			if ((o = elementClass.newInstance()) != null) {
				if (data != null) {
					setData(o);
				}

				Enumeration<XMLElementSerializer> e = children();
				if (e != null) {
					while (e.hasMoreElements()) {
						e.nextElement().update(this, o,
								classLoader, packageName);
					}
					loadLists(o);
				}
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Update error IllegalArgumentException "
					+ e.getMessage());
		} catch (Exception e) {
			System.err.println("Exception thrown Update. " + e);
			e.printStackTrace();
		}
		return o;
	}

	@SuppressWarnings("unchecked")
	final public Object update(XMLElementSerializer parentElement, Object parent,
			ClassLoader classLoader, String packageName) {

		Object o = null;
		try {
			Field f = null;
			try {
				// Get the field if one exists
				try {
					if(parent!=null) {
						name = getProperty(parent.getClass().getName(), name);
					}
					f = parent.getClass().getDeclaredField("_" + name);
				} catch (NoSuchFieldException nsf) {
					// May not be a generated class.
					f = parent.getClass().getDeclaredField(name);
				}
				// If this is an element set the class so an instance can be
				// created.
				if (bIsElement) {
					elementClass = f.getType();
				}
			} catch (NoSuchFieldException nsf) {
				// If no field found it may be the content elements
				// There is no field to but this element there must be a
				// problem.
				try {
					f = parent.getClass().getDeclaredField("_contentElements");
				} catch (NoSuchFieldException nsf2) {
					return null;
				}
				// Unknown class from who knows where.
				if (bIsElement) {
					setElementClass(classLoader, packageName);
				}
			}

			int fieldType = getType(f.getType());
			if (bIsElement) {
				// Create an instance of the object.

				if (fieldType == USERTYPE || fieldType == LIST) {
					o = elementClass.newInstance();
				} else if (fieldType == ARRAY) {
					Class<?> componetClass = elementClass.getComponentType();
					int componetType = getType(componetClass);
					if (componetType == USERTYPE || fieldType == LIST) {
						o = componetClass.newInstance();
					}
				}

				if (o != null) {
					Enumeration<XMLElementSerializer> e = children();
					if (e != null) {
						while (e.hasMoreElements()) {
							e.nextElement().update(this, o,
									classLoader, packageName);
						}
					}
					loadLists(o);
				}
			}

			if (fieldType == USERTYPE || fieldType == LIST) {

				if (getType(parentElement.getElementClass()) == LIST) {
					parentElement.getList(parentElement.getName(),
							parentElement.getElementClass()).add(o);
				} else {
					setAccessible(true, f);
					f.set(parent, o);
				}
				// Set the contentData.
				if (data != null && fieldType == USERTYPE) {
					setData(o);
				}
			} else if (fieldType == ARRAY) {
				Class<?> c = f.getType().getComponentType();
				int componetType = getType(c);
				if (componetType == PRIMITIVE) {
					if (data != null) {
						insertIntoArray(f, parent);
					}
				} else {
					if (data != null) {
						switch (componetType) {

						case STRING:
							o = String.copyValueOf(data);
							break;

						case DATE:
							o = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
									.parse(String.copyValueOf(data));
							break;

						case SHORT:
							o = Short.valueOf(String.copyValueOf(data));
							break;

						case BOOLEAN:
							o = Boolean.valueOf(String.copyValueOf(data));
							break;

						case INTEGER:
							o = Integer.valueOf(String.copyValueOf(data));
							break;

						case BIGINTEGER:
							o = new BigInteger(new String(data));
							break;

						case BIGDECIMAL:
							o = new BigDecimal(new String(data));
							break;

						case LONG:
							o = Long.valueOf(String.copyValueOf(data));
							break;

						case FLOAT:
							o = Float.valueOf(String.copyValueOf(data));
							break;

						case DOUBLE:
							o = Double.valueOf(String.copyValueOf(data));
							break;

						default:
							break;
						}
					}
					setAccessible(true, f);
					// Get the old array
					Object oldArray = f.get(parent);
					// calculate the new size of the array
					int size = 1;
					Object array = null;
					if (oldArray != null && Array.get(oldArray, 0) != null) {
						size = Array.getLength(oldArray) + 1;
						// Create a new array
						array = Array.newInstance(c, size);
						System.arraycopy(oldArray, 0, array, 0, size - 1);
					} else {
						array = Array.newInstance(c, size);
					}
					Array.set(array, size - 1, o);
					f.set(parent, array);
				}
			} else if (data != null) {

				setAccessible(true, f);
				switch (fieldType) {

				case STRING:
					f.set(parent, String.copyValueOf(data));
					break;

				case DATE:
					f.set(parent, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
							.parse(String.copyValueOf(data)));
					break;

				case PRIMITIVE:
					XMLElementSerializer.setPrimitive(f, parent, String
							.copyValueOf(data));
					break;

				case SHORT:
					f.set(parent, Short.valueOf(String.copyValueOf(data)));
					break;

				case BOOLEAN:
					f.set(parent, Boolean.valueOf(String.copyValueOf(data)));
					break;

				case INTEGER:
					f.set(parent, Integer.valueOf(String.copyValueOf(data)));
					break;

				case LONG:
					f.set(parent, Long.valueOf(String.copyValueOf(data)));
					break;

				case FLOAT:
					f.set(parent, Float.valueOf(String.copyValueOf(data)));
					break;

				case DOUBLE:
					f.set(parent, Double.valueOf(String.copyValueOf(data)));
					break;

				case BIGINTEGER:
					f.set(parent, new BigInteger(new String(data)));
					break;

				case BIGDECIMAL:
					f.set(parent, new BigDecimal(new String(data)));
					break;

				default:
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Update error IllegalArgumentException "
					+ e.getMessage());
		} catch (IllegalAccessException e) {
			System.err.println("Update error IllegalAccessException "
					+ e.getMessage());
		} catch (Exception e) {
			System.err.println("Exception thrown Update. " + e);
			e.printStackTrace();
		}
		return o;
	}

	protected void setElementClass(ClassLoader classLoader, String packageName)
			throws ClassNotFoundException {

		elementClass = null;
		if (classLoader != null) {
			if (packageName != null && packageName.length() > 0) {
				StringTokenizer tokenizer = new StringTokenizer(packageName,
						";");
				ClassNotFoundException classNotFound = null;
				while (tokenizer.hasMoreTokens()) {
					try {
						elementClass = classLoader.loadClass(tokenizer
								.nextToken()
								+ "." + name);
						break;
					} catch (ClassNotFoundException e) {
						classNotFound = e;
					}
				}
				if (classNotFound != null) {
					throw classNotFound;
				}
			} else {
				elementClass = classLoader.loadClass(name);
			}
		} else {
			if (packageName != null && packageName.length() > 0) {
				StringTokenizer tokenizer = new StringTokenizer(packageName,
						";");
				ClassNotFoundException classNotFound = null;
				while (tokenizer.hasMoreTokens()) {
					try {
						elementClass = Class.forName(tokenizer.nextToken()
								+ "." + name);
						break;
					} catch (ClassNotFoundException e) {
						classNotFound = e;
					}
				}
				if (classNotFound != null) {
					throw classNotFound;
				}
			} else {
				elementClass = Class.forName(name);
			}
		}
	}

	final private void loadLists(Object o) throws IllegalAccessException {

		if (lists != null) {
			Enumeration<String> keys = lists.keys();
			while (keys.hasMoreElements()) {
				String p = keys.nextElement();
				Field f = null;
				try {
					try {
						f = elementClass.getDeclaredField("_" + p);
					} catch (NoSuchFieldException ex) {
						f = elementClass.getDeclaredField(p);
					}
				} catch (NoSuchFieldException ex) {
					// Use content elements vector
					try {
						f = elementClass.getDeclaredField("_contentElements");
					} catch (NoSuchFieldException ex2) {
					}
				}

				if (f != null) {
					setAccessible(true, f);
					f.set(o, lists.get(p));
				}
			}
		}
		lists = null;
	}

	@SuppressWarnings("rawtypes")
	static final private short getType(Class c) {

		short type = 0;
		try {
			if (c != null) {
				if (c.isPrimitive()) {
					type = PRIMITIVE;
				} else if (c.getName().equals("java.lang.String")) {
					type = STRING;
				} else if (c.getName().equals("java.lang.Short")) {
					type = SHORT;
				} else if (c.getName().equals("java.lang.Boolean")) {
					type = BOOLEAN;
				} else if (c.getName().equals("java.lang.Integer")) {
					type = INTEGER;
				} else if (c.getName().equals("java.lang.Long")) {
					type = LONG;
				} else if (c.getName().equals("java.lang.Float")) {
					type = FLOAT;
				} else if (c.getName().equals("java.lang.Double")) {
					type = DOUBLE;
				} else if (c.getName().equals("java.math.BigInteger")) {
					type = BIGINTEGER;
				} else if (c.getName().equals("java.math.BigDecimal")) {
					type = BIGDECIMAL;
				} else if (c.isArray()) {
					type = ARRAY;
				} else if (isList(c)) {
					type = LIST;
				} else if (c.getName().equals("java.util.Date")) {
					type = DATE;
				} else {
					type = USERTYPE;
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		return type;
	}

	@SuppressWarnings("rawtypes")
	static private final boolean isList(Class c) {

		Class[] interfaces = c.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].getName().equals("java.util.List")) {
				return true;
			}
		}

		return false;
	}

	private final void setData(Object o) {

		try {
			Field dataField = elementClass.getDeclaredField("_contentData");

			if (dataField != null) {
				setAccessible(true, dataField);
				switch (getType(dataField.getType())) {

				case PRIMITIVE:
					setPrimitive(dataField, o, new String(data));
					break;

				case STRING:
				default:
					dataField.set(o, new String(data));
					break;
				}
			}
		} catch (NoSuchFieldException e) {
			// Fall-Thru;
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	final private void insertIntoArray(Field f, Object obj) {

		try {
			setAccessible(true, f);
			Class<?> c = f.getType().getComponentType();
			Object array = Array.newInstance(c, len);
			System.arraycopy(data, 0, array, 0, len);
			f.set(obj, array);
		} catch (SecurityException e) {
			System.out.println("SecurityException in setPrimitive: "
					+ e.getMessage());
		} catch (Exception e) {
			System.out.println("IllegalAccessException in setPrimitive: "
					+ e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	final private void insertObjectIntoArray(Class<?> c, Object parent, Field f,
			Object o) {

		try {
			setAccessible(true, f);
			// Get the old array
			Object oldArray = f.get(parent);
			// calculate the new size of the array
			Object array = null;
			int size = 10;
			if (oldArray != null && Array.get(oldArray, 0) != null) {
				size = Array.getLength(oldArray) + 10;
				// Create a new array
				array = Array.newInstance(c, size);
				System.arraycopy(oldArray, 0, array, 0, size - 1);
			} else {
				array = Array.newInstance(c, size);
			}
			Array.set(array, size - 1, o);
			f.set(parent, array);
		} catch (SecurityException e) {
			System.out.println("SecurityException in setPrimitive: "
					+ e.getMessage());
		} catch (Exception e) {
			System.out.println("IllegalAccessException in setPrimitive: "
					+ e.getMessage());
		}
	}

	static final private void setPrimitive(Field f, Object obj, String data) {

		try {
			String className = f.getType().getName();
			if (className.compareTo("boolean") == 0) {
				Boolean z = new Boolean(data);
				f.setBoolean(obj, z.booleanValue());
			} else if (className.compareTo("byte") == 0) {
				Byte b = new Byte(data);
				f.setByte(obj, b.byteValue());
			} else if (className.compareTo("char") == 0) {
				f.setChar(obj, data.charAt(0));
			} else if (className.compareTo("double") == 0) {
				Double d = new Double(data);
				f.setDouble(obj, d.doubleValue());
			} else if (className.compareTo("float") == 0) {
				Float flt = new Float(data);
				f.setFloat(obj, flt.floatValue());
			} else if (className.compareTo("int") == 0) {
				Integer i = new Integer(data);
				f.setInt(obj, i.intValue());
			} else if (className.compareTo("long") == 0) {
				Long l = new Long(data);
				f.setLong(obj, l.longValue());
			} else if (className.compareTo("short") == 0) {
				Short s = new Short(data);
				f.setShort(obj, s.shortValue());
			}
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException in setPrimitive: "
					+ e.getMessage());
		} catch (SecurityException e) {
			System.out.println("SecurityException in setPrimitive: "
					+ e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	private final List getList(String name, Class<?> parentClass)
			throws InstantiationException, IllegalAccessException {

		List<?> l = null;
		if (lists == null) {
			lists = new Hashtable<String, List>();
		} else {
			l = lists.get(name);
		}
		if (l == null) {
			l = (List<?>)parentClass.newInstance();
			lists.put(name, l);
		}
		return l;
	}

	private Class<?> getElementClass() {
		return elementClass;
	}

	public String getName() {
		return name;
	}

	protected void setAccessible(boolean b, Field f) {

		try {
			f.setAccessible(b);
		} catch (Exception e) {
		}
	}

	final public void setData(char buf[], int offset, int len) {

		if (this.len > 0) {
			char temp[] = new char[len + this.len];
			System.arraycopy(data, 0, temp, 0, this.len);
			System.arraycopy(buf, offset, temp, this.len, len);
			this.len += len;
			data = new char[this.len];
			System.arraycopy(temp, 0, data, 0, this.len);
		} else {
			this.len = len;
			data = new char[len];
			System.arraycopy(buf, offset, data, 0, len);
		}
	}

	final public void setData(String buf) {
		this.len = buf.length();
		data = new char[len];
		buf.getChars(0, len, data, 0);
	}

	final public String getData() {
		return String.copyValueOf(data);
	}

	final public String toString() {
		return String.copyValueOf(data);
	}
}
