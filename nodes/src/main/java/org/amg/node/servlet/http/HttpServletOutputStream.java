package org.amg.node.servlet.http;

import java.io.DataOutputStream;
import java.io.IOException;

import org.amg.node.http.Response;
import org.amg.node.servlet.ServletOutputStream;

public class HttpServletOutputStream extends ServletOutputStream {
	DataOutputStream out = null;
	HttpServletResponse resp = null;
	Response reply = null;

	byte buffer[] = null;
	int count = 0;
	boolean committed = false;
	boolean writerUsed = false;

	byte ln[] = { (byte) '\r', (byte) '\n' };

	private void flushBuffer() throws IOException {
		if (out == null) {
			if (reply != null) {
				out = new DataOutputStream(reply.getOutputStream());
				resp.writeHeaders(out);
			}
		}
		if (count > 0) {
			out.write(buffer, 0, count);
			count = 0;
		}
		committed = true;
	}

	public void print(int i) throws IOException {
		write(i);
	}

	public void print(double i) throws IOException {
		print(Double.toString(i));
	}

	public void print(long l) throws IOException {
		print(Long.toString(l));
	}

	public void print(String s) throws IOException {
		write(s.getBytes());
	}

	public void println() throws IOException {
		write(ln);
	}

	public void println(int i) throws IOException {
		print(i);
		println();
	}

	public void println(double i) throws IOException {
		print(i);
		println();
	}

	public void println(long l) throws IOException {
		print(l);
		println();
	}

	public void println(String s) throws IOException {
		print(s);
		println();
	}

	public void write(int b) throws IOException {
		write((byte) b);
	}

	protected void write(byte b) throws IOException {
		if (count >= buffer.length) {
			flushBuffer();
		}
		buffer[count++] = b;
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (len >= buffer.length) {
			flushBuffer();
			out.write(b, off, len);
			return;
		}
		if (len > buffer.length - count) {
			flushBuffer();
		}
		System.arraycopy(b, off, buffer, count, len);
		count += len;
	}

	public void flush() throws IOException {
		if (!writerUsed) {
			flushBuffer();
			out.flush();
		}
	}

	public void realFlush() throws IOException {
		flushBuffer();
		out.flush();
	}

	public void close() throws IOException {
		flushBuffer();
		out.close();
	}

	public void reset() throws IllegalStateException {
		if (committed) {
			throw new IllegalStateException("Response already committed");
		}
		// empty the buffer
		count = 0;
	}

	public boolean isCommitted() {
		return committed;
	}

	HttpServletOutputStream(HttpServletResponse resp, DataOutputStream out, int bufsize, boolean writerUsed) {
		this.out = out;
		this.resp = resp;
		this.writerUsed = writerUsed;
		if (bufsize <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		this.buffer = new byte[bufsize];
		this.count = 0;
		this.committed = false;
	}

	HttpServletOutputStream(HttpServletResponse resp, Response reply, int bufsize, boolean writerUsed) {
		this.resp = resp;
		this.reply = reply;
		this.writerUsed = writerUsed;
		if (bufsize <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		this.buffer = new byte[bufsize];
		this.count = 0;
		this.committed = false;
	}
}
