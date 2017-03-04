package org.adaptinet.node.servlet.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.adaptinet.nnode.http.Request;
import org.adaptinet.node.servlet.Cookie;

public class HttpServletRequest {
	private final static int NEITHER = 0;
	private final static int STREAM = 1;
	private final static int READER = 2;

	private static int state = NEITHER;

	private Request request;
	private Hashtable formParams;
	private Vector cookies = new Vector();
	private Properties attributes = new Properties();
	private String sessionId;
	private HttpSession session = null;

	public HttpServletRequest(Request r) {
		request = r;
		formParams = mergeParameters(parseQueryString(), parsePost());
		parseCookies();
	}

	public String getAuthType() {
		return request.getProperty("authorization");
	}

	public Cookie[] getCookies() {
		Cookie[] ret = new Cookie[cookies.size()];
		for (int x = 0; x < ret.length; x++)
			ret[x] = (Cookie) cookies.elementAt(x);
		return ret;
	}

	public long getDateHeader(String name) {
		String date = request.getProperty(name.toLowerCase());
		if (date == null)
			throw new IllegalArgumentException(name + " header not found.");
		try {
			Date d = SimpleDateFormat.getInstance().parse(date);
			return d.getTime();
		} catch (java.text.ParseException pe) {
			throw new IllegalArgumentException(date + " is not a valid Date.");
		}
	}

	public String getHeader(String name) {
		return request.getProperty(name.toLowerCase());
	}

	public Enumeration getHeaders(String name) {
		Vector v = new Vector(1);
		v.add(request.getProperty(name));
		return v.elements();
	}

	public Enumeration getHeaderNames() {
		return request.getHeaderNames();
	}

	public int getIntHeader(String name) {
		String integer = request.getProperty(name);
		if (integer == null)
			throw new IllegalArgumentException("No header " + name + " is available.");
		return Integer.parseInt(integer);
	}

	public String getMethod() {
		return request.getMethod();
	}

	public String getPathTranslated() {
		try {
			String servletPath = (String) getAttribute("com.adaptinet.servlet.directory");
			int semi = servletPath.indexOf(';');
			if (semi > 0)
				return servletPath.substring(0, semi);
			return servletPath;
		} catch (Exception e) {
			return "";
		}
	}

	public String getRealPath(String name) {
		String path = getPathTranslated();
		if (!path.endsWith(File.separator))
			path += File.separator;

		return path + name;
	}

	public String getContextPath() {
		return "/servlet"; // hard-code for now...unimportant
	}

	public String getServletPath() {
		return "/";
	}

	public String getPathInfo() {
		try {
			String path = getRequestURI();
			if (path == null)
				return null;

			int servlet = path.indexOf(getContextPath());
			if (servlet == -1)
				return null;

			servlet += getContextPath().length() + 1;
			return path.substring(servlet);
		} catch (Exception e) {
		}
		return null;
	}

	public String getRequestURI() {
		return request.getRequestURI();
	}

	public String getQueryString() {
		return request.getQueryString();
	}

	public String getRemoteUser() {
		return request.getUsername();
	}

	public boolean isUserInRole(String name) {
		String user = request.getUsername();
		if (user != null && user.equals(name))
			return true;
		return false;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public String getRequestedSessionId() {
		if (session != null)
			return session.getId();
		return null;
	}

	public HttpSession getSession(boolean create) {
		if (session == null && create == true) {
			session = new HttpSession();
			this.sessionId = session.getId();
		}
		return session;
	}

	public HttpSession getSession() {
		if (session != null)
			((HttpSession) session).setNew(false);
		return session;
	}

	public boolean isRequestedSessionIdValid() {
		return (session != null);
	}

	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	// deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return attributes.keys();
	}

	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	public int getContentLength() {
		String len = request.getProperty("content-length");
		if (len == null)
			return 0;
		return Integer.parseInt(len);
	}

	public String getContentType() {
		return request.getProperty("content-type");
	}

	public HttpServletInputStream getInputStream() throws IOException {
		if (state == READER)
			throw new IllegalStateException("A BufferedReader was already created.");

		if (request.getMethod().equals(request.GET))
			throw new IllegalStateException("HTTP method is GET");

		ByteArrayInputStream bis = new ByteArrayInputStream(request.getRequest().getBytes());
		state = STREAM;
		return new HttpServletInputStream(bis);
	}

	public String getParameter(String name) {
		Object o = formParams.get(name);
		if (o == null)
			return null;

		if (o instanceof String[]) {
			String[] ret = (String[]) o;
			return ret[0];
		}
		return (String) o;
	}

	public Enumeration getParameterNames() {
		return formParams.keys();
	}

	public String[] getParameterValues(String name) {
		Object o = formParams.get(name);
		if (o == null)
			return null;

		if (o instanceof String) {
			String[] s = new String[1];
			s[0] = (String) o;
			return s;
		}

		return (String[]) o;
	}

	public String getProtocol() {
		return null;
	}

	public String getScheme() {
		return "Cookies";
	}

