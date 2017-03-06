package org.adaptinet.node.scripting;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Stack;

public class BaseRunner implements ScriptRunner {

	HashMap<String, Object> heap = new HashMap<String, Object>();

	public Object getValue(String name) throws Exception {

		if (name == null || name.isEmpty()) {
			return null;
		}

		try {
			Object value = heap.get(name);
			if (value == null) {
				value = this;
				final String tokens[] = name.split(".");
				for(String token : tokens) {
					value = ScriptProcessor.getValue(value, token);
				}
				heap.put(name, value);
			}
			return value;
		} catch (Exception e) {
			error("Runner getValue for " + name + " exception = "
					+ e.getMessage());
			throw e;
		}
	}

	public Object getValue(String name, int idx) throws Exception {

		if (name == null || name.isEmpty() || idx < 0) {
			return null;
		}

		Object value = this;
		try {
			final String tokens[] = name.split(".");
			for(int i = 0; i < tokens.length; ++i) {
				if (i<tokens.length-1) {
					value = ScriptProcessor.getValue(value, tokens[i]);
				} else {
					value = ScriptProcessor.getValue(value, tokens[i], idx);
				}
			}
		} catch (Exception e) {
			error("Runner getValue for " + name + " exception = "
					+ e.getMessage());
			throw e;
		}
		return value;
	}

	public Object execute(String methodName, Stack<?> callstack) throws Exception {

		int dot = methodName.lastIndexOf(".");
		final String objName = methodName.substring(0, dot);
		final Object obj = getValue(objName);
		final String method = methodName.substring(dot + 1);
		final Object[] args = callstack.toArray();
		
		try {
			return ScriptProcessor.executeMethod(obj, method, args);
		} catch (NoSuchMethodException e) {
			error("Runner executing method (No Such Method) " + methodName
					+ " exception = " + e.getMessage());
			throw e;
		} catch (InvocationTargetException e) {
			error("Runner executing method (Invocation Target) " + methodName
					+ " exception = " + e.getMessage());
			throw e;
		} catch (IllegalAccessException e) {
			error("Runner executing method (Illegal Access) " + methodName
					+ " exception = " + e.getMessage());
			throw e;
		}
	}

	public void setValue(String name, Object value) throws Exception {

		if (name != null && !name.isEmpty()) {
			try {
				Object property = heap.get(name);
				if (property == null) {
					final String tokens[] = name.split(".");
					for(int i = 0; i < tokens.length; ++i) {
						if (i<tokens.length-1) {
							property = ScriptProcessor.getValue(value, tokens[i]);
						} else {
							ScriptProcessor.setValue(property, tokens[i], value);
						}
					}
					heap.put(name, property);
				}
			} catch (Exception e) {
				error("Runner setValue for " + name + " exception = "
						+ e.getMessage());
				throw e;
			}
		}
	}

	public void setValue(String name, int idx, Object value) throws Exception {
		// TODO: set array value
	}

	public Object classForName(String name) {
		return null;
	}

	public void trace(String msg) {

	}

	public void debug(String msg) {

	}

	public void info(String msg) {

	}

	public void error(String msg) {

	}

	public void fatal(String msg) {

	}
}
