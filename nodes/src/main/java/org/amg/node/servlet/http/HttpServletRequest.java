package org.amg.node.servlet.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.amg.node.http.Request;
import org.amg.node.servlet.Cookie;

public class HttpServletRequest {
	private final static int NEITHER = 0;
	private final static int STREAM = 1;
	private final static int READER = 2;

	private static int state = NEITHER;

	private Request request;
	private Map<String, String[]> formParams;
	private List<Cookie> cookies = new ArrayList<>();
	private Map<String, ? super Object> attributes = new HashMap<>();
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
		return cookies.toArray(ret);
	}

	public long getDateHeader(String name) {
		final String date = request.getProperty(name.toLowerCase());
		if (date == null)
			throw new IllegalArgumentException(name + " header not found.");
		final DateTimeFormatter f = DateTimeFormatter.ISO_DATE_TIME;
		final ZonedDateTime zdt = ZonedDateTime.parse(date, f);
		return zdt.toEpochSecond();
	}

	public String getHeader(String name) {
		return request.getProperty(name.toLowerCase());
	}

	public Set<String> getHeaderNames() {
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
		final String servletPath = (String) getAttribute("com.amg.node.directory");
		final String tokens[] = servletPath.split(";");
		return tokens[0];
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
			session.setNew(false);
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

	public Iterator<String> getAttributeNames() {
		return attributes.keySet().iterator();
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

		if (request.getMethod().equals(Request.GET))
			throw new IllegalStateException("HTTP method is GET");

		final ByteArrayInputStream bis = new ByteArrayInputStream(request.getRequest().getBytes());
		state = STREAM;
		return new HttpServletInputStream(bis);
	}

	public String getParameter(String name) {
		final String parameters[] = formParams.get(name);
		if (parameters != null) {
			return parameters.toString();
		}
		return null;
	}

	public Set<String> getParameterNames() {
		return formParams.keySet();
	}

	public String[] getParameterValues(String name) {
		return formParams.get(name);
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

	public BufferedReader getReader() throws IOException, IllegalStateException {
		if (state == STREAM)
			throw new IllegalStateException("A HttpServletInputStream was already created.");
		if (request.getMethod().equals(Request.GET))
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

	public List<Locale> getLocales() {
		return Arrays.asList(Locale.getDefault());
	}

	public String getLocalesAsString() {
		return Locale.getDefault().toString();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	private Map<String, String[]> parseQueryString() {
		Map<String, String[]> querys = new HashMap<>();
		try {
			final Decoder b64 = Base64.getDecoder();
			final String tokens[] = request.getQueryString().split("&");
			for (String s : tokens) {
				final String elements[] = s.split("=");
				final String name = b64.decode(elements[0]).toString();
				final String value = b64.decode(elements[1]).toString();
				final String values[] =  querys.get(name);
				if(values!=null) {
					final String newValues[] = new String[values.length + 1];
					System.arraycopy(values, 0, newValues, 0, values.length);
					newValues[values.length] = value;
					querys.put(name, newValues);
				}
				else {
					final String newValues[] = new String[1];
					newValues[0] = value;
					querys.put(name, newValues);
				}
			}
		} catch (Exception e) {
		}
		return querys;
	}

	private Map<String, String[]> parsePost() {
		Map<String, String[]> post = new HashMap<>();
		try {
			// not POST
			if (!request.getMethod().equals(Request.POST))
				return post;

			// no POST data
			if (request.getRequest() == null)
				return post;

			// not URL-Encoded
			String content = request.getProperty("content-type");
			if (content != null && !content.equalsIgnoreCase("application/x-www-form-urlencoded"))
				return post;

			final Decoder b64 = Base64.getUrlDecoder();
			final String tokens[] = request.getRequest().split("&");
			for(String s : tokens) {
				String elements[] = s.split("=");
				// here's the tricky part, watch carefully...we have to make
				// String[]'s for
				// each formName, then merge them again with the GET later,
				// yuck
				final String name = b64.decode(elements[0]).toString();
				final String value = b64.decode(elements[1]).toString();
				String values[] = post.get(name);
				if(values!=null) {
					final String newValues[] = new String[values.length + 1];
					System.arraycopy(values, 0, newValues, 0, values.length);
					newValues[values.length] = value;
					post.put(name, newValues);
				}
				else {
					final String newValues[] = new String[1];
					newValues[0] = value;
					post.put(name, newValues);
				}
			}
		} catch (Exception e) {
		}
		return post;
	}

	private Map<String, String[]> mergeParameters(Map<String, String[]> source, Map<String, String[]> dest) {
		return Stream.of(source, dest)
					 .map(Map::entrySet)
					 .flatMap(Collection::stream)
					 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private void parseCookies() {
		String cookieString = request.getProperty("cookie");
		if (cookieString == null)
			return;

		StringTokenizer st = new StringTokenizer(cookieString, ";");
		while (st.hasMoreElements()) {
			try {
				StringTokenizer tok = new StringTokenizer((String) st.nextElement(), "=");
				
				Base64.Decoder decoder = Base64.getUrlDecoder(); 
				String name = decoder.decode(tok.nextToken().trim()).toString();
				String value = decoder.decode(tok.nextToken().trim()).toString();
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