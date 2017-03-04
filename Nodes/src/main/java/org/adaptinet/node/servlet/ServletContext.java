package org.adaptinet.node.servlet;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.adaptinet.node.exception.AdaptinetException;

public class ServletContext {
	Properties attributes = new Properties();
	Properties initProps = new Properties();

	public ServletContext() {
	}

	public ServletContext getContext(String uriPath) {
		return this;
	}

	public int getMajorVersion() {
		return 2;
	}

	public int getMinorVersion() {
		return 2;
	}

	public String getMimeType(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.canRead()) {
			if (fileName.endsWith(".jpg"))
				return "image/jpeg";
			else if (fileName.endsWith(".gif"))
				return "image/gif";
			else if (fileName.endsWith(".html"))
				return "text/html";
			else if (fileName.endsWith(".xml"))
				return "text/xml";
			else if (fileName.endsWith(".xsl"))
				return "text/html";
		}
		return "application/octet-stream";
	}

	public URL getResource(String path) throws MalformedURLException {
		return ClassLoader.getSystemResource(path);
	}

	public InputStream getResourceAsStream(String path) {
		return ClassLoader.getSystemResourceAsStream(path);
	}

	public void log(String message, Throwable cause) {
		AdaptinetException ce = new AdaptinetException(AdaptinetException.MIN_SEVERITY, AdaptinetException.GEN_MESSAGE);
		ce.logMessage(message);
	}

	public void log(String message) {
		log(message, null);
	}

	public String getRealPath(String path) {
		File file = new File(path);
		return file.getAbsolutePath();
	}

	public String getServerInfo() {
		return "1.1"; //org.adaptinet.server.XMLServer.VERSION;
	}

	public String getInitParameter(String name) {
		return initProps.getProperty(name);
	}

	public Enumeration getInitParameterNames() {
		return initProps.keys();
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return attributes.keys();
	}

	public void setAttribute(String name, Object attribute) {
		attributes.put(name, attribute);
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public void log(Exception cause, String message) {
		log(message);
	}
}