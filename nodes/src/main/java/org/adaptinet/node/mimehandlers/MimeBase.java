package org.adaptinet.node.mimehandlers;

import java.io.ByteArrayOutputStream;

import org.adaptinet.node.http.Request;
import org.adaptinet.node.server.IServer;


public interface MimeBase {
	public abstract ByteArrayOutputStream process(IServer server,
			Request request);

	public abstract int getStatus();

	public abstract void init(String u, IServer s);

	public String getContentType();
}