	public String getServerName() {
		return request.getHost();
	}

	public int getServerPort() {
		return request.getServer().getPort();
	}

	public BufferedReader getReader() throws IOException {
		if (state == STREAM)
			throw new IllegalStateException("A HttpServletInputStream was already created.");

		if (request.getMethod().equals(request.GET))
			throw new IllegalStateException("The HTTP method was GET.");

		state = READER;
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getRequest().getBytes())));
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public void setAttribute(String name, Object attribute) {
		attributes.put(name, attribute);
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public Locale getLocale() {
		return Locale.getDefault();
	}

	public Enumeration getLocales() {
		Vector v = new Vector(1);
		v.add(Locale.getDefault());
		return v.elements();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	private Hashtable parseQueryString() {
		Hashtable get = new Hashtable();
		try {
			StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
			while (st.hasMoreElements()) {
				StringTokenizer s = new StringTokenizer((String) st.nextElement(), "=");
				try {
					String name = (String) s.nextElement();
					String value = (String) s.nextElement();
					name = URLDecoder.decode(name);
					value = URLDecoder.decode(value);
					Object o = get.get(name);
					if (o == null) {
						get.put(name, value);
					} else if (o instanceof String) {
						String newValues[] = new String[2];
						newValues[0] = (String) o;
						newValues[1] = value;
						get.put(name, newValues);
					} else if (o instanceof String[]) {
						String oldValues[] = (String[]) o;
						String newValues[] = new String[oldValues.length + 1];
						System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
						newValues[oldValues.length] = value;
						get.put(name, newValues);
					}
				} catch (Exception une) {
				} // if this happens, the browser has misbehaved badly
			}
		} catch (Exception e) {
		}
		return get;
	}

	private Hashtable parsePost() {
		Hashtable post = new Hashtable();
		try {
			// not POST
			if (!request.getMethod().equals(request.POST))
				return post;

			// no POST data
			if (request.getRequest() == null)
				return post;

			// not URL-Encoded
			String content = request.getProperty("content-type");
			if (content != null && !content.equalsIgnoreCase("application/x-www-form-urlencoded"))
				return post;

			StringTokenizer st = new StringTokenizer(request.getRequest(), "&");
			while (st.hasMoreElements()) {
				StringTokenizer s = new StringTokenizer((String) st.nextElement(), "=");
				try {
					// here's the tricky part, watch carefully...we have to make
					// String[]'s for
					// each formName, then merge them again with the GET later,
					// yuck
					String name = (String) s.nextElement();
					String value = (String) s.nextElement();
					name = URLDecoder.decode(name);
					value = URLDecoder.decode(value);
					Object o = post.get(name);
					if (o == null) {
						post.put(name, value);
					} else if (o instanceof String) {
						String newValues[] = new String[2];
						newValues[0] = (String) o;
						newValues[1] = value;
						post.put(name, newValues);
					} else if (o instanceof String[]) {
						String oldValues[] = (String[]) o;
						String newValues[] = new String[oldValues.length + 1];
						System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
						newValues[oldValues.length] = value;
						post.put(name, newValues);
					}
				} catch (Exception une) {
				} // if this happens, the browser has misbehaved badly
			}
		} catch (Exception e) {
		}
		return post;
	}

	private Hashtable mergeParameters(Hashtable source, Hashtable dest) {
		if (source == null)
			return dest;

		if (dest != null) {
			Enumeration e = source.keys();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				Object value = dest.get(name);
				if (value == null) {
					dest.put(name, source.get(name));
				} else if (value instanceof String[]) {
					String oldValues[] = (String[]) value;
					String newValues[] = new String[oldValues.length + 1];
					System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
					newValues[oldValues.length] = (String) source.get(name);
					dest.put(name, newValues);
				} else {
					String newValues[] = new String[2];
					newValues[0] = (String) source.get(name);
					newValues[1] = (String) value;
					dest.put(name, newValues);
				}
			}
			return dest;
		} else {
			return source;
		}
	}

	private void parseCookies() {
		String cookieString = request.getProperty("cookie");
		if (cookieString == null)
			return;

		StringTokenizer st = new StringTokenizer(cookieString, ";");
		while (st.hasMoreElements()) {
			try {
				StringTokenizer tok = new StringTokenizer((String) st.nextElement(), "=");
				String name = URLDecoder.decode(((String) tok.nextElement()).trim());
				String value = URLDecoder.decode(((String) tok.nextElement()).trim());
				if (name.equals("JSESSIONID")) {
					sessionId = value;
				} else {
					Cookie cookie = new Cookie(name, value);
					cookies.add(cookie);
				}
			} catch (Exception e) {
			} // browser misbehavior
		}
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSession(HttpSession session) {
		this.session = session;
		if (session != null)
			this.sessionId = session.getId();
	}

	public Cookie getSessionCookie() {
		if (session == null)
			return null;

		Cookie s = new Cookie("JSESSIONID", sessionId);
		s.setMaxAge(session.getMaxInactiveInterval());
		return s;
	}
}