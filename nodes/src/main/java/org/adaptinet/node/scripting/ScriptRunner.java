package org.adaptinet.node.scripting;

import java.util.Stack;

public interface ScriptRunner {

	public Object getValue(String name) throws Exception;

	public Object getValue(String name, int idx) throws Exception;

	public Object execute(String methodName, Stack<?> callstack) throws Exception;

	public void setValue(String name, Object value) throws Exception;

	public void setValue(String name, int idx, Object value) throws Exception;

	public Object classForName(String name);

	public void trace(String msg);
	
	public void debug(String msg);
	
	public void info(String msg);

	public void error(String msg);

	public void fatal(String msg);
}