package org.adaptinet.node.servlet.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.adaptinet.node.servlet.ServletConfig;
import org.adaptinet.node.servlet.ServletContext;

public class HttpServletConfig implements ServletConfig {
	
	private Map<String, String> props = new HashMap<>();
	private String servletName;
	private ServletContext servletContext;

	public HttpServletConfig(ServletContext s, String name) {
		servletContext = s;
		servletName = name;
	}

	public String getServletName() {
		return servletName;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getInitParameter(String name) {
		try {
			return (String) props.get(name);
		} catch (ClassCastException cce) {
			return null;
		}
	}

	public Set<String> getInitParameterNames() {
		return props.keySet();
	}
}