package org.adaptinet.node.mimehandlers.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.mimehandlers.controllers.Controller;
import org.adaptinet.node.servlet.Cookie;
import org.adaptinet.node.servlet.ServletRequest;
import org.adaptinet.node.servlet.ServletResponse;
import org.adaptinet.node.servlet.http.HttpServlet;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.servlet.http.HttpServletRequest;
import org.adaptinet.node.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ControllerServlet extends HttpServlet {

	public static final String INITIALCONTEXT = "InitialContext";
	public static final String DEBUGFLAG = "Debug.Flag";
	public static final int PARAM_START = 4;
	private boolean debug = false;
	// private final Logger logger = LoggerFactory
	// .getLogger(ControllerServlet.class);

	@Override
	public void init(HttpServletConfig sc) throws ServletException {
		super.init(sc);
		String debug = sc.getInitParameter(DEBUGFLAG);
		if (debug != null && debug.equalsIgnoreCase("TRUE")) {
			this.debug = true;
		}
		ServicesConfig.init(sc, getServletContext());
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {

		try {
			// if (debug)
			// logTransaction(req);

			// Initialize the path variables
			String buffer = req.getContextPath() + req.getServletPath() + req.getPathInfo();
			String s[] = buffer.split("/");
			String controllerName = s[2];
			String methodName = s[3];

			Class controllerClass = ServicesConfig.getController(controllerName);
			Controller controller = (Controller) controllerClass.newInstance();

			Object args[] = null;
			@SuppressWarnings("unchecked")
			HashMap<String, String[]> params = new HashMap<String, String[]>(); // =
																					// req.getParameterMap();
			int r = s.length - PARAM_START;
			int len = r;

			boolean hasParams = false;

			if (params.size() > 0) {
				len++;
				hasParams = true;
			}

			if (len > 0) {
				args = new Object[len];
				for (int i = 0; i < r; i++) {
					args[i] = s[i + PARAM_START];
				}
				if (hasParams) {
					args[len - 1] = params;
				}
			}

			controller.setReq(req);
			controller.setResp(resp);
			controller.setServletContext(getServletContext());
			controller.setViewClassName("/views/" + controllerName.toLowerCase() + "/" + methodName + ".jsp");
			executeMethod(controller, methodName, args);

		} catch (Exception e) {
			String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><servlet>" + this.getClass().getName()
					+ "</servlet><code>1</code><desc>" + e.getMessage() + "</desc><timestamp>"
					+ (new java.util.Date(System.currentTimeMillis()).toString()) + "</timestamp></status>";
			try {
				(resp).addHeader("JPM-Error-Message: ", result);
				(resp).sendError(601);
			} catch (Exception ioe) {
				logDebugMessage("JPM-Error-Message: " + result);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected Object executeMethod(Object targetObject, String name, Object args[]) throws Exception {
		Object ret = null;

		for (Method method : targetObject.getClass().getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name)) {
				boolean bExecute = true;
				Class pvec[] = method.getParameterTypes();

				if (args == null) {
					if (pvec != null && pvec.length != 0) {
						bExecute = false;
					}
				} else if (pvec.length == args.length) {
					bExecute = true;
					for (int i = 0; i < pvec.length; i++) {
						if (pvec[i] != args[i].getClass()) {
							bExecute = false;
							break;
						}
					}
				}
				if (bExecute) {
					ret = method.invoke(targetObject, args);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	public void logTransaction(ServletRequest request) {

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		// writer.println("Request Received at "
		// + (new Timestamp(System.currentTimeMillis())));

		writer.println("--------- From XMLServlet --------");
		writer.println(" characterEncoding=" + request.getCharacterEncoding());
		writer.println("     contentLength=" + request.getContentLength());
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
				writer.println("            cookie=" + cookies[i].getName() + "=" + cookies[i].getValue());
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
			writer.println("requestedSessionId=" + hrequest.getRequestedSessionId());
			writer.println("        requestURI=" + hrequest.getRequestURI());
			writer.println("       servletPath=" + hrequest.getServletPath());
		}

		writer.println("=============================================");

		writer.flush();
		logDebugMessage(sw.getBuffer().toString());
	}

	protected void sendErrorMessage(String msg, ServletResponse response) {
		sendErrorMessage(msg, null, response);
	}

	protected void sendErrorMessage(Exception e, ServletResponse response) {
		sendErrorMessage(null, e, response);
	}

	public void sendErrorMessage(String msg, Exception e, ServletResponse response) {

		try {
			logDebugMessage(msg + " " + e.getMessage());
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
			out.println("</desc><timestamp>" + new java.util.Date(System.currentTimeMillis()).toString()
					+ "</timestamp></status>");
		} catch (Exception ex) {
			// Really nothing we can do.
		}
	}

	public void logDebugMessage(String msg) {
		//logger.debug(msg);
	}
}
