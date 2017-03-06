package org.adaptinet.node.http;

import java.net.URL;

public class HttpClient {
	
	private String proxy;
	private int proxyPort;
	private URL url;

	public HttpClient(URL url, String proxy, int proxyPort) {
		this.url = url;
		this.proxy = proxy;
		this.proxyPort = proxyPort;
	}

	public String getProxy() {
		return proxy;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public URL getUrl() {
		return url;
	}
	public void setProxy(String proxy) {
		this.proxy = proxy;
	} 
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}

}
