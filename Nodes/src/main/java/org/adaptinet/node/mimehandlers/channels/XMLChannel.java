package org.adaptinet.node.mimehandlers.channels;

import java.io.InputStream;
import java.util.logging.Logger;

import org.adaptinet.node.mimehandlers.controllers.Controller;
import org.adaptinet.node.mimehandlers.servlets.ServicesConfig;
import org.adaptinet.node.servlet.http.HttpServletResponse;
import org.adaptinet.node.xmltools.xmlutils.IXMLInputSerializer;
import org.adaptinet.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.adaptinet.node.xmltools.xmlutils.XMLSerializerFactory;


public class XMLChannel extends BaseChannel {

	//private final Logger logger = Logger.getLogger();

	public void doGet(String controllerName, String methodName, Object[] args) {

		try {
			Controller controller = getController(controllerName);
			IXMLOutputSerializer outserial = XMLSerializerFactory
					.getOutputSerializer();

			Object returnObj = executeMethod(controller, methodName, args);
			String retVal = null;
			if (returnObj != null) {
				retVal = outserial.get(returnObj);
			}

			if (outserial.hasError()) {
				if (outserial.getLastError() != null) {
//					logger.error(
//							"XMLSVC: Error serializing response to XML, ex="
//									+ outserial.getLastError().getMessage(),
//							outserial.getLastError());
				} else {
//					logger.error("XMLSVC: Unknown error serializing response to XML");
				}
//				setError(resp, "Error serializing to XML response.");
			}
//			if(DUMP)
//				logger.debug(new StringBuffer("XMLSVC: REQXML=[").append(args)
//						.append("], RESPXML=[")
//						.append(outserial.hasError() ? "ERROR" : retVal)
//						.append("]").toString());

			if (retVal != null && retVal.length() > 0) {
//				writeDebugFile(controllerName + methodName, logger, retVal,
//						false);
				writeOutput(retVal);
			}
		} catch (Exception e) {
			String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><servlet>"
					+ this.getClass().getName()
					+ "</servlet><code>1</code><desc>"
					+ e.getMessage()
					+ "</desc><timestamp>"
					+ (new java.util.Date(System.currentTimeMillis())
							.toString()) + "</timestamp></status>";
			try {
				(resp).addHeader("Adaptinet-Error-Message: ", result);
				(resp).sendError(601);
			} catch (Exception ioe) {
//				logger.debug("Adaptinet-Error-Message: " + result);
			}
		}
	}

	public void doPost(String controllerName, String methodName) {

		try {
			Controller controller = getController(controllerName);
			int bytesRead = 0;
			int cl = req.getContentLength();

			String xml = null;
			if (cl > 0) {
				byte[] b = new byte[cl];
				InputStream is = req.getInputStream();
				while (bytesRead < cl) {
					int br = is.read(b, bytesRead, cl - bytesRead);
					if (br == -1) {
						break;
					}
					bytesRead += br;
				}

				xml = new String(b);
				b = null;
//				writeDebugFile(controllerName + methodName, logger, xml, true);

			}
			IXMLInputSerializer inserial = XMLSerializerFactory
					.getInputSerializer();
			inserial.setPackage(ServicesConfig.getPackage(controllerName));
//			if(DUMP)
//				logger.debug("XMLSVC: REQXML=" + xml);

			Object args[] = new Object[1];

			if (xml != null && xml.length() > 0) {
				args[0] = inserial.get(xml);
			} else {
				args[0] = null;
			}
			if (inserial.hasError()) {
				throw inserial.getLastError();
			}

			Object returnObj = null;
			returnObj = executeMethod(controller, methodName, args);
			String retVal = null;
			if (returnObj != null) {
				IXMLOutputSerializer outserial = XMLSerializerFactory
						.getOutputSerializer();
				retVal = outserial.get(returnObj);
				if (outserial.hasError()) {
					if (outserial.getLastError() != null) {
//						logger.error(
//								"XMLSVC: Error serializing response to XML, ex="
//										+ outserial.getLastError().getMessage(),
//								outserial.getLastError());
					} else {
//						logger.error("XMLSVC: Unknown error serializing response to XML");
					}
					setError(resp, "Error serializing to XML response.");
				}
//				if(DUMP)
//					logger.debug(new StringBuffer("XMLSVC: REQXML=[").append(args)
//							.append("], RESPXML=[")
//							.append(outserial.hasError() ? "ERROR" : retVal)
//							.append("]").toString());
				
			}

			if (retVal != null && retVal.length() > 0) {
//				writeDebugFile(controllerName + methodName, logger, retVal,
//						false);
				writeOutput(retVal);
			}
		} catch (Exception e) {
			String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><servlet>"
					+ this.getClass().getName()
					+ "</servlet><code>1</code><desc>"
					+ e.getMessage()
					+ "</desc><timestamp>"
					+ (new java.util.Date(System.currentTimeMillis())
							.toString()) + "</timestamp></status>";
			try {
				(resp).addHeader("Adaptinet-Error-Message: ", result);
				(resp).sendError(601);
			} catch (Exception ioe) {
//				logger.debug("Adaptinet-Error-Message: " + result);
			}
		}
	}

	@Override
	public String getMimeType() {
		return "text/xml";
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
			resp.addHeader("Adaptinet-Error-Message: ", result);
			resp.sendError(601);
		} catch (Exception ioe) {
//			logger.error(
//					"XMLSVC: Sending HTTP 601 Adaptinet-Error-Message response : "
//							+ result + ", ex=" + ioe.getMessage(), ioe);
		}
	}
}
