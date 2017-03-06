package org.adaptinet.node.mimehandlers.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.servlet.ServletContext;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.servlet.http.HttpServletRequest;
import org.adaptinet.node.servlet.http.HttpServletResponse;

public abstract class Controller {

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

	protected HttpServletRequest req = null;
	protected HttpServletResponse resp = null;
	protected ServletContext context;
	protected HttpServletConfig config;
	// private final Logger logger = new Logger(Controller.class);

	final public void setReq(HttpServletRequest req) {
		this.req = req;
	}

	final public void setResp(HttpServletResponse resp) {
		this.resp = resp;
	}

	final public void setServletContext(ServletContext context) {
		this.context = context;
	}

	final public ServletContext getServletContext() {
		return context;
	}

	final public void setViewClassName(String viewClassName) {
	}

	static public String getGuid() {
		return UUID.randomUUID().toString();
	}

	public final void View(Object viewData) throws IOException, ServletException {
		try {
			req.setAttribute("ViewData", viewData);
			// context.getRequestDispatcher(viewClassName).forward(req, resp);
			// } catch (IOException e) {
			// e.printStackTrace();
			// throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void View() throws IOException, ServletException {
		// context.getRequestDispatcher(viewClassName).forward(req, resp);
	}

	public final void RedirectToAction(String view, String controller) throws IOException, ServletException {
		resp.sendRedirect("/" + controller + "/" + view);
	}

	public String getParam(String key, Map<String, String[]> params) {
		String param = null;

		try {
			String values[] = params.get(key);
			if (values != null && values.length > 0) {
				param = values[0];
			}
		} catch (Exception e) {
			param = null;
		}

		return param;
	}

	public void populateRequest(Object o, Map<String, String[]> params) {

		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			String[] param = params.get(name);
			if (param != null && param.length > 0) {
				String data = param[0];
				if (data == null)
					continue;
				try {
					Field f = o.getClass().getDeclaredField(name);
					int fieldType = getType(f.getType());
					setAccessible(true, f);
					switch (fieldType) {
					case STRING:
						executeMethod(o, name, data);
						break;

					case DATE:
						try {
							executeMethod(o, name, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(data));
						} catch (ParseException e) {
						}
						break;

					case PRIMITIVE:
						setPrimitive(f, o, data);
						break;

					case SHORT:
						if (checkData(data))
							executeMethod(o, name, Short.valueOf(data));
						break;

					case BOOLEAN:
						if (checkData(data))
							executeMethod(o, name, Boolean.valueOf(data));
						break;

					case INTEGER:
						if (checkData(data))
							executeMethod(o, name, Integer.valueOf(data));
						break;

					case LONG:
						if (checkData(data))
							executeMethod(o, name, Long.valueOf(data));
						break;

					case FLOAT:
						if (checkData(data))
							executeMethod(o, name, Float.valueOf(data));
						break;

					case DOUBLE:
						if (checkData(data))
							executeMethod(o, name, Double.valueOf(data));
						break;

					default:
						break;
					}
				} catch (Exception e) {
					logMessage(e.getMessage());
				}
			}
		}
	}

	protected Object executeMethod(Object targetObject, String name, Object data)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Object ret = null;
		final Class<?> paramdef[] = new Class[1];
		paramdef[0] = data.getClass();
		final String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		final Method m = targetObject.getClass().getMethod(methodName, paramdef);
		ret = m.invoke(targetObject, data);
		return ret;
	}

	static final private boolean checkData(String data) {
		if (data == null || data.length() == 0) {
			return false;
		}
		return true;
	}

	static final private void setPrimitive(Field f, Object obj, String data) {

		try {
			if (data == null || data.length() == 0) {
				data = "0";
			}
			final String className = f.getType().getName();
			if (className.compareTo("boolean") == 0) {
				Boolean z = Boolean.valueOf(data);
				f.setBoolean(obj, z.booleanValue());
			} else if (className.compareTo("byte") == 0) {
				Byte b = Byte.valueOf(data);
				f.setByte(obj, b.byteValue());
			} else if (className.compareTo("char") == 0) {
				f.setChar(obj, data.charAt(0));
			} else if (className.compareTo("double") == 0) {
				Double d = Double.valueOf(data);
				f.setDouble(obj, d.doubleValue());
			} else if (className.compareTo("float") == 0) {
				Float flt = Float.valueOf(data);
				f.setFloat(obj, flt.floatValue());
			} else if (className.compareTo("int") == 0) {
				Integer i = Integer.valueOf(data);
				f.setInt(obj, i.intValue());
			} else if (className.compareTo("long") == 0) {
				Long l = Long.valueOf(data);
				f.setLong(obj, l.longValue());
			} else if (className.compareTo("short") == 0) {
				Short s = Short.valueOf(data);
				f.setShort(obj, s.shortValue());
			}
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException in setPrimitive: " + e.getMessage());
		} catch (SecurityException e) {
			System.out.println("SecurityException in setPrimitive: " + e.getMessage());
		}
	}

	private final void setAccessible(boolean b, Field f) {

		try {
			f.setAccessible(b);
		} catch (Exception e) {
		}
	}

	static final private short getType(Class<?> c) {
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
		} catch (Exception e) {
			System.err.println(e);
		}
		return type;
	}

	private void logMessage(String msg) {
		final StringWriter sw = new StringWriter();
		final PrintWriter writer = new PrintWriter(sw);
		writer.println("=============================================");
		writer.println(msg);
		writer.println("=============================================");
		writer.flush();
		logDebugMessage(sw.getBuffer().toString());
	}

	private void logDebugMessage(String msg) {
		// logger.info(msg);
	}
}
