package org.adaptinet.node.scripting;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScriptProcessor {

	static private final short PRIMITIVE = 1;
	static private final short STRING = 2;
	static private final short BOOLEAN = 3;
	static private final short SHORT = 4;
	static private final short INTEGER = 5;
	static private final short LONG = 6;
	static private final short FLOAT = 7;
	static private final short DOUBLE = 8;
	static private final short ARRAY = 9;
	static private final short VECTOR = 10;
	static private final short DATE = 11;
	static private final short USERTYPE = 12;

	static Object executeMethod(Object targetObject, String name, Object[] args)
			throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {

		Object ret = null;
		for (Method method : targetObject.getClass().getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name)) {
				boolean bExecute = true;
				final Class<?> pvec[] = method.getParameterTypes();

				if (args == null) {
					if (pvec != null && pvec.length != 0) {
						bExecute = false;
					}
				} else if (pvec.length == args.length) {
					bExecute = true;
					for (int i = 0; i < pvec.length; i++) {
						if (pvec[i] != args[i].getClass()) {
							bExecute = false;
							break;
						}
					}
				}
				if (bExecute) {
					try {
						ret = method.invoke(targetObject, args);
						break;
					} catch (Exception e) {
						// Just fall thru
					}
				}
			}
		}
		return ret;
	}

	static void setValue(Object o, String name, Object data) throws Exception {

		Field f = o.getClass().getDeclaredField(name);
		int fieldType = getType(f.getType());
		setAccessible(true, f);

		if (fieldType == PRIMITIVE) {
			setPrimitive(f, o, data);
		} else {
			f.set(o, data);
		}
	}

	static Object getValue(Object o, String name) throws Exception {

		Object data = null;
		Field f = o.getClass().getDeclaredField(name);
		int fieldType = getType(f.getType());
		setAccessible(true, f);
		if (fieldType == PRIMITIVE) {
			data = getPrimitive(f, o);
		} else {
			return f.get(o);
		}
		return data;
	}

	static Object getValue(Object parent, String name, int idx)
			throws Exception {

		Object data = null;
		Field f = parent.getClass().getDeclaredField(name);
		final Class<?> type = f.getType();
		setAccessible(true, f);
		if (type.isArray()) {
			Object array = f.get(parent);
			int len = Array.getLength(array);
			if (idx < len) {
				final Class<?> cc = type.getComponentType();
				if (cc.isPrimitive() == true) {
					data = getPrimitive(cc, array, idx);
				} else {
					data = Array.get(array, idx);
				}
			} else {
				throw new Exception("Get Value for " + name + " index: " + idx
						+ " failed. Out of index error.");
			}
		}

		return data;
	}

	static private final void setAccessible(boolean b, Field f) {

		f.setAccessible(b);
	}

	static final short getType(Class<?> c) {

		short type = 0;
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
			} else if (c.isArray()) {
				type = ARRAY;
			} else if (c.getName().equals("java.util.Vector")) {
				type = VECTOR;
			} else if (c.getName().equals("java.util.Date")) {
				type = DATE;
			} else {
				type = USERTYPE;
			}
		}
		return type;
	}

	static final void setPrimitive(Field f, Object obj, Object data)
			throws Exception {

		if (data != null) {
			String className = f.getType().getName();
			if (className.compareTo("boolean") == 0) {
				f.setBoolean(obj, ((Boolean) data).booleanValue());
			} else if (className.compareTo("byte") == 0) {
				f.setByte(obj, ((Byte) data).byteValue());
			} else if (className.compareTo("char") == 0) {
				f.setChar(obj, ((Character) data).charValue());
			} else if (className.compareTo("double") == 0) {
				f.setDouble(obj, ((Double) data).doubleValue());
			} else if (className.compareTo("float") == 0) {
				f.setFloat(obj, ((Float) data).floatValue());
			} else if (className.compareTo("int") == 0) {
				f.setInt(obj, ((Integer) data).intValue());
			} else if (className.compareTo("long") == 0) {
				f.setLong(obj, ((Long) data).longValue());
			} else if (className.compareTo("short") == 0) {
				f.setShort(obj, ((Short) data).shortValue());
			}
		}
	}

	static final Object getPrimitive(Field f, Object obj) throws Exception {

		Object data = null;
		String className = f.getType().getName();
		if (className.compareTo("boolean") == 0) {
			data = new Boolean(f.getBoolean(obj));
		} else if (className.compareTo("byte") == 0) {
			data = new Byte(f.getByte(obj));
		} else if (className.compareTo("char") == 0) {
			data = new Character(f.getChar(obj));
		} else if (className.compareTo("double") == 0) {
			data = new Double(f.getDouble(obj));
		} else if (className.compareTo("float") == 0) {
			data = new Float(f.getFloat(obj));
		} else if (className.compareTo("int") == 0) {
			data = new Integer(f.getInt(obj));
		} else if (className.compareTo("long") == 0) {
			data = new Long(f.getLong(obj));
		} else if (className.compareTo("short") == 0) {
			data = new Short(f.getShort(obj));
		}
		return data;
	}

	static final Object getPrimitive(Class<?> c, Object obj, int idx) {

		Object data = null;
		String className = c.getName();
		if (className.compareTo("boolean") == 0) {
			data = new Boolean(Array.getBoolean(obj, idx));
		} else if (className.compareTo("byte") == 0) {
			data = new Byte(Array.getByte(obj, idx));
		} else if (className.compareTo("char") == 0) {
			data = new Character(Array.getChar(obj, idx));
		} else if (className.compareTo("double") == 0) {
			data = new Double(Array.getDouble(obj, idx));
		} else if (className.compareTo("float") == 0) {
			data = new Float(Array.getFloat(obj, idx));
		} else if (className.compareTo("int") == 0) {
			data = new Integer(Array.getInt(obj, idx));
		} else if (className.compareTo("long") == 0) {
			data = new Long(Array.getLong(obj, idx));
		} else if (className.compareTo("short") == 0) {
			data = new Short(Array.getShort(obj, idx));
		}
		return data;
	}

	static Object appendto(Object target, Object source) {

		short tt = getType(target.getClass());
		short st = getType(source.getClass());
		Object appended = null;

		if (tt == st) {
			switch (tt) {
			case BOOLEAN:
				break;
			case INTEGER:
				appended = new Integer(((Integer) target).intValue()
						+ ((Integer) source).intValue());
				break;
			case LONG:
				appended = new Long(((Long) target).longValue()
						+ ((Long) source).longValue());
				break;
			case FLOAT:
				appended = new Float(((Float) target).floatValue()
						+ ((Float) source).floatValue());
				break;
			case DOUBLE:
				appended = new Double(((Double) target).doubleValue()
						+ ((Double) source).doubleValue());
				break;
			case STRING:
			default:
				appended = target.toString() + source.toString();
				break;
			}
		} else {
			switch (tt) {
			case INTEGER:
				switch (st) {
				case LONG:
					appended = new Integer(((Integer) target).intValue()
							+ ((Long) source).intValue());
					break;
				case FLOAT:
					appended = new Integer(((Integer) target).intValue()
							+ ((Float) source).intValue());
					break;
				case DOUBLE:
					appended = new Integer(((Integer) target).intValue()
							+ ((Double) source).intValue());
					break;
				default:
					appended = source;
					break;
				}
				break;
			case LONG:
				switch (st) {
				case INTEGER:
					appended = new Integer(((Long) target).intValue()
							+ ((Integer) source).intValue());
					break;
				case FLOAT:
					appended = new Long(((Long) target).longValue()
							+ ((Float) source).longValue());
					break;
				case DOUBLE:
					appended = new Long(((Long) target).longValue()
							+ ((Double) source).longValue());
					break;
				default:
					appended = source;
					break;
				}
				break;
			case FLOAT:
				switch (st) {
				case INTEGER:
					appended = new Float(((Float) target).floatValue()
							+ ((Integer) source).floatValue());
					break;
				case LONG:
					appended = new Float(((Float) target).floatValue()
							+ ((Long) source).floatValue());
					break;
				case DOUBLE:
					appended = new Float(((Float) target).floatValue()
							+ ((Double) source).floatValue());
					break;
				default:
					appended = source;
					break;
				}
				break;
			case DOUBLE:
				switch (st) {
				case INTEGER:
					appended = new Double(((Double) target).doubleValue()
							+ ((Integer) source).floatValue());
					break;
				case LONG:
					appended = new Double(((Double) target).doubleValue()
							+ ((Long) source).floatValue());
					break;
				case FLOAT:
					appended = new Double(((Double) target).doubleValue()
							+ ((Double) source).floatValue());
					break;
				default:
					appended = source;
					break;
				}
				break;
			case BOOLEAN:
			case STRING:
			default:
				appended = target.toString() + source.toString();
				break;
			}
		}
		return appended;
	}

	static Object addto(Object target, Object source) {

		short tt = getType(target.getClass());
		short st = getType(source.getClass());
		Object results = null;

		if (tt == st) {
			switch (tt) {
			case INTEGER:
				results = new Integer(((Integer) target).intValue()
						+ ((Integer) source).intValue());
				break;
			case LONG:
				results = new Long(((Long) target).longValue()
						+ ((Long) source).longValue());
				break;
			case FLOAT:
				results = new Float(((Float) target).floatValue()
						+ ((Float) source).floatValue());
				break;
			case DOUBLE:
				results = new Double(((Double) target).doubleValue()
						+ ((Double) source).doubleValue());
				break;
			default:
				results = null;
				break;
			}
		} else {
			switch (tt) {
			case INTEGER:
				switch (st) {
				case LONG:
					results = new Integer(((Integer) target).intValue()
							+ ((Long) source).intValue());
					break;
				case FLOAT:
					results = new Integer(((Integer) target).intValue()
							+ ((Float) source).intValue());
					break;
				case DOUBLE:
					results = new Integer(((Integer) target).intValue()
							+ ((Double) source).intValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case LONG:
				switch (st) {
				case INTEGER:
					results = new Integer(((Long) target).intValue()
							+ ((Integer) source).intValue());
					break;
				case FLOAT:
					results = new Long(((Long) target).longValue()
							+ ((Float) source).longValue());
					break;
				case DOUBLE:
					results = new Long(((Long) target).longValue()
							+ ((Double) source).longValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case FLOAT:
				switch (st) {
				case INTEGER:
					results = new Float(((Float) target).floatValue()
							+ ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Float(((Float) target).floatValue()
							+ ((Long) source).floatValue());
					break;
				case DOUBLE:
					results = new Float(((Float) target).floatValue()
							+ ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case DOUBLE:
				switch (st) {
				case INTEGER:
					results = new Double(((Double) target).doubleValue()
							+ ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Double(((Double) target).doubleValue()
							+ ((Long) source).floatValue());
					break;
				case FLOAT:
					results = new Double(((Double) target).doubleValue()
							+ ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			default:
				results = null;
				break;
			}
		}
		return results;
	}

	static Object subtractFrom(Object target, Object source) {

		short tt = getType(target.getClass());
		short st = getType(source.getClass());
		Object results = null;

		if (tt == st) {
			switch (tt) {
			case INTEGER:
				results = new Integer(((Integer) target).intValue()
						- ((Integer) source).intValue());
				break;
			case LONG:
				results = new Long(((Long) target).longValue()
						- ((Long) source).longValue());
				break;
			case FLOAT:
				results = new Float(((Float) target).floatValue()
						- ((Float) source).floatValue());
				break;
			case DOUBLE:
				results = new Double(((Double) target).doubleValue()
						- ((Double) source).doubleValue());
				break;
			default:
				results = null;
				break;
			}
		} else {
			switch (tt) {
			case INTEGER:
				switch (st) {
				case LONG:
					results = new Integer(((Integer) target).intValue()
							- ((Long) source).intValue());
					break;
				case FLOAT:
					results = new Integer(((Integer) target).intValue()
							- ((Float) source).intValue());
					break;
				case DOUBLE:
					results = new Integer(((Integer) target).intValue()
							- ((Double) source).intValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case LONG:
				switch (st) {
				case INTEGER:
					results = new Integer(((Long) target).intValue()
							- ((Integer) source).intValue());
					break;
				case FLOAT:
					results = new Long(((Long) target).longValue()
							- ((Float) source).longValue());
					break;
				case DOUBLE:
					results = new Long(((Long) target).longValue()
							- ((Double) source).longValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case FLOAT:
				switch (st) {
				case INTEGER:
					results = new Float(((Float) target).floatValue()
							- ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Float(((Float) target).floatValue()
							- ((Long) source).floatValue());
					break;
				case DOUBLE:
					results = new Float(((Float) target).floatValue()
							- ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case DOUBLE:
				switch (st) {
				case INTEGER:
					results = new Double(((Double) target).doubleValue()
							- ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Double(((Double) target).doubleValue()
							- ((Long) source).floatValue());
					break;
				case FLOAT:
					results = new Double(((Double) target).doubleValue()
							- ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			default:
				results = null;
				break;
			}
		}
		return results;
	}

	static Object multiply(Object target, Object source) {

		short tt = getType(target.getClass());
		short st = getType(source.getClass());
		Object results = null;

		if (tt == st) {
			switch (tt) {
			case INTEGER:
				results = new Integer(((Integer) target).intValue()
						* ((Integer) source).intValue());
				break;
			case LONG:
				results = new Long(((Long) target).longValue()
						* ((Long) source).longValue());
				break;
			case FLOAT:
				results = new Float(((Float) target).floatValue()
						* ((Float) source).floatValue());
				break;
			case DOUBLE:
				results = new Double(((Double) target).doubleValue()
						* ((Double) source).doubleValue());
				break;
			default:
				results = null;
				break;
			}
		} else {
			switch (tt) {
			case INTEGER:
				switch (st) {
				case LONG:
					results = new Integer(((Integer) target).intValue()
							* ((Long) source).intValue());
					break;
				case FLOAT:
					results = new Integer(((Integer) target).intValue()
							* ((Float) source).intValue());
					break;
				case DOUBLE:
					results = new Integer(((Integer) target).intValue()
							* ((Double) source).intValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case LONG:
				switch (st) {
				case INTEGER:
					results = new Integer(((Long) target).intValue()
							* ((Integer) source).intValue());
					break;
				case FLOAT:
					results = new Long(((Long) target).longValue()
							* ((Float) source).longValue());
					break;
				case DOUBLE:
					results = new Long(((Long) target).longValue()
							* ((Double) source).longValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case FLOAT:
				switch (st) {
				case INTEGER:
					results = new Float(((Float) target).floatValue()
							* ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Float(((Float) target).floatValue()
							* ((Long) source).floatValue());
					break;
				case DOUBLE:
					results = new Float(((Float) target).floatValue()
							* ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case DOUBLE:
				switch (st) {
				case INTEGER:
					results = new Double(((Double) target).doubleValue()
							* ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Double(((Double) target).doubleValue()
							* ((Long) source).floatValue());
					break;
				case FLOAT:
					results = new Double(((Double) target).doubleValue()
							* ((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			default:
				results = null;
				break;
			}
		}
		return results;
	}
	
	static Object divide(Object target, Object source) {

		short tt = getType(target.getClass());
		short st = getType(source.getClass());
		Object results = null;

		if (tt == st) {
			switch (tt) {
			case INTEGER:
				results = new Integer(((Integer) target).intValue()
						/ ((Integer) source).intValue());
				break;
			case LONG:
				results = new Long(((Long) target).longValue()
						/ ((Long) source).longValue());
				break;
			case FLOAT:
				results = new Float(((Float) target).floatValue()
						/ ((Float) source).floatValue());
				break;
			case DOUBLE:
				results = new Double(((Double) target).doubleValue()
						/ ((Double) source).doubleValue());
				break;
			default:
				results = null;
				break;
			}
		} else {
			switch (tt) {
			case INTEGER:
				switch (st) {
				case LONG:
					results = new Integer(((Integer) target).intValue()
							/ ((Long) source).intValue());
					break;
				case FLOAT:
					results = new Integer(((Integer) target).intValue()
							/ ((Float) source).intValue());
					break;
				case DOUBLE:
					results = new Integer(((Integer) target).intValue()
							/ ((Double) source).intValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case LONG:
				switch (st) {
				case INTEGER:
					results = new Integer(((Long) target).intValue()
							/ ((Integer) source).intValue());
					break;
				case FLOAT:
					results = new Long(((Long) target).longValue()
							/ ((Float) source).longValue());
					break;
				case DOUBLE:
					results = new Long(((Long) target).longValue()
							/ ((Double) source).longValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case FLOAT:
				switch (st) {
				case INTEGER:
					results = new Float(((Float) target).floatValue()
							/ ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Float(((Float) target).floatValue()
							/ ((Long) source).floatValue());
					break;
				case DOUBLE:
					results = new Float(((Float) target).floatValue()
							/((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			case DOUBLE:
				switch (st) {
				case INTEGER:
					results = new Double(((Double) target).doubleValue()
							/ ((Integer) source).floatValue());
					break;
				case LONG:
					results = new Double(((Double) target).doubleValue()
							/ ((Long) source).floatValue());
					break;
				case FLOAT:
					results = new Double(((Double) target).doubleValue()
							/((Double) source).floatValue());
					break;
				default:
					results = null;
					break;
				}
				break;
			default:
				results = null;
				break;
			}
		}
		return results;
	}
}
