package org.amg.node.servlet.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpServletInputStream extends java.io.InputStream{
	private InputStream in = null;

	HttpServletInputStream(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte b[]) throws IOException {
		return in.read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	public int available() throws IOException {
		return in.available();
	}

	public void close() throws IOException {
		in.close();
	}

	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	public synchronized void reset() throws IOException {
		in.reset();
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public int readLine(byte b[], int off, int len) throws IOException {
		int got = 0;
		while (got < len) {
			int ch = in.read();
			switch (ch) {
			case -1:
				return -1;
			// case '\r':
			case '\n':
				b[off + got] = (byte) (ch & 0xff);
				got++;
				// in.mark(1);
				// if ((ch = in.read()) != '\n' )
				// in.reset();
				return got;
			default:
				b[off + got] = (byte) (ch & 0xff);
				got++;
			}
		}
		return got;
	}
}
