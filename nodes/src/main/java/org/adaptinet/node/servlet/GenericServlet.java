package org.adaptinet.node.servlet;

import java.io.IOException;
import java.util.Set;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.servlet.http.HttpServletConfig;

public abstract class GenericServlet implements Servlet, ServletConfig, java.io.Serializable {
	private static final long serialVersionUID = 1128919148883348623L;
	private transient HttpServletConfig config;

	public GenericServlet() {
	}

	public void destroy() {
	}

	public String getInitParameter(String name) {
		return getServletConfig().getInitParameter(name);
	}

	public Set<String> getInitParameterNames() {
		return getServletConfig().getInitParameterNames();
	}

	public HttpServletConfig getServletConfig() {
		return config;
	}

	public ServletContext getServletContext() {
		return getServletConfig().getServletContext();
	}

	public String getServletInfo() {
		return "";
	}

	public void init(HttpServletConfig config) throws ServletException {
		this.config = config;
		this.init();
	}

	public void init() throws ServletException {

	}

	public void log(String msg) {
		getServletContext().log(getServletName() + ": " + msg);
	}

	public void log(String message, Throwable t) {
		getServletContext().log(getServletName() + ": " + message, t);
	}

	public abstract void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

	public String getServletName() {
		return config.getServletName();
	}

}
