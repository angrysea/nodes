package org.adaptinet.node.servlet;

import java.io.IOException;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.servlet.http.HttpServletConfig;

public interface Servlet {
	public void init(HttpServletConfig config) throws ServletException;

	public HttpServletConfig getServletConfig();

	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

	public String getServletInfo();

	public void destroy();
}
