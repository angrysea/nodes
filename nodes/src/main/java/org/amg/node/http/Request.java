package org.amg.node.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import org.amg.node.mimehandlers.MimeBase;
import org.amg.node.server.IServer;

public class Request {

	static final public short COMMAND = 4;
	static final public short DATA_TEXT = 7;
	static final public String GET = "GET";
	static final public short HTML_IMAGE = 3;
	static final public short HTML_TEXT = 2;
	static final public short MIME_BINARY = 6;
	static final public short NONE = 0;
	static final public String POST = "POST";
	static final public short XML_TEXT = 1;
	static final public short XML_WS = 5;
	protected Socket client = null;
	protected byte[] data = null;
	protected int lport = 0;
	protected short major = 1;
	protected String method = null;
	protected short mimeType = 0;
	protected short minor = 1;
	protected OutputStream outs = null;
	protected String password = null;
	protected HashMap<String, String> properties;
	protected IServer server;
	private String type = null;
	protected String url = null;
	protected String username = null;

	public Request() {
		properties = new HashMap<String, String>(10);
	}

	public byte[] getData() {
		return data;
	}

	public Set<String> getHeaderNames() {
		return properties.keySet();
	}

	public String getHost() {
		return properties.get("host");
	}

	final public String getMethod() {
		return method;
	}

	public OutputStream getOutStream() {
		return outs;
	}

	public final String getPassword() {
		if (password.isEmpty())
			getUsername();
		return password;
	}

	public int getPort() {
		return lport;
	}

	public String getProperty(final String name) {
		return properties.get(name);
	}

	public String getQueryString() {
		try {
			return url.substring(url.indexOf('?') + 1);
		} catch (Exception e) {
		}
		return "";
	}

	public final String getRemoteAddr() {
		if (client == null)
			return null;
		return client.getInetAddress().getHostAddress();
	}

	public final String getRemoteHost() {
		if (client == null)
			return null;
		return client.getInetAddress().getHostName();
	}

	public String getRequest() {
		return new String(data);
	}

	public short getRequestType() {
		return mimeType;
	}

	public String getRequestURI() {
		int sep = url.indexOf('?');
		if (sep == -1)
			return url;
		return url.substring(0, sep);
	}

	public IServer getServer() {
		return server;
	}

	public final String getUsername() {
		try {
			Decoder b64 = Base64.getDecoder();
			String auth = getProperty("authorization");
			if (!auth.isEmpty()) {
				StringTokenizer st = new StringTokenizer(auth, " ");
				st.nextToken();
				byte[] b = b64.decode(st.nextToken());
				String basic = new String(b);
				st = new StringTokenizer(basic, ":");
				if (st.hasMoreTokens())
					username = st.nextToken();
				if (st.hasMoreTokens())
					password = st.nextToken();
			}
		} catch (NullPointerException npe) {
		}
		return username;
	}

	public final boolean isSecure() {
		try {
			int cport = client.getLocalPort();
			return (cport == server.getSecurePort());
		} catch (NullPointerException npe) {
		}
		return false;
	}

	final public short majorVersion() {
		return major;
	}

	final public short minorVersion() {
		return minor;
	}

	public Response processRequest(final IServer server, final OutputStream out) {

		int status = 200;
		Response response = null;
		boolean respond = true;
		String contentType = null;
		outs = out;
		ByteArrayOutputStream result = null;
		MimeBase mime = null;
		this.server = server;

		try {
			lport = client.getLocalPort();

			switch (mimeType) {

			case XML_TEXT:
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.MimeXML_" + type).newInstance();
				contentType = HTTP.contentTypeXML;
				break;

			case DATA_TEXT:
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.MimeCommand_" + type).newInstance();
				contentType = HTTP.contentTypeData;
				break;

			case COMMAND:
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.MimeCommand_" + type).newInstance();
				contentType = HTTP.contentTypeHTML;
				break;

			case HTML_TEXT:
				if (type == null)
					type = "Processor";
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.MimeHTML_HTTP").newInstance();
				mime.init(url, server);
				contentType = mime.getContentType();
				respond = false;
				break;

			case XML_WS:
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.MimeXML_WS").newInstance();
				mime.init(url, server);
				respond = true;
				break;

			case MIME_BINARY:
				mime = (MimeBase) Class.forName("org.amg.node.mimehandlers.Mime_Binary").newInstance();
				mime.init(url, server);
				contentType = HTTP.contentTypeBinary;
				respond = true;
				break;
			}

			if (mime != null) {
				result = mime.process(server, this);
				status = mime.getStatus();
			}

			if (!respond)
				return null;

			response = new Response(out, getHost(), contentType);
			response.setResponse(result);
			response.setStatus(status);
		} catch (Exception e) {
			return null;
		}
		return response;
	}

	public void putProperty(final String name, final String value) {
		properties.put(name, value);
	}

	public void putRequest(byte[] data, final String requestType, final String type) {

		this.type = type;
		final String mime = properties.get("content-type");

		if (requestType.equals("POST") && data != null) {
			if (mime == null || mime.startsWith("text/xml")) {
				final String request = new String(data);
				final int start = request.indexOf("<?xml");
				if (start >= 0) {
					mimeType = XML_TEXT;
					final int length = data.length - start;
					this.data = new byte[length];
					System.arraycopy(data, start, this.data, 0, length);
				}
			} else if (mime != null && mime.startsWith("application/x-www-form-urlencoded")) {
				this.data = data;
				try {
					data = URLDecoder.decode(new String(data), "UTF-8").getBytes();
				} catch (Exception e) {
				}
				mimeType = COMMAND;
			} else if (mime.startsWith("application/ws+xml")) {
				mimeType = XML_WS;
			} else if (mime.startsWith("application/octet-stream")) {
				mimeType = MIME_BINARY;
			} else {
				mimeType = DATA_TEXT;
			}
		} else if (requestType.equals("GET")) {
			this.data = url.getBytes();
			mimeType = HTML_TEXT;
		} else if (requestType.equals("PUT")) {
			mimeType = HTML_TEXT;
		} else if (requestType.equals("DELETE")) {
			mimeType = HTML_TEXT;
		} else if (requestType.equals("LINK")) {
			mimeType = HTML_TEXT;
		} else if (requestType.equals("UNLINK")) {
			mimeType = HTML_TEXT;
		} else {
			mimeType = HTML_TEXT;
		}
	}

	public void setSocket(final java.net.Socket s) {
		client = s;
	}

	final public void setVersionMethod(final String data) throws Exception {
		try {
			String tokens[] = data.split(" ");
			method = tokens[0];
			url = tokens[1];
			String version = tokens[2];
			tokens = version.substring(5).split( ".");
			major = Short.parseShort(tokens[0]);
			minor = Short.parseShort(tokens[1]);
		} catch (final Exception e) {
			throw new Exception("Error parsing Version/URL/Method : " + data);
		}
	}
}
