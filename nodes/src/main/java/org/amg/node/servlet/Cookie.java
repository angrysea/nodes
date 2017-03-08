package org.amg.node.servlet;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Cookie implements Cloneable {
	private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
	private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);
	/*
		err.cookie_name_is_token = Cookie name "{0}" is a reserved token 
		err.io.negativelength = Negative Length given in write method 
		err.io.short_read = Short Read 
		http.method_delete_not_supported = Http method DELETE is not supported by this URL 
		http.method_get_not_supported = HTTP method GET is not supported by this URL 
		http.method_not_implemented = Method {0} is not defined in RFC 2068 and is not supported by the Servlet API  
		http.method_post_not_supported = HTTP method POST is not supported by this URL 
		http.method_put_not_supported = HTTP method PUT is not supported by this URL 
	*/
	private String name;
	private String value;

	private String comment;
	private String domain;
	private int maxAge = -1;
	private String path;
	private boolean secure;
	private int version = 0;

	public Cookie(String name, String value) {
		if (!isToken(name) || name.equalsIgnoreCase("Comment")
				|| name.equalsIgnoreCase("Discard")
				|| name.equalsIgnoreCase("Domain") || name.equalsIgnoreCase("Expires")
				|| name.equalsIgnoreCase("Max-Age")
				|| name.equalsIgnoreCase("Path") || name.equalsIgnoreCase("Secure") || name.equalsIgnoreCase("Version")
				|| name.startsWith("$")) {
			String errMsg = lStrings.getString("err.cookie_name_is_token");
			Object[] errArgs = new Object[1];
			errArgs[0] = name;
			errMsg = MessageFormat.format(errMsg, errArgs);
			throw new IllegalArgumentException(errMsg);
		}

		this.name = name;
		this.value = value;
	}

	public void setComment(String purpose) {
		comment = purpose;
	}

	public String getComment() {
		return comment;
	}

	public void setDomain(String pattern) {
		domain = pattern.toLowerCase();
	}

	public String getDomain() {
		return domain;
	}

	public void setMaxAge(int expiry) {
		maxAge = expiry;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setPath(String uri) {
		path = uri;
	}

	public String getPath() {
		return path;
	}

	public void setSecure(boolean flag) {
		secure = flag;
	}

	public boolean getSecure() {
		return secure;
	}

	public String getName() {
		return name;
	}

	public void setValue(String newValue) {
		value = newValue;
	}

	public String getValue() {
		return value;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int v) {
		version = v;
	}

	private static final String tspecials = ",; ";

	private boolean isToken(String value) {
		int len = value.length();

		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);

			if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
				return false;
		}
		return true;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
