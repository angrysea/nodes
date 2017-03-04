package org.adaptinet.node.mimehandlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.adaptinet.nnode.http.Request;
import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.messaging.ResponseWriter;
import org.adaptinet.node.processoragent.ProcessorAgent;
import org.adaptinet.node.processoragent.ProcessorFactory;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.server.NetworkAgent;

public class Mime_Binary implements MimeBase {

	static private int HANDLER_POS=1;
	private ProcessorAgent processor = null;
	protected boolean bRollBackOnly = false;
	protected int contentLength = 0;
	protected final static short WAIT = 0;
	protected final static short CHECK = 1;
	protected final static short ROLLBACK = 2;
	protected final static short COMMIT = 3;
	protected final static short RETURN = 4;
	protected final static short COMPLETE = 5;
	protected boolean bVerbose = false;
	protected String url;
	
	static NetworkAgent networkAgent = null;

	static {
		networkAgent = (NetworkAgent) IServer.getServer().getService(
				"networkAgent");
	}
	

	public Mime_Binary() {
		bVerbose = IServer.getServer().getVerboseFlag();
	}
	
	public int getStatus() {
		return 200;
	}

	public String getContentType() {
		return null;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void init(String url, IServer server) {
		this.url=url;
		
		synchronized(networkAgent) {
			if(networkAgent==null) {
				networkAgent = (NetworkAgent) server.getService("networkAgent");			
			}
		}
	}

	public Object getObject() {
		return null;
	}

	public ByteArrayOutputStream process(IServer server, Request request) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			if (bVerbose == true)
				System.out.println("=============== Incoming Binary Transaction ===============\n"
								+ request.getRequestURI());

			String s[] = url.split("/");

			if (s != null && s.length > HANDLER_POS) {
				String name = s[HANDLER_POS];
				String methodName = s[HANDLER_POS + 1];

				if (name != null && name.length()>0) {
					processor = (ProcessorAgent)server.getAvailableProcessor(name);
					if(processor!=null) {
						processor.preProcess(ProcessorFactory.SERVICECLASS);
						processor.startProcessor();

						AdaptinetException exMessage = new AdaptinetException(
								AdaptinetException.SEVERITY_SUCCESS,
								AdaptinetException.GEN_MESSAGE);
						exMessage.logMessage("Processor received Name: "
								+ processor.getName());

						Object ret = processor.execute(methodName, request.getData());
						new ResponseWriter(out, false).writeResponse((byte [])ret);
					}
				}
			} else {
				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_ERROR,
						AdaptinetException.GEN_BASE);
				exMessage.logMessage("Unable to load find available handler: ");
			}

			AdaptinetException exMessage = null;


			exMessage = new AdaptinetException(
					AdaptinetException.SEVERITY_SUCCESS,
					AdaptinetException.GEN_MESSAGE);
			exMessage.logMessage("Processor successfully executed Name: "
					/*+ processor.getName()*/);
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
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