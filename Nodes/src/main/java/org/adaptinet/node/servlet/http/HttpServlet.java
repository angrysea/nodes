package org.adaptinet.node.servlet.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.adaptinet.node.exception.BaseException;
import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.servlet.GenericServlet;
import org.adaptinet.node.servlet.ServletOutputStream;
import org.adaptinet.node.servlet.ServletRequest;
import org.adaptinet.node.servlet.ServletResponse;

public class HttpServlet extends GenericServlet implements java.io.Serializable {

	private static final String METHOD_DELETE = "DELETE";
	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_GET = "GET";
	private static final String METHOD_OPTIONS = "OPTIONS";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_TRACE = "TRACE";

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";
	private static final String HEADER_LASTMOD = "Last-Modified";

	private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
	private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

	public HttpServlet() {
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_get_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	protected long getLastModified(HttpServletRequest req) {
		return -1;
	}

	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
		//resp.set.setContentLength();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_post_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_put_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String protocol = req.getProtocol();
		String msg = lStrings.getString("http.method_delete_not_supported");
		if (protocol.endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	private static Method[] getAllDeclaredMethods(Class c) {

		if (c.equals(org.adaptinet.node.servlet.http.HttpServlet.class)) {
			return null;
		}

		Method[] parentMethods = getAllDeclaredMethods(c.getSuperclass());
		Method[] thisMethods = c.getDeclaredMethods();

		if ((parentMethods != null) && (parentMethods.length > 0)) {
			Method[] allMethods = new Method[parentMethods.length + thisMethods.length];
			System.arraycopy(parentMethods, 0, allMethods, 0, parentMethods.length);
			System.arraycopy(thisMethods, 0, allMethods, parentMethods.length, thisMethods.length);

			thisMethods = allMethods;
		}

		return thisMethods;
	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Method[] methods = getAllDeclaredMethods(this.getClass());

		boolean ALLOW_GET = false;
		boolean ALLOW_HEAD = false;
		boolean ALLOW_POST = false;
		boolean ALLOW_PUT = false;
		boolean ALLOW_DELETE = false;
		boolean ALLOW_TRACE = true;
		boolean ALLOW_OPTIONS = true;

		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];

			if (m.getName().equals("doGet")) {
				ALLOW_GET = true;
				ALLOW_HEAD = true;
			}
			if (m.getName().equals("doPost"))
				ALLOW_POST = true;
			if (m.getName().equals("doPut"))
				ALLOW_PUT = true;
			if (m.getName().equals("doDelete"))
				ALLOW_DELETE = true;
		}

		String allow = null;
		if (ALLOW_GET)
			if (allow == null)
				allow = METHOD_GET;
		if (ALLOW_HEAD)
			if (allow == null)
				allow = METHOD_HEAD;
			else
				allow += ", " + METHOD_HEAD;
		if (ALLOW_POST)
			if (allow == null)
				allow = METHOD_POST;
			else
				allow += ", " + METHOD_POST;
		if (ALLOW_PUT)
			if (allow == null)
				allow = METHOD_PUT;
			else
				allow += ", " + METHOD_PUT;
		if (ALLOW_DELETE)
			if (allow == null)
				allow = METHOD_DELETE;
			else
				allow += ", " + METHOD_DELETE;
		if (ALLOW_TRACE)
			if (allow == null)
				allow = METHOD_TRACE;
			else
				allow += ", " + METHOD_TRACE;
		if (ALLOW_OPTIONS)
			if (allow == null)
				allow = METHOD_OPTIONS;
			else
				allow += ", " + METHOD_OPTIONS;

		resp.setHeader("Allow", allow);
	}

	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int responseLength;

		String CRLF = "\r\n";
		String responseString = "TRACE " + req.getRequestURI() + " " + req.getProtocol();

		Enumeration reqHeaderEnum = req.getHeaderNames();

		while (reqHeaderEnum.hasMoreElements()) {
			String headerName = (String) reqHeaderEnum.nextElement();
			responseString += CRLF + headerName + ": " + req.getHeader(headerName);
		}

		responseString += CRLF;

		responseLength = responseString.length();

		resp.setContentType("message/http");
		resp.setContentLength(responseLength);
		ServletOutputStream out = resp.getOutputStream();
		out.print(responseString);
		out.close();
		return;
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String method = req.getMethod();

		if (method.equals(METHOD_GET)) {
			long lastModified = getLastModified(req);
			if (lastModified == -1) {
				// servlet doesn't support if-modified-since, no reason
				// to go through further expensive logic
				doGet(req, resp);
			} else {
				long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
				if (ifModifiedSince < (lastModified / 1000 * 1000)) {
					// If the servlet mod time is later, call doGet()
					// Round down to the nearest second for a proper compare
					// A ifModifiedSince of -1 will always be less
					maybeSetLastModified(resp, lastModified);
					doGet(req, resp);
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
			}

		} else if (method.equals(METHOD_HEAD)) {
			long lastModified = getLastModified(req);
			maybeSetLastModified(resp, lastModified);
			doHead(req, resp);

		} else if (method.equals(METHOD_POST)) {
			doPost(req, resp);

		} else if (method.equals(METHOD_PUT)) {
			doPut(req, resp);

		} else if (method.equals(METHOD_DELETE)) {
			doDelete(req, resp);

		} else if (method.equals(METHOD_OPTIONS)) {
			doOptions(req, resp);

		} else if (method.equals(METHOD_TRACE)) {
			doTrace(req, resp);

		} else {
			//
			// Note that this means NO servlet supports whatever
			// method was requested, anywhere on this server.
			//

			String errMsg = lStrings.getString("http.method_not_implemented");
			Object[] errArgs = new Object[1];
			errArgs[0] = method;
			errMsg = MessageFormat.format(errMsg, errArgs);

			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
		}
	}

	private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
		if (resp.containsHeader(HEADER_LASTMOD))
			return;
		if (lastModified >= 0)
			resp.setDateHeader(HEADER_LASTMOD, lastModified);
	}

	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

		HttpServletRequest request;
		HttpServletResponse response;

		try {
			request = (HttpServletRequest) req;
			response = (HttpServletResponse) res;
		} catch (ClassCastException e) {
			throw new ServletException(BaseException.SEVERITY_FATAL, ServletException.TCV_URLFAILDED);
		}
		service(request, response);
	}
}
