package org.adaptinet.node.servlet;

import java.util.Enumeration;

public interface ServletConfig {
	public String getServletName();

	public ServletContext getServletContext();

	public String getInitParameter(String name);

	public Enumeration getInitParameterNames();
}
