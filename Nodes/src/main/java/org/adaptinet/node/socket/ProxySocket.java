package org.adaptinet.node.socket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.adaptinet.nnode.http.Parser;
import org.adaptinet.nnode.http.Request;
import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.messaging.PostWriter;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.serverutils.CachedThread;
import org.adaptinet.node.xmltools.parser.InputSource;

public class ProxySocket extends BaseSocket {

	private Message msg = null;

	private PostWriter writer = new PostWriter();

	private Object args[] = new Object[1];

	public ProxySocket(String type, IServer server,
			BaseSocketServer baseSocketServer, CachedThread thread) {

		super(type, server, baseSocketServer, thread);
		address = "http://" + server.getProxyAddress();
		msg = new Message(address);
		msg.getAddress().setMethod("getRequest");
		msg.getAddress().setProcessor("ProxyServer");
		msg.getAddress().setSync();

		args[0] = new String(server.getHost());
	}

	synchronized private boolean startConnection(InputStream in) {

		try {
			Parser parser = new Parser(new InputSource(in), type);
			Request request = parser.parse();
			if (request.getRequestType() == Request.NONE) {
				return interrupted;
			}

			request.setSocket(null);
			request.processRequest(server, null);
			input.close();
		} catch (AdaptinetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed");
		}
		return interrupted;
	}

	public void run() {
		try {
			startConnection(new BufferedInputStream(input));
		} catch (Exception e) {
		} finally {
			baseSocketServer.socketFinished(this);
		}
	}

	protected synchronized void terminate() {

		if (running == true) {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				System.out.println(ex);
			}

			input = null;
			interrupted = false;
			running = false;
		}
	}

	protected synchronized void interruptConnection(boolean now) {

		if (running == true) {
			interrupted = true;
			if (now == true) {
				terminate();
			}
		}
	}

	public int sendContinue() throws IOException {

		if (cont == true) {
			return -1;
		}

		cont = true;
		return 0;
	}

	public OutputStream getOutputStream() {
		return null;
	}

	public void requestProxy() throws Exception {

		try {
			Message requestMsg = new Message(msg);
			requestMsg.setMethod("requestProxy");

			URL url = new URL(address);
			URLConnection connection = url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", " " + "text/xml");
			connection.setRequestProperty("Connection", " close");
			OutputStream os = connection.getOutputStream();

			writer.setStream(os);
			writer.write(requestMsg, args);

			connection.connect();
			os.flush();
			os.close();

			input = connection.getInputStream();

			InputStreamReader reader = new InputStreamReader(
					new BufferedInputStream(connection.getInputStream()));
			StringBuffer stringbuffer = new StringBuffer();

			int b = -1;
			while ((b = reader.read()) != -1) {
				stringbuffer.append((char) b);
			}

			String result = stringbuffer.toString();
			reader.close();

			if (result.length() < 1)
				throw new Exception("Unknown return no results");
		} catch (Exception e) {
			throw new ProcessorException(AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
		}
	}

	public void bind() throws Exception {

		try {
			URL url = new URL(address);
			URLConnection connection = url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", " " + "text/xml");
			connection.setRequestProperty("Connection", " close");
			OutputStream os = connection.getOutputStream();

			writer.setStream(os);
			writer.write(msg, args);

			connection.connect();
			os.flush();
			os.close();

			input = connection.getInputStream();
			try {
				while ((input.available()) < 1) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
					}
				}
				baseSocketServer.run(this);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} catch (Exception e) {
			throw new ProcessorException(AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
		}
	}
}
