package org.amg.node.servlet;

import java.util.Set;

public interface ServletConfig {
	public String getServletName();

	public ServletContext getServletContext();

	public String getInitParameter(String name);

	public Set<String> getInitParameterNames();
}
