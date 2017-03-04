package org.adaptinet.node.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

public class HttpTimeoutHandler extends sun.net.www.protocol.http.Handler {
	int fiTimeoutVal;

	HttpURLConnectionTimeout fHUCT;

	public HttpTimeoutHandler(int iT) {
		fiTimeoutVal = iT;
	}

	protected java.net.URLConnection openConnection(URL u) throws IOException {
		return fHUCT = new HttpURLConnectionTimeout(u, this, fiTimeoutVal);
	}

	String GetProxy() {
		return proxy;
	}

	int GetProxyPort() {
		return proxyPort;
	}

	public void Close() throws Exception {
		fHUCT.Close();
	}

	public Socket GetSocket() {
		return fHUCT.GetSocket();
	}
}