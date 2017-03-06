package org.adaptinet.node.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class KickStart {
	/** Main method */
	public static void main(String[] args) {
		try {
			String port = new String(args[0]);
			String processor = new String(args[1]);
			String method = new String(args[2]);

			String message = new String();
			message = "<?xml version=\"1.0\"?>";
			message += "<Envelope><Header><Message>";
			message += "<To>";
			message += "<Address><Prefix>";
			message += "http";
			message += "</Prefix><Host>";
			message += "localhost";
			message += "</Host><Port>";
			message += port;
			message += "</Port>";
			message += "<Processor>";
			message += processor;
			message += "</Processor>";
			message += "<Method>";
			message += method;
			message += "</Method>";
			message += "</Address>";
			message += "</To>";
			message += "<Key>";
			message += "0";
			message += "</Key>";
			message += "</Message>";
			message += "</Header><Body>";
			message += "</Body></Envelope>";

			// Post the request
			String urlString = "http://localhost:" + port + "/";
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", " " + "text/xml");
			connection.setRequestProperty("Connection", " close");
			OutputStream os = connection.getOutputStream();
			int len = message.length();
			byte[] data = message.getBytes();
			for (int i = 0; i < len; i++) {
				os.write(data[i]);
			}
			os.flush();

			// Read the response
			InputStream is = connection.getInputStream();
			StringBuffer stringbuffer = new StringBuffer();
			int b = -1;
			while (true) {
				b = is.read();
				if (b == -1)
					break;
				stringbuffer.append((char) b);
			}

			System.out.print(stringbuffer.toString());

			is.close();
			os.close();
		} catch (Exception e) {
			System.out.println("Usage: port processor method");
			e.printStackTrace();
		}
	}
}