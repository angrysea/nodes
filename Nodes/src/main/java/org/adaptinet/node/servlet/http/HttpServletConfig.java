package org.adaptinet.node.servlet.http;

import java.util.Enumeration;
import java.util.Properties;

import org.adaptinet.node.servlet.ServletConfig;
import org.adaptinet.node.servlet.ServletContext;

public class HttpServletConfig implements ServletConfig {
	
	private Properties props = new Properties();
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

	public Enumeration getInitParameterNames() {
		return props.keys();
	}
}