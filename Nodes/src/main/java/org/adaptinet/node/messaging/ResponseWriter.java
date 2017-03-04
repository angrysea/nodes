package org.adaptinet.node.messaging;

import java.io.OutputStream;

public class ResponseWriter extends BaseWriter {
	public ResponseWriter(OutputStream ostream) {
		super(ostream);
	}

	public ResponseWriter(OutputStream ostream, boolean bWriteHeader) {
		super(ostream, bWriteHeader);
	}

	public void writeResponse(Object ret) throws Exception {
		// Declared up here hopefully put into register.
		try {
			writeString("<Envelope><Body>");
			convertToXML(ret);
			writeString("</Body></Envelope>");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
	}
	
	public void writeResponse(String ret) throws Exception {
		// Declared up here hopefully put into register.
		try {
			writeString(ret);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
	}

	public void writeResponse(byte [] ret) throws Exception {
		// Declared up here hopefully put into register.
		try {
			write(ret);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
	}
}