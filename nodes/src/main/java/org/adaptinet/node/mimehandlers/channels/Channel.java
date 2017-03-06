package org.adaptinet.node.mimehandlers.channels;

import org.adaptinet.node.servlet.ServletContext;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.servlet.http.HttpServletRequest;
import org.adaptinet.node.servlet.http.HttpServletResponse;

public interface Channel {
	abstract public HttpServletConfig getSc();

	abstract public void init(HttpServletConfig sc, ServletContext context);

	abstract public void setReq(HttpServletRequest req);

	abstract public void setResp(HttpServletResponse resp);

	abstract public void setServletContext(ServletContext context);

	abstract public ServletContext getServletContext();

	abstract public void doGet(String controllerName, String methodName,
			Object[] args);

	abstract public void doPost(String controllerName, String methodName);
	
	abstract public String getMimeType();

}
