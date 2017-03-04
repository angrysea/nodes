package org.adaptinet.node.messaging;

import org.adaptinet.node.server.IServer;

public final class Header {

	Message message = null;

	public Header() {
	}

	public Header(IServer server) {
		this(server, false);
	}

	public Header(IServer server, boolean bSecure) {
		message = new Message(server, false);
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
