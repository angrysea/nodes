package org.amg.node.mimehandlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.amg.node.exception.AMGException;
import org.amg.node.messaging.ResponseWriter;
import org.amg.node.processoragent.ProcessorAgent;
import org.amg.node.processoragent.ProcessorFactory;
import org.amg.node.server.IServer;
import org.amg.node.xmltools.xmlutils.IXMLInputSerializer;
import org.amg.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.amg.node.xmltools.xmlutils.XMLSerializerFactory;

public class MimeXML_WS extends Mime {

	static private int HANDLER_POS=1;
	private ProcessorAgent processor = null;

	public MimeXML_WS() {
	}

	public ByteArrayOutputStream process(IServer server, String xml) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			if (bVerbose == true)
				System.out.println("=============== Incoming XML Transaction ===============\n"
								+ xml);

			String s[] = url.split("/");

			if (s != null && s.length > HANDLER_POS) {
				String name = s[HANDLER_POS];
				String methodName = s[HANDLER_POS + 1];

				if (name != null && name.length()>0) {
					processor = (ProcessorAgent)server.getAvailableProcessor(name);
					if(processor!=null) {
						processor.preProcess(ProcessorFactory.SERVICECLASS);
						processor.startProcessor();

						AMGException exMessage = new AMGException(
								AMGException.SEVERITY_SUCCESS,
								AMGException.GEN_MESSAGE);
						exMessage.logMessage("Processor received Name: "
								+ processor.getName());

						IXMLInputSerializer inserial = XMLSerializerFactory
								.getInputSerializer();
						inserial.setPackage(processor.getPackageName());
						Object obj = inserial.get(xml);
						if (obj != null) {
							Object ret = processor.execute(methodName, obj);
					        IXMLOutputSerializer outSerializer = XMLSerializerFactory.getOutputSerializer();        
					        String resposneMsg=outSerializer.get(ret, true);
							new ResponseWriter(out).writeResponse(resposneMsg);
						}
					}
				}
			} else {
				AMGException exMessage = new AMGException(
						AMGException.SEVERITY_ERROR,
						AMGException.GEN_BASE);
				exMessage.logMessage("Unable to load find available handler: ");
			}

			AMGException exMessage = null;

			exMessage = new AMGException(
					AMGException.SEVERITY_SUCCESS,
					AMGException.GEN_MESSAGE);
			exMessage.logMessage("Processor successfully executed Name: "
					/*+ processor.getName()*/);
		} catch (Exception e) {
			AMGException exMessage = new AMGException(
					AMGException.GEN_MESSAGE,
					AMGException.SEVERITY_SUCCESS);
			exMessage.logMessage("Execution failed reason: " + e.getMessage());
			try {
				out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><processor>unknown</processor><code>1</code><desc>"
								+ e.getMessage()
								+ "</desc><timestamp>"
								+ (new java.util.Date(System
										.currentTimeMillis()).toString()) + "</timestamp></status>")
								.getBytes());
			} catch (IOException ioe) {
			}
		} finally {
		}

		return out;
	}
	
	protected Object executeMethod(Object targetObject, String name,
			Object[] args) throws Exception {
		Object ret = null;
		for (Method method : targetObject.getClass().getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name)) {
				boolean bExecute = true;
				@SuppressWarnings("rawtypes")
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
					try {
						ret = method.invoke(targetObject, args);
					} catch (Exception e) {
						throw new Exception("Error executing method.");
					}
				} else {
					throw new Exception("No Such Method Found.");
				}
			}
		}
		return ret;
	}
}