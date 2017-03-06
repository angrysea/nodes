package org.adaptinet.node.mimehandlers.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.adaptinet.node.exception.ServletException;
import org.adaptinet.node.logging.logger.Logger;
import org.adaptinet.node.logging.logger.LoggerFactory;
import org.adaptinet.node.mimehandlers.controllers.Controller;
import org.adaptinet.node.servlet.Cookie;
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
	
	private final Logger logger = LoggerFactory
	 .getLogger(ControllerServlet.class);

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
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {

		try {
			if (debug)
				logTransaction(req);

			// Initialize the path variables
			final String buffer = req.getContextPath() + req.getServletPath() + req.getPathInfo();
			final String s[] = buffer.split("/");
			final String controllerName = s[2];
			final String methodName = s[3];

			final Class<?> controllerClass = ServicesConfig.getController(controllerName);
			final Controller controller = (Controller) controllerClass.newInstance();

			Object args[] = null;
			final HashMap<String, String[]> params = new HashMap<String, String[]>(); // =
																					// req.getParameterMap();
			final int r = s.length - PARAM_START;
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

	protected Object executeMethod(Object targetObject, String name, Object args[]) throws Exception {
		Object ret = null;

		for (Method method : targetObject.getClass().getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name)) {
				boolean bExecute = true;
				Class<?> pvec[] = method.getParameterTypes();

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

	public void logTransaction(HttpServletRequest req) {

		final StringWriter sw = new StringWriter();
		final PrintWriter writer = new PrintWriter(sw);
		// writer.println("Request Received at "
		// + (new Timestamp(System.currentTimeMillis())));

		writer.println("--------- From XMLServlet --------");
		writer.println(" characterEncoding=" + req.getCharacterEncoding());
		writer.println("     contentLength=" + req.getContentLength());
		writer.println("       contentType=" + req.getContentType());
		writer.println("            locale=" + req.getLocale());
		writer.print("           locales=");
		writer.println(req.getLocales().stream().map(l -> l.toString()).collect(Collectors.joining(", ")));

		Set<String> names = req.getParameterNames();
		for(String name : names) {
			writer.print("         parameter " + name + "=");
			writer.println(Arrays.asList(req.getParameterValues(name)).stream().collect(Collectors.joining()));
		}
		writer.println("          protocol=" + req.getProtocol());
		writer.println("        remoteAddr=" + req.getRemoteAddr());
		writer.println("        remoteHost=" + req.getRemoteHost());
		writer.println("            Scheme=" + req.getScheme());
		writer.println("        serverName=" + req.getServerName());
		writer.println("        serverPort=" + req.getServerPort());
		writer.println("          isSecure=" + req.isSecure());

		if (req instanceof HttpServletRequest) {
			writer.println("---------------------------------------------");
			final HttpServletRequest hrequest = (HttpServletRequest) req;
			writer.println("       contextPath=" + hrequest.getContextPath());
			Cookie cookies[] = hrequest.getCookies();
			if (cookies == null)
				cookies = new Cookie[0];
			for (int i = 0; i < cookies.length; i++) {
				writer.println("            cookie=" + cookies[i].getName() + "=" + cookies[i].getValue());
			}

			names = hrequest.getHeaderNames();

			for(String name : names) {
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
		logger.debug(msg);
	}
}
