package org.amg.node.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;

public class AsyncResponse {

	public AsyncResponse() {
	}

	public void Respond(final String xmlBuffer) {
		String responseData = null;
		try {
			final String postData = URLEncoder.encode(xmlBuffer.toString(), "UTF-8");
			if (postData != null) {
				final int port = 80;
				final Socket s = new Socket("localhost", port);
				final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				final DataInputStream dis = new DataInputStream(s.getInputStream());

				dos.writeBytes("POST " + "HTTP/1.1\r\n" + "Content-length: " + postData.length() + "\r\n\r\n");
				dos.writeBytes(postData);
				dos.close();

				final StringBuffer readBuffer = new StringBuffer();
				final byte[] bytes = new byte[128];

				while (dis.read(bytes) != -1)
					readBuffer.append(bytes);

				responseData = readBuffer.toString();
				s.close();
			}
		} catch (final IOException ioe) {
			responseData = ioe.getMessage();
			System.err.println(responseData);
		} catch (final SecurityException se) {
			responseData = se.getMessage();
			System.err.println(responseData);
		}
	}
}
