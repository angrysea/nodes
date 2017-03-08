package org.amg.node.servlet;

import java.io.IOException;

import org.amg.node.exception.ServletException;

public interface RequestDispatcher {

	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException;

	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
