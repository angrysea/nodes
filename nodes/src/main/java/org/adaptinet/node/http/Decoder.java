package org.adaptinet.node.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Decoder {
	static public String decode(byte[] input) throws IOException {
		final ByteArrayInputStream in = new ByteArrayInputStream(input);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c = -1;
		while ((c = in.read()) > 0) {
			if (c == '+') {
				out.write(' ');
			} else if (c == '%') {
				final int c1 = Character.digit((char) in.read(), 16);
				final int c2 = Character.digit((char) in.read(), 16);
				out.write((char) (c1 * 16 + c2));
			} else {
				out.write(c);
			}
		}
		out.flush();
		return out.toString();
	}
}
