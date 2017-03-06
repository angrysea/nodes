package org.adaptinet.node.mimehandlers.channels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.adaptinet.node.logging.logger.Logger;
import org.adaptinet.node.mimehandlers.controllers.Controller;
import org.adaptinet.node.mimehandlers.servlets.ServicesConfig;
import org.adaptinet.node.servlet.ServletContext;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.servlet.http.HttpServletRequest;
import org.adaptinet.node.servlet.http.HttpServletResponse;

public abstract class BaseChannel implements Channel {

	public static final String DUMPTRANS = "Dump.Transactions";
	public static final String DUMPDIR = "Dump.Directory";
	public static final String OPTIMIZECHARARRAYS = "Optimize.Char.Arrays";
	private HttpServletConfig sc = null;
	protected HttpServletRequest req = null;
	protected HttpServletResponse resp = null;
	protected ServletContext context;
	protected String dumpDir = System.getProperty("java.io.tmpdir");
	protected boolean DUMP = false;
	protected boolean optimizeCharArrays = false;
	@SuppressWarnings("unused")
	private static final String GENERATE_XML = "generate.xml";

	@Override
	public HttpServletConfig getSc() {
		return sc;
	}

	@Override
	public void init(HttpServletConfig sc, ServletContext context) {
		this.sc = sc;
		this.context = context;
		String dump = sc.getInitParameter(DUMPTRANS);
		if (dump != null && dump.equalsIgnoreCase("TRUE")) {
			DUMP = true;
		}
		dump = sc.getInitParameter(DUMPDIR);
		if (dump != null && !dump.isEmpty()) {
			dumpDir = dump;
		}
		String optimize = sc.getInitParameter(OPTIMIZECHARARRAYS);
		if (optimize != null && optimize.equalsIgnoreCase("TRUE")) {
			optimizeCharArrays = true;
		}
	}

	@Override
	public void setReq(HttpServletRequest req) {
		this.req = req;
	}

	@Override
	public void setResp(HttpServletResponse resp) {
		this.resp = resp;
	}

	@Override
	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	public void writeOutput(String retVal) throws IOException {

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (this.isAcceptGzip(req)) {
			resp.setHeader("Content-Encoding", "gzip");
			GZIPOutputStream gzos = new GZIPOutputStream(bos);
			gzos.write(retVal.getBytes());
			gzos.flush(); // Not only it is necessary to flush,
			gzos.close(); // but we also have to close it.
		} else {
			bos.write(retVal.getBytes());
			bos.flush();
			bos.close();
		}

		resp.setContentLength(bos.size());
		resp.setContentType(getMimeType());
		resp.getOutputStream().write(bos.toByteArray());
		resp.getOutputStream().flush();
	}

	private boolean isAcceptGzip(HttpServletRequest request) {
		final String header = request.getHeader("Accept-Encoding");
		return (header != null && header.indexOf("gzip") > -1);
	}

	@Override
	public String getMimeType() {
		return "text/html";
	}

	public Controller getController(String serviceName) throws InstantiationException, IllegalAccessException {
		final Class<?> controllerClass = ServicesConfig.getController(serviceName);
		final Controller controller = (Controller) controllerClass.newInstance();
		controller.setReq(req);
		controller.setResp(resp);
		controller.setServletContext(getServletContext());
		return controller;
	}

	protected Object executeMethod(Object targetObject, String name, Object[] args) throws Exception {
		Object ret = null;
		for (Method method : targetObject.getClass().getDeclaredMethods()) {
			if (method.getName().equalsIgnoreCase(name)) {
				boolean bExecute = true;
				final Class<?> pvec[] = method.getParameterTypes();

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

	public void writeDebugFile(String fileName, Logger logger, String data, boolean bInputFile) {

		java.io.FileOutputStream os = null;
		java.io.File file = null;
		if (DUMP) {
			String filename = fileName + Long.toString(new Date().getTime());
			String suffix = bInputFile ? ".in.xml" : ".out.xml";

			try {
				file = java.io.File.createTempFile(filename, suffix, null);
				os = new java.io.FileOutputStream(file);
				os.write(data.getBytes());
				if (bInputFile)
					logger.info("Request: Dumped to file : " + file.getAbsolutePath());
				else
					logger.info("Response: Dumped to file : " + file.getAbsolutePath());

			} catch (Exception e) {
				logger.info("CANVAS SVC: Error writing to debug file: " + filename + e.getMessage());
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						logger.info("Error closing file  : " + file.getAbsolutePath());
					}
				}
			}
		}
	}
}
