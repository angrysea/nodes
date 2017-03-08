package org.amg.node.servlet.http;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public class HttpSession {
	private String sessionId;
	private Date date = new Date();
	private Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	private boolean isNew = true;
	private int maxAge = 10;
	private boolean isValid = true;

	public void setNew(boolean New) {
		isNew = New;
	}

	public HttpSession() {
		sessionId = Integer.toString(this.hashCode());
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public Enumeration<String> getAttributeNames() {
		return attributes.keys();
	}

	public long getCreationTime() {
		return date.getTime();
	}

	public String getId() {
		return sessionId;
	}

	public long getLastAccessedTime() {
		return (new Date()).getTime();
	}

	public int getMaxInactiveInterval() {
		return maxAge;
	}

	public Object getValue(String key) {
		return attributes.get(key);
	}

	public String[] getValueNames() {
		return (String[]) attributes.keySet().toArray();
	}

	public void invalidate() {
		isValid = false;
	}

	public boolean isvalid() {
		return isValid;
	}

	public boolean isNew() {
		return isNew;
	}

	public void putValue(String key, Object value) {
		attributes.put(key, value);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public void removeValue(String key) {
		attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public void setMaxInactiveInterval(int interval) {
		maxAge = interval;
	}
}