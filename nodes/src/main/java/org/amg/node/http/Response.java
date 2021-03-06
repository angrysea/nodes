package org.amg.node.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Response {

	private boolean bRespond = true;
	private String contentType;
	@SuppressWarnings("unused")
	private String host = "localhost";
	private int nStatus = 200;
	private OutputStream os = null;
	private ByteArrayOutputStream response;

	public Response() {
	}

	public Response(final OutputStream output, final String newHost, final String contentType) {
		os = output;
		host = newHost;
		this.contentType = contentType;
	}

	private String getDate() {
		final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM, yyyy hh:mm:ss zzz");
		return format.format(new Date());
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public void respond() throws IOException {
		if (!bRespond)
			return;

		final byte[] bytes = response.toByteArray();

		writeHeader(bytes.length);

		os.write(bytes);
		os.write(HTTP.crlf.getBytes());
		os.flush();
	}

	public void setHost(final String newValue) {
		host = newValue;
	}

	public void setRespond(final boolean b) {
		bRespond = b;
	}

	public void setResponse(final ByteArrayOutputStream newValue) {
		try {
			if (newValue != null) {
				response = newValue;
			} else {
				response = new ByteArrayOutputStream();
				response.write(new String("Error in Processing Processor").getBytes());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void setStatus(final int newValue) {
		nStatus = newValue;
	}

	public void writeHeader(final long contentLength) {
		try {
			os.write(HTTP.byteArrayVersion);
			os.write(Integer.toString(nStatus).getBytes());

			String strCode = "OK";
			if (nStatus < 200)
				strCode = HTTP.msg100[nStatus - 100];
			else if (nStatus < 300)
				strCode = HTTP.msg200[nStatus - 200];
			else if (nStatus < 400)
				strCode = HTTP.msg300[nStatus - 300];
			else if (nStatus < 500)
				strCode = HTTP.msg400[nStatus - 400];
			else if (nStatus < 600)
				strCode = HTTP.msg500[nStatus - 500];

			os.write(strCode.getBytes());
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.server.getBytes());
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.date.getBytes());
			os.write(this.getDate().getBytes());
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.nocache.getBytes());
			os.write(HTTP.crlf.getBytes());

			if (nStatus == HTTP.UNAUTHORIZED) {
				os.write("WWW-Authenticate: Basic realm=\"Server\"".getBytes());
				os.write(HTTP.crlf.getBytes());
				os.write(HTTP.crlf.getBytes());
				os.flush();
				return;
			}

			if (contentType == null) {
				os.write(HTTP.contentTypeXML.getBytes());
			} else {
				os.write(contentType.getBytes());
			}
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.acceptRange.getBytes());
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.lastModified.getBytes());
			os.write(this.getDate().getBytes());
			os.write(HTTP.crlf.getBytes());

			if (contentLength != 0) {
				os.write(HTTP.contentLength.getBytes());
				os.write(Long.toString(contentLength).getBytes());
				os.write(HTTP.crlf.getBytes());
			}
			os.write(HTTP.crlf.getBytes());

			if (nStatus >= 400)
				os.write(("<html><head><title>" + Integer.toString(nStatus) + " " + strCode + "</title></head><h1>"
						+ strCode + "</h1></html>").getBytes());

			os.flush();
		} catch (Throwable t) {
			System.out.println("Error in response");
			t.printStackTrace();
		}
	}

}
