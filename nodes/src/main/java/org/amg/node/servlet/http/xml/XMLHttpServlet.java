package org.amg.node.servlet.http.xml;

import java.io.IOException;

import org.amg.node.exception.BaseException;
import org.amg.node.exception.ServletException;
import org.amg.node.servlet.GenericServlet;
import org.amg.node.servlet.ServletRequest;
import org.amg.node.servlet.ServletResponse;
import org.amg.node.servlet.http.HttpServletConfig;
import org.amg.node.servlet.http.HttpServletRequest;
import org.amg.node.servlet.http.HttpServletResponse;
import org.amg.node.xmltools.xmlutils.IXMLInputSerializer;
import org.amg.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.amg.node.xmltools.xmlutils.XMLSerializerFactory;

public class XMLHttpServlet extends GenericServlet {
	private static final long serialVersionUID = 5706796160037027080L;
	private String xml = null;

	/**
	 * This method should be overridden to specify the package name for the
	 * repository... are acceptable for this XMLHttpServlet
	 *
	 * The default implementation returns <i>null</i>
	 */
	protected String getPackageName() {
		return null;
	}

	/**
	 * This method should be overridden to specify that only certain
	 * <b>Content-Type</b>'s are acceptable for this XMLHttpServlet
	 *
	 * The default implementation is to support all types
	 */
	protected boolean handleContentType(String contentType) {
		return true;
	}

	/**
	 * Must be overridden to implement the functionality of the Transaction
	 *
	 * The Object <b>in</b> will be the populated serialized form of XML to Java
	 * the method is responsible for returning a valid populated Object for
	 * serialization back to XML
	 */
	protected Object doTransaction(Object in) throws ServletException {
		throw new ServletException(BaseException.SEVERITY_FATAL, ServletException.TCV_INVALIDNOTIMPLEMENT);
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();
		if (method.equals("POST") && req.getContentType() != null && handleContentType(req.getContentType()))
			doTransaction(req, resp);
	}

	private void doTransaction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			IXMLInputSerializer xmlIn = XMLSerializerFactory.getInputSerializer();
			IXMLOutputSerializer xmlOut = XMLSerializerFactory.getOutputSerializer();
			if (xmlIn == null || xmlOut == null)
				throw new Exception("Cannot create Serializers");

			String packageName = getPackageName();
			if (packageName != null)
				xmlIn.setPackage(getPackageName());

			req.getInputStream();
			int cl = req.getContentLength();
			byte[] b = new byte[cl];
			req.getInputStream().read(b);
			this.xml = new String(b);

			Object objIn = xmlIn.get(xml, this.getClass().getClassLoader());
			if (xmlIn.hasError())
				throw xmlIn.getLastError();

			if (objIn == null)
				throw new Exception("The input serialization returned null...");

			Object objOut = doTransaction(objIn);
			if (objOut == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				String sxmlOut = xmlOut.get(objOut);
				if (sxmlOut == null) // this really cannot happen much unless
										// the xerces isn't in the path
					throw new Exception("The output serialization returned null...");

				if (xmlOut.hasError())
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Exception occured: " + xmlOut.getLastError() + " returned XML was: " + sxmlOut);
				else
					resp.getOutputStream().print(sxmlOut);
			}
		} catch (Exception e) {
			ServletException se = new ServletException(BaseException.SEVERITY_FATAL, ServletException.TCV_URLFAILDED);
			se.logMessage("Exception during doTransaction(req,resp): " + e.getMessage());
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Exception during XMLHttpServlet::doTransaction(req,resp) = [" + e + "]");
			throw se;
		}
	}

	final protected String getIncomingXML() {
		return this.xml;
	}

	public void init(HttpServletConfig sc) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
}