package org.adaptinet.node.mimehandlers.servlets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.mimehandlers.channels.Channel;
import org.adaptinet.node.mimehandlers.utils.RestPropertiesUtil;
import org.adaptinet.node.servlet.Cookie;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.servlet.http.HttpServletRequest;
import org.adaptinet.node.servlet.http.HttpServletResponse;
import org.adaptinet.node.servlet.http.xml.XMLHttpServlet;

@SuppressWarnings("serial")
public class ChannelServlet extends XMLHttpServlet {

	public static final String INITIALCONTEXT = "InitialContext";
	public static final String DEBUGFLAG = "Debug.Flag";
	public static final String DEFAULTAPP = "Default.App";	
	public static final String PARAM_XMLSVC_VERBOSE = "xmlsvc-verbose";
	public boolean debug = false;
	public boolean defaultApp = false;

	private int PARAM_START = 5;
	private int CONTROLLER_POS = 3;
	private static final String LOG_RESPONSE_HEADER = "log.response.header";
	private HttpServletConfig sc = null;
	//private final Logger logger = LoggerFactory.getLogger(ChannelServlet.class);

	@Override
	public void init(HttpServletConfig sc) throws ServletException {
		super.init(sc);
		this.sc = sc;
		String debug = sc.getInitParameter(DEBUGFLAG);
		if (debug != null && debug.equalsIgnoreCase("TRUE")) {
			this.debug = true;
		}
		String defApp = sc.getInitParameter(DEFAULTAPP);
		if (defApp == null || defApp.equalsIgnoreCase("TRUE")) {
			this.defaultApp = true;
			CONTROLLER_POS = 3;
			PARAM_START = 5;
		} else {
			this.defaultApp = false;
			CONTROLLER_POS = 2;
			PARAM_START = 4;
		}

		ServicesConfig.init(sc, getServletContext());
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void service(HttpServletRequest req, HttpServletResponse resp) {

		if (debug)
			logTransaction(req);

		// Initialize the path variables
		String buffer = req.getContextPath() + req.getServletPath()
				+ req.getPathInfo();
		int dot = buffer.lastIndexOf('.');
		String channelName = null;
		if (dot > 0) {
			channelName = buffer.substring(dot + 1).toUpperCase();
			buffer = buffer.substring(0, dot);
		} else {
			// Default to XML
			channelName = "XML";
		}
		String s[] = buffer.split("/");

		if (s != null && s.length > CONTROLLER_POS) {
			String controllerName = s[CONTROLLER_POS];
			String methodName = s[CONTROLLER_POS + 1];

			if (controllerName != null && controllerName.length() > 0
					&& methodName != null && methodName.length() > 0) {
				try {
					Class channelClass = Class
							.forName("com.db.canvas.channels." + channelName
									+ "Channel");
					Channel channel = (Channel) channelClass.newInstance();
					channel.init(sc, getServletContext());
					channel.setReq(req);
					channel.setResp(resp);

					Object args[] = null;
					HashMap<String, String[]> params = new HashMap<String, String[]>(); // = req.getParameterMap();
					int r = s.length - PARAM_START;
					int len = r;

					boolean hasParams = false;
					if (params.size() > 0) {
						len++;
						hasParams = true;
					}

					if (s.length - r > len) {
						args = new Object[len];
						for (int i = 0; i < r; i++) {
							args[i] = s[i + PARAM_START];
						}
						if (hasParams) {
							args[len - 1] = params;
						}
					}
					if (req.getMethod() == "GET") {
						channel.doGet(controllerName, methodName, args);
					} else {
						channel.doPost(controllerName, methodName);
					}
				} catch (ClassNotFoundException e) {
					//logger.error("Invalid channel : " + channelName);
					setError(resp, "Invalid channel : " + channelName);
				} catch (Exception e) {
					//logger.error("Processing request, ex=" + e.getMessage(), e);
					setError(resp, e.getMessage());
				}
			} else {
				//logger.error("Malformed URL either controller name or method is missing.");
			}
		} else {
			//logger.error("Malformed URL unable to interpret URL.");
		}
	}

	private void setError(HttpServletResponse resp, String msg) {
		String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><servlet>"
				+ this.getClass().getName()
				+ "</servlet><code>1</code><desc>"
				+ msg
				+ "</desc><timestamp>"
				+ (new java.util.Date(System.currentTimeMillis()).toString())
				+ "</timestamp></status>";
		try {
			resp.addHeader("CANVAS-Error-Message: ", result);
			resp.sendError(601);
		} catch (Exception ioe) {
			//logger.error(
			//		"XMLSVC: Sending HTTP 601 CANVAS-Error-Message response : "
			//				+ result + ", ex=" + ioe.getMessage(), ioe);
		}
	}

	@SuppressWarnings("rawtypes")
	public void logTransaction(HttpServletRequest request) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		// writer.println("Request Received at "
		// + (new Timestamp(System.currentTimeMillis())));

		writer.print("XMLSVC: ");
		writer.println(" characterEncoding=" + request.getCharacterEncoding());
		writer.println("     contentLength=" + request.getContentLength());
		//logger.info(" contentLength of ChannelServlet.java");
		String logResponseHeader = RestPropertiesUtil.getInstance().getProperty(LOG_RESPONSE_HEADER);
		
		//logger.info("logResponseHeader reade from properties file >> "+logResponseHeader);
		
		if(logResponseHeader != null && "true".equals(logResponseHeader)){
		writer.println("       contentType=" + request.getContentType());
		writer.println("            locale=" + request.getLocale());
		writer.print("           locales=");
		Enumeration locales = request.getLocales();
		boolean first = true;
		while (locales.hasMoreElements()) {
			Locale locale = (Locale) locales.nextElement();
			if (first)
				first = false;
			else
				writer.print(", ");
			writer.print(locale.toString());
		}

		writer.println();

		Enumeration names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			writer.print("         parameter " + name + "=");
			String values[] = request.getParameterValues(name);
			for (int i = 0; i < values.length; i++) {
				if (i > 0)
					writer.print(", ");
				writer.print(values[i]);
			}
			writer.println();
		}
		writer.println("          protocol=" + request.getProtocol());
		writer.println("        remoteAddr=" + request.getRemoteAddr());
		writer.println("        remoteHost=" + request.getRemoteHost());
		writer.println("            Scheme=" + request.getScheme());
		writer.println("        serverName=" + request.getServerName());
		writer.println("        serverPort=" + request.getServerPort());
		writer.println("          isSecure=" + request.isSecure());

		if (request instanceof HttpServletRequest) {
			writer.println("---------------------------------------------");
			HttpServletRequest hrequest = (HttpServletRequest) request;
			writer.println("       contextPath=" + hrequest.getContextPath());
			Cookie cookies[] = hrequest.getCookies();
			if (cookies == null)
				cookies = new Cookie[0];
			for (int i = 0; i < cookies.length; i++) {
				writer.println("            cookie=" + cookies[i].getName()
						+ "=" + cookies[i].getValue());
			}

			names = hrequest.getHeaderNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				String value = hrequest.getHeader(name);
				writer.println("            header=" + name + "=" + value);
			}

			writer.println("            method=" + hrequest.getMethod());
			writer.println("          pathInfo=" + hrequest.getPathInfo());
			writer.println("       queryString=" + hrequest.getQueryString());
			writer.println("        remoteUser=" + hrequest.getRemoteUser());
			writer.println("requestedSessionId="
					+ hrequest.getRequestedSessionId());
			writer.println("        requestURI=" + hrequest.getRequestURI());
			writer.println("       servletPath=" + hrequest.getServletPath());
		}
		}
		writer.println("=============================================");

		writer.flush();
		logMessage(sw.getBuffer().toString());
	}

	protected void sendErrorMessage(String msg, HttpServletResponse response) {
		sendErrorMessage(msg, null, response);
	}

	protected void sendErrorMessage(Exception e, HttpServletResponse response) {
		sendErrorMessage(null, e, response);
	}

	public void sendErrorMessage(String msg, Exception e,
			HttpServletResponse response) {

		try {
			//logger.error("XMLSVC: Sending error msg: " + msg + ",ex="
			//		+ e.getMessage());
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><broker>");
			out.println(getClass().getName());
			out.println("</broker><code>1</code><desc>");
			if (msg != null)
				out.println(msg);
			if (e != null) {
				out.println(e.getMessage());
				e.printStackTrace(out);
			}
			out.println("</desc><timestamp>"
					+ new java.util.Date(System.currentTimeMillis()).toString()
					+ "</timestamp></status>");
		} catch (Exception ex) {
			//logger.error("XMLSVC: Sending error message: " + msg + ", origex="
			//		+ e.getMessage() + ", newex=" + ex.getMessage(), ex);
		}
	}

	public void logMessage(String msg) {
		//logger.info(msg);
	}
}
