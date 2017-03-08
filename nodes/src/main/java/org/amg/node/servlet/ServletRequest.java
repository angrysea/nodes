package org.amg.node.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface ServletRequest {
    public Object getAttribute(String name);
    public Set<String> getAttributeNames();
    public String getCharacterEncoding();
    public void setCharacterEncoding(String env) throws java.io.UnsupportedEncodingException;
    public int getContentLength();
    public String getContentType();
    public ServletInputStream getInputStream() throws IOException; 
    public String getParameter(String name);
    public Set<String> getParameterNames();
    public String[] getParameterValues(String name);
    public Map<String, String> getParameterMap();
    public String getProtocol();
    public String getScheme();
    public String getServerName();
    public int getServerPort();
    public BufferedReader getReader() throws IOException;
    public String getRemoteAddr();
    public String getRemoteHost();
    public void setAttribute(String name, Object o);
    public void removeAttribute(String name);
    public Locale getLocale();
    public Set<String> getLocales();
    public boolean isSecure();
    public RequestDispatcher getRequestDispatcher(String path);
    public String getRealPath(String path);
    public int getRemotePort();
    public String getLocalName();
    public String getLocalAddr();
}
