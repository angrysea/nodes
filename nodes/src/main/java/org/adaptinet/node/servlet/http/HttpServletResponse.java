package org.adaptinet.node.servlet.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.http.HTTP;
import org.adaptinet.node.http.Response;
import org.adaptinet.node.servlet.Cookie;

public class HttpServletResponse {
	public final static int INITIAL = 0;
	public final static int FLUSHED = 1;
	public final static int DONE = 2;
	public final static int CLOSED = 3;

	private final static int NEITHER = 0;
	private final static int WRITER = 1;
	private final static int STREAM = 2;

	public static final int SC_CONTINUE = 100;
	public static final int SC_SWITCHING_PROTOCOLS = 101;
	public static final int SC_OK = 200;
	public static final int SC_CREATED = 201;
	public static final int SC_ACCEPTED = 202;
	public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
	public static final int SC_NO_CONTENT = 204;
	public static final int SC_RESET_CONTENT = 205;
	public static final int SC_PARTIAL_CONTENT = 206;
	public static final int SC_MULTIPLE_CHOICES = 300;
	public static final int SC_MOVED_PERMANENTLY = 301;
	public static final int SC_MOVED_TEMPORARILY = 302;
	public static final int SC_FOUND = 302;
	public static final int SC_SEE_OTHER = 303;
	public static final int SC_NOT_MODIFIED = 304;
	public static final int SC_USE_PROXY = 305;
	public static final int SC_TEMPORARY_REDIRECT = 307;
	public static final int SC_BAD_REQUEST = 400;
	public static final int SC_UNAUTHORIZED = 401;
	public static final int SC_PAYMENT_REQUIRED = 402;
	public static final int SC_FORBIDDEN = 403;
	public static final int SC_NOT_FOUND = 404;
	public static final int SC_METHOD_NOT_ALLOWED = 405;
	public static final int SC_NOT_ACCEPTABLE = 406;
	public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
	public static final int SC_REQUEST_TIMEOUT = 408;
	public static final int SC_CONFLICT = 409;
	public static final int SC_GONE = 410;
	public static final int SC_LENGTH_REQUIRED = 411;
	public static final int SC_PRECONDITION_FAILED = 412;
	public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
	public static final int SC_REQUEST_URI_TOO_LONG = 414;
	public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
	public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	public static final int SC_EXPECTATION_FAILED = 417;
	public static final int SC_INTERNAL_SERVER_ERROR = 500;
	public static final int SC_NOT_IMPLEMENTED = 501;
	public static final int SC_BAD_GATEWAY = 502;
	public static final int SC_SERVICE_UNAVAILABLE = 503;
	public static final int SC_GATEWAY_TIMEOUT = 504;
	public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	private int outputType = NEITHER;
	private Response response;
	private HttpServletOutputStream sos;
	private int outputState = INITIAL;
	private PrintWriter pw;
	private Locale locale = Locale.getDefault();
	private String contentType;
	private int contentLen = 0;
	private String statMessage;
	private int status = HTTP.OK;
	private String redirectUrl;
	private List<Cookie> cookies = new ArrayList<>();
	private Map<String, String> headers = new HashMap<>();
	private HttpServletRequest request = null;

	public HttpServletResponse(Response r) {
		response = r;
	}

	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	public boolean containsHeader(String name) {
		if (headers.get(name) == null)
			return false;
		return true;
	}

	public void setDateHeader(String name, long date) {
		java.util.Date d = new java.util.Date(date);
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd MMM, yyyy hh:mm:ss zzz");
		headers.put(name, df.format(d));
	}

