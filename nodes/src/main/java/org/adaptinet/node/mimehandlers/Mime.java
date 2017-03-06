package org.adaptinet.node.mimehandlers;

import java.io.ByteArrayOutputStream;

import org.adaptinet.node.http.Request;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.server.NetworkAgent;


public abstract class Mime implements MimeBase {

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

	Mime() {
		bVerbose = IServer.getServer().getVerboseFlag();
	}

	public abstract ByteArrayOutputStream process(IServer server,
			String xml);

	public int getStatus() {
		return 200;
	}

	public String getContentType() {
		return null;
	}

	public int getContentLength() {
		return contentLength;
	}

	public ByteArrayOutputStream process(IServer server,
			Request request) {
		String xml = request.getRequest();
		return process(server, xml);
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

	static {
		networkAgent = (NetworkAgent) IServer.getServer().getService(
				"networkAgent");
	}
}
