package org.amg.node.socket;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.TimeUnit;

public class HttpTimeoutHandler extends URLStreamHandler {
	private int connectTimeout;
	private int readTimeout;
	private Proxy proxy;
	private ProxySelector proxySelector;
    private CookieHandler cookieHandler; 


	public void setConnectTimeout(long timeout, TimeUnit unit) {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout < 0");
		}
		if (unit == null) {
			throw new IllegalArgumentException("unit == null");
		}
		long millis = unit.toMillis(timeout);
		if (millis > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Timeout too large.");
		}
		connectTimeout = (int) millis;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setReadTimeout(long timeout, TimeUnit unit) {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout < 0");
		}
		if (unit == null) {
			throw new IllegalArgumentException("unit == null");
		}
		long millis = unit.toMillis(timeout);
		if (millis > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Timeout too large.");
		}
		readTimeout = (int) millis;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public HttpTimeoutHandler setProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public HttpTimeoutHandler setProxySelector(ProxySelector proxySelector) {
		this.proxySelector = proxySelector;
		return this;
	}

	public ProxySelector getProxySelector() {
		return proxySelector;
	}

	public HttpTimeoutHandler setCookieHandler(CookieHandler cookieHandler) { 
	    this.cookieHandler = cookieHandler; 
	    return this; 
	  } 
	 
	  public CookieHandler getCookieHandler() { 
	    return cookieHandler; 
	  } 
	  
	  
      @Override
      protected URLConnection openConnection(URL url) throws IOException {
        URL target = new URL(url.toString());
        URLConnection connection = target.openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        return(connection);
      }		 
}