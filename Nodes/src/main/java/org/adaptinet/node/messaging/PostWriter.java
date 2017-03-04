package org.adaptinet.node.messaging;

import java.io.OutputStream;

import org.adaptinet.node.server.IServer;


public class PostWriter extends BaseWriter {

	public PostWriter() {
	}

	public PostWriter(OutputStream ostream) {
		super(ostream);
	}

	public void write(Message msg, Object[] args) throws Exception {
		// System.out.println();
		// System.out.println("*************Outgoing XML*********************");

		// Declared up here hopefully put into register.
		String temp = null;
		try {
			Address addr = msg.getAddress();
			writeString("<Envelope><Header><Message>");
			if (addr != null) {
				writeString("<To>");
				writeAddress(addr);
				writeString("</To>");
			}

			temp = msg.getID();
			if (temp != null) {
				writeString("<id>");
				writeString(temp);
				writeString("</id>");
			}

			// Check to see if a certificate is available
			if (IServer.getCertificate() != null) {
				writeString("<Certificate>");
				writeString(IServer.getCertificate());
				writeString("</Certificate>");
			}

			// Check to see if a certificate is available
			if (IServer.getCertificate() != null) {
				writeString("<Certificate>");
				writeString(IServer.getCertificate());
				writeString("</Certificate>");
			}

			writeString("<Key>");
			writeString(IServer.getKey());
			writeString("</Key>");
			writeString("<Timestamp>");
			writeString(msg.getTimeStamp());
			writeString("</Timestamp>");
			temp = msg.getHops();
			if (temp != null) {
				writeString("<Hops>");
				writeString(temp);
				writeString("</Hops>");
			}
			Address replyTo = msg.getReplyTo();
			if (replyTo != null) {
				writeString("<ReplyTo>");
				writeAddress(replyTo);
				writeString("</ReplyTo>");
			}
			writeString("</Message>");
			writeString("</Header><Body>");
			if (args != null) {
				for (int i = 0; i < args.length; i++)
					convertToXML(args[i]);
			}
			writeString("</Body></Envelope>");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
		//System.out.println(ostream.toString());
	}

	public void writeAddress(Address address) throws Exception {
		String temp = null;
		writeString("<Address><Prefix>");
		writeString(address.getPrefix());
		writeString("</Prefix><Host>");
		writeString(address.getHost());
		writeString("</Host><Port>");
		writeString(address.getPort());
		writeString("</Port>");
		temp = address.getPostfix();
		if (temp != null) {
			writeString("<Postfix>");
			writeString(temp);
			writeString("</Postfix>");
		}
		temp = address.getProcessor();
		if (temp != null) {
			writeString("<Processor>");
			writeString(temp);
			writeString("</Processor>");
		}
		temp = address.getMethod();
		if (temp != null) {
			writeString("<Method>");
			writeString(temp);
			writeString("</Method>");
		}
		temp = address.getType();
		if (temp != null) {
			writeString("<Type>");
			writeString(temp);
			writeString("</Type>");
		}
		temp = address.getEmail();
		if (temp != null) {
			writeString("<Email>");
			writeString(temp);
			writeString("</Email>");
		}
		Address route = address.getRoute();
		if (route != null) {
			writeString("<Route>");
			writeAddress(route);
			writeString("</Route>");
		}
		writeString("</Address>");
	}
}