package org.amg.node.mimehandlers;

import java.io.ByteArrayOutputStream;

import org.amg.node.http.Request;
import org.amg.node.server.IServer;
import org.amg.node.server.NetworkAgent;

public abstract class MimeData implements MimeBase {

	static NetworkAgent networkAgent = null;

	protected boolean bRollBackOnly = false;
	protected int contentLength = 0;
	protected final static short WAIT = 0;
	protected final static short CHECK = 1;
	protected final static short ROLLBACK = 2;
	protected final static short COMMIT = 3;
	protected final static short RETURN = 4;
	protected final static short COMPLETE = 5;
	protected boolean bVerbose = false;

	public MimeData() {
		bVerbose = IServer.getServer().getVerboseFlag();
	}

	public abstract boolean process(String trxdata);

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
		process(request.getRequest());
		return null;
	}

	public void init(String u, IServer server) {
	}

	public Object getObject() {
		return null;
	}

	static {
		networkAgent = (NetworkAgent) IServer.getServer().getService(
				"networkAgent");
	}

}
