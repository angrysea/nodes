package org.amg.node.mimehandlers;

import java.io.ByteArrayOutputStream;

import org.amg.node.http.Request;
import org.amg.node.server.IServer;


public class MimeImage implements MimeBase {
	@SuppressWarnings("unused")
	private String url = null;

	private String contentType = null;

	public MimeImage(String url) {
		this.url = url;
	}

	public void init(String u, IServer s) {
	}

	public String getContentType() {
		return contentType;
	}

	public ByteArrayOutputStream process(IServer server,
			Request request) {
		// TODO: Implement this org.amg.sdk.http.MimeBase method
		return null;
	}

	public int getStatus() {
		return 200;
	}
}