	public void addDateHeader(String name, long date) {
		java.util.Date d = new java.util.Date(date);
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd MMM, yyyy hh:mm:ss zzz");
		headers.put(name, df.format(d));
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void setIntHeader(String name, int value) {
		headers.put(name, Integer.toString(value));
	}

	public void addIntHeader(String name, int value) {
		headers.put(name, Integer.toString(value));
	}

	public void sendError(int code, String message) throws IOException {
		if (sos == null)
			getOutputStream();
		else
			sos.reset();

		status = code;
		statMessage = message;
		sos.print(statMessage);
		sos.close();
		outputState = CLOSED;
	}

	public void sendError(int code) throws IOException {
		sendError(code, "");
	}

	public void sendRedirect(String url) throws IOException {
		if (sos == null)
			getOutputStream();
		else
			sos.reset();

		status = HTTP.FOUND;
		this.redirectUrl = url;
		sos.close();
		outputState = CLOSED;
	}

	public void setStatus(int code) {
		status = code;
	}

	public void setStatus(int code, String message) {
		status = code;
		statMessage = message;
	}

	public void setContentLength(int len) {
		contentLen = len;
	}

	public void setContentType(String type) {
		contentType = type;
	}

	public String encodeURL(String url) {
		return encodeUrl(url);
	}

	public String encodeRedirectURL(String url) {
		return encodeRedirectUrl(url);
	}

	public String encodeUrl(String url) {
		return Base64.getUrlEncoder().encode(url.getBytes()).toString();
	}

	public String encodeRedirectUrl(String url) {
		return Base64.getUrlEncoder().encode(url.getBytes()).toString();
	}

	public String getCharacterEncoding() {
		return "UTF-8";
	}

	public HttpServletOutputStream getOutputStream() throws IOException {
		if (outputType == WRITER)
			throw new IllegalStateException("PrintWriter was already allocated for the output stream.");

		if (sos == null) {
			sos = new HttpServletOutputStream(this, response, 4096, false);
			outputState = INITIAL;
			outputType = STREAM;
		}
		return this.sos;
	}

	public PrintWriter getWriter() throws IOException {
		if (outputType == STREAM)
			throw new IllegalStateException("The output stream was already allocated.");

		if (sos == null) {
			sos = new HttpServletOutputStream(this, response, 4096, true);
			outputState = INITIAL;
			outputType = WRITER;
		}
		pw = new PrintWriter(sos);
		return pw;
	}

	public void setBufferSize(int len) {
		// TODO: not possible...yet
	}

	public int getBufferSize() {
		return sos.buffer.length;
	}

	public void flushBuffer() throws IOException {
		sos.flush();
	}

	public boolean isCommitted() {
		return (outputState > INITIAL);
	}

	public void reset() {
		if (outputType == STREAM)
			sos.reset();
	}

	public void close() {
		try {
			if (outputType == WRITER)
				pw.close();
			else
				sos.close();
		} catch (Exception e) {
		}
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	protected void writeHeaders(DataOutputStream out) {
		try {
			// HTTP/1.0 200 OK
			out.write(HTTP.byteArrayVersion);
			out.write(Integer.toString(status).getBytes());
			if (status < 200)
				out.write(HTTP.msg100[status - 100].getBytes());
			else if (status < 300)
				out.write(HTTP.msg200[status - 200].getBytes());
			else if (status < 400)
				out.write(HTTP.msg300[status - 300].getBytes());
			else if (status < 500)
				out.write(HTTP.msg400[status - 400].getBytes());
			else if (status < 600)
				out.write(HTTP.msg500[status - 500].getBytes());
			out.write(HTTP.crlf.getBytes());

			if (status > 299 && status < 400 && redirectUrl != null) { // Redirect...
				out.write(HTTP.redirect.getBytes());
				out.write(redirectUrl.getBytes());
				out.write(HTTP.crlf.getBytes());
			}

			// Date
			out.write(HTTP.date.getBytes());
			out.write(getNow().getBytes());
			out.write(HTTP.crlf.getBytes());

			// Server:
			out.write(HTTP.server.getBytes());
			out.write(HTTP.crlf.getBytes());

			out.write("Connection: close".getBytes());
			out.write(HTTP.crlf.getBytes());

			if (status == HTTP.INTERNAL_SERVER_ERROR) {
				out.write(HTTP.contentTypeHTML.getBytes());
				out.write(HTTP.crlf.getBytes());
				out.write(HTTP.crlf.getBytes());
				// out.write(this.statMessage.getBytes());
				out.flush();
				outputState = FLUSHED;
				return;
			}

			// Headers
			for(String key : headers.keySet()) {
				out.write(key.getBytes());
				out.write(": ".getBytes());
				out.write(headers.get(key).getBytes());
			}

			// session cookie
			if (request != null && request.getSession() != null) {
				Cookie c = request.getSessionCookie();
				if (c != null)
					addCookie(c);
			}

			// Cookies
			for(Cookie c : cookies) {
				out.write(cookieToString(c).getBytes());
				out.write(HTTP.crlf.getBytes());
			}

			// Content-Type
			if (contentType != null) {
				out.write("Content-Type: ".getBytes());
				out.write(contentType.getBytes());
				out.write(HTTP.crlf.getBytes());
			}

			// Content-Length
			if (contentLen > 0) {
				out.write("Content-Length: ".getBytes());
				out.write(Integer.toString(contentLen).getBytes());
				out.write(HTTP.crlf.getBytes());
			}

			// end
			out.write(HTTP.crlf.getBytes());
			out.flush();
			outputState = FLUSHED;
		} catch (IOException ioe) {
			AdaptinetException c360ex = new AdaptinetException(AdaptinetException.SEVERITY_ERROR,
					AdaptinetException.FACILITY_HTTP, 999);
			c360ex.logMessage(ioe);
		}
	}

	private String getNow() {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz");
		return df.format(new Date());
	}

	private String getExpires(int numMins) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, numMins);
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd-MMM-yy hh:mm:ss zzz");
		return df.format(cal.getTime());
	}

	private String cookieToString(Cookie c) {
		try {
			StringBuilder cookie = new StringBuilder("Set-Cookie: ");
			cookie.append(Base64.getUrlEncoder().encode(c.getName().getBytes()));
			cookie.append("=");
			cookie.append(Base64.getUrlEncoder().encode(c.getValue().getBytes()));
			if (c.getMaxAge() > 0)
				cookie.append(";EXPIRES=" + getExpires(c.getMaxAge()));
			if (c.getDomain() != null)
				cookie.append(";DOMAIN=" + c.getDomain());
			if (c.getPath() != null)
				cookie.append(";PATH=" + c.getPath());
			if (c.getSecure())
				cookie.append(";SECURE");
			return cookie.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public void setRequest(HttpServletRequest req) {
		this.request = req;
	}
}