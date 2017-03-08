package org.amg.node.messaging;

import java.net.InetAddress;

import org.amg.node.server.IServer;


/**
 * The Address class represents a source or destination URL of a node.
 */
public final class Address {

	private boolean bLocked = false;
	private String email = null;
	private int hashcode = 0;
	private String host = null;
	private String method = null;
	private String name = null;
	private String port = null;
	private String postfix = null;
	private String preffix = null;
	private String processor = null;
	private Address route = null;
	private String type = null;
	private static Address address = new Address(IServer.getServer());
	public static final String SYNC = "sync";

	/**
	 * Retrieves the host name or IP of this Address
	 * 
	 * @return host IP address
	 */
	static public String getMyHost() {
		return address.getHost();
	}
	/**
	 * Retrieves the port used by this Address
	 * 
	 * @return port used by this Address
	 */
	static public String getMyPort() {
		return address.getPort();
	}
	/**
	 * Retrieves the URL this object represents
	 * 
	 * @return URL of this object
	 */
	static public String getMyURL() {
		return address.getURL();
	}
	/**
	 * Constructs an empty Address object with the default (http) prefix
	 */
	public Address() {
		this.preffix = "http";
		computeHashCode();
	}

	/**
	 * Constructs an Address that is a copy of the specified address
	 * 
	 * @param address
	 *            Address to use for initialization
	 */
	public Address(Address address) {
		try {
			if (address != null) {
				if (address.preffix != null)
					preffix = address.preffix;
				if (address.postfix != null)
					postfix = address.postfix;
				if (address.host != null)
					host = new String(address.host);
				if (address.port != null)
					port = new String(address.port);
				if (address.name != null)
					host = new String(address.name);
				if (address.processor != null)
					processor = new String(address.processor);
				if (address.method != null)
					method = new String(address.method);
				if (address.email != null)
					email = new String(address.email);
				if (address.type != null)
					type = new String(address.type);
				hashcode = address.hashcode;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Constucts an Address representing the URI of the specified server
	 * using the default (http) protocol.
	 * 
	 * @param server
	 *            Server to use for initialization
	 */
	public Address(IServer server) {
		this(server, false);
	}
	
	
	/**
	 * Constucts an Address representing the URI of the specified server
	 * 
	 * @param server
	 *            Server to use for initialization
	 * @param bSecure
	 *            true to use the https protocol. false to use the http
	 *            protocol.
	 */
	public Address(IServer server, boolean bSecure) {
		if (bSecure == false)
			this.preffix = "http";
		else
			this.preffix = "https";
		if (server != null) {
			setHost(new String(server.getHost()));
			this.port = Integer.toString(server.getPort());
			computeHashCode();
		}
	}

	/**
	 * Constucts an Address object representing the given URI
	 * 
	 * @param uri
	 *            URI that this object represents
	 */
	public Address(String uri) {
		if (uri != null)
			setURI(uri);
	}

	/**
	 * Checks the host name to see if it is an IP address or not if it is not it
	 * converts the name to an IP address.
	 */
	private void changeHostToIP(String name) {
		// Check to see if the host is already an IP address.
		boolean lastdot = false;
		for (char c : name.toCharArray()) {
			if (c == '.') {
				// If this is true there is come kind of error
				// but try the conversion
				if (lastdot == true)
					return;
			} else if (Character.isDigit(c) == false) {
				changeToIP(name);
				return;
			}
		}

		// If we have dropped down to here and there are
		// we can't be sure lets excpet an ip
		changeToName(name);
	}

	/**
	 * Utility function used by changeHostToIP
	 */
	private void changeToIP(String name) {
		try {
			host = InetAddress.getByName(name).toString();
			int start = 0;
			if ((start = host.indexOf('/')) > -1)
				host = host.substring(start + 1);
			this.name = name;
		} catch (Exception e) {
			try {
				changeToName(name);
			} catch (Exception ee) {
			}
		}
	}

	/**
	 * Utility function used by changeHostToIP
	 */
	private void changeToName(String ip) {
		try {
			host = ip;
			name = ip;
		} catch (Exception e) {
			try {
				changeToIP(ip);
			} catch (Exception ee) {
			}
		}
	}

	/**
	 * Computes a new hash code based on the attributes set on this object
	 */
	private void computeHashCode() {
		if (bLocked == false) {
			hashcode = (host + port).hashCode();
		}
	}

	public final boolean equals(Address compareTo) {
		try {
			if (!host.equals(compareTo.host) ||
				!port.equals(compareTo.port)) {
				return false;
			}
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o == null || getClass() != o.getClass())
			return false;

		Address a = (Address) o;

		return hashcode == a.hashcode;
	}

	/**
	 * Retrieves the email name for this Address
	 * 
	 * @return email name
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Retrieves the host name or IP of this Address
	 * 
	 * @return host IP address
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * Retrieves the host name or IP of this Address
	 * 
	 * @return host name address
	 */
	public String getHostByName() {
		return this.name;
	}

	/**
	 * Retrieves the method name for this Address
	 * 
	 * @return method name
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Retrieves the URL with Host Name this object represents
	 * 
	 * @return URL of this object
	 */
	public String getNameURL() {
		StringBuffer uri = new StringBuffer();
		try {
			if (preffix != null && host != null) {
				uri.append(preffix);
				uri.append("://");
				uri.append(name);
				uri.append(":");
				if (port != null)
					uri.append(port);
				else
					uri.append("8082");
			}
			return uri.toString();
		} catch (NullPointerException ex) {
			return getURL();
		}
	}

	/**
	 * Retrieves the port used by this Address
	 * 
	 * @return port used by this Address
	 */
	public String getPort() {
		return this.port;
	}

	/**
	 * Retrieves the postfix used by this Address
	 * 
	 * @return postfix used
	 */
	public String getPostfix() {
		return this.postfix;
	}

	/**
	 * Retrieves the preffix used by this Address
	 * 
	 * @return preffix used
	 */
	public String getPrefix() {
		return this.preffix;
	}

	/**
	 * Retrieves the name of the processor for this Address
	 * 
	 * @return name of processor
	 */
	public String getProcessor() {
		return this.processor;
	}

	/**
	 * If the node sits behind a firewall all messages will have to be routed
	 * through. This will allow another node to post messages through the
	 * firewall utility.
	 */
	public Address getRoute() {
		if (route != null && route.lastStop() == false)
			return route.getRoute();
		return route;
	}

	/**
	 * Retrieves the type for this Address
	 * 
	 * @return type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Retrieves the URI this object represents
	 * 
	 * @return URI of this object
	 */
	public String getURI() {
		StringBuffer uri = new StringBuffer();
		try {
			if (preffix != null && host != null) {
				uri.append(preffix);
				uri.append("://");
				uri.append(host);
				uri.append(":");
				if (port != null)
					uri.append(port);
				else
					uri.append("8082");
				if (processor != null) {
					uri.append("/");
					uri.append(processor);
					if (method != null) {
						uri.append("/");
						uri.append(method);
					}
				}
			}
			return uri.toString();
		} catch (NullPointerException ex) {
			return getURL();
		}
	}

	/**
	 * Retrieves the URL this object represents
	 * 
	 * @return URL of this object
	 */
	public String getURL() {
		StringBuffer uri = new StringBuffer();
		try {
			if (preffix != null && host != null) {
				uri.append(preffix);
				uri.append("://");
				uri.append(host);
				uri.append(":");
				if (port != null)
					uri.append(port);
				else
					uri.append("8082");
			}
			return uri.toString();
		} catch (NullPointerException ex) {
			return getURL();
		}
	}

	/**
	 * Returns the hash code.
	 * 
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return hashcode;
	}

	public Address hop() {

		Address hop = null;
		if (route != null) {
			if (route.lastStop()) {
				hop = route;
				route = null;
			} else
				hop = route.hop();
		}
		return hop;
	}

	/**
	 * Determines whether this Address is using a secure protocol.
	 * 
	 * @return true if a secure protocol is in use, otherwise false
	 */
	public boolean isSecure() {
		try {
			return preffix.equals("https");
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public final boolean isSync() {
		return (type != null && type.equalsIgnoreCase(SYNC));
	}

	public boolean lastStop() {
		return (route == null);
	}

	/**
	 * Locks the hash code for this object
	 */
	public void lock() {
		bLocked = true;
	}

	/**
	 * Sets the email name used for this Address
	 * 
	 * @param email
	 *            email name
	 */
	public void setEmail(String email) {
		this.email = new String(email);
	}

	/**
	 * Sets the host name or IP address of this Address
	 * 
	 * @param host
	 *            host to set
	 */
	public void setHost(String host) {
		changeHostToIP(host);
		computeHashCode();
	}

	/**
	 * Sets the method name used for this Address
	 * 
	 * @param method
	 *            method name
	 */
	public void setMethod(String method) {
		this.method = new String(method);
	}

	/**
	 * Sets the port number of this Address
	 * 
	 * @param port
	 *            port to set
	 */
	public void setPort(String port) {
		this.port = new String(port);
		computeHashCode();
	}

	/**
	 * Sets the postfix to use for this Address
	 * 
	 * @param postfix
	 *            postfix to set
	 */
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	/**
	 * Sets the preffix to use for this Address
	 * 
	 * @param preffix
	 *            preffix to set
	 */
	public void setPrefix(String preffix) {
		this.preffix = preffix;
	}

	/**
	 * Sets the processor name of this Address
	 * 
	 * @param processor
	 *            name of processor
	 */
	public void setProcessor(String processor) {
		this.processor = new String(processor);
	}

	public void setRoute(Address route) {
		if (this.route != null)
			this.route.setRoute(route);
		this.route = route;
	}

	/**
	 * Sets the security protocol for this address.
	 * 
	 * @param bSecure
	 *            true to use https protocol, false to use the http protocol.
	 */
	public void setSecure(boolean bSecure) {
		if (bSecure == false)
			this.preffix = "http";
		else
			this.preffix = "https";
	}

	public final void setSync() {
		this.type = SYNC;
	}

	/**
	 * Sets the type for this Address
	 * 
	 * @param type
	 *            type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Initializes this object with the specified URI
	 * 
	 * @param uri
	 *            source URI
	 */
	public void setURI(String uri) {

		int start = 0;
		processor = null;
		method = null;

		try {
			preffix = "http";
			if (uri.startsWith("https://")) {
				start = 8;
				preffix = "https";
			} else if (uri.startsWith("http://")) {
				start = 7;
			} else {
				start = 0;
			}

			int next = uri.indexOf(":", start);
			if (next > -1) {
				setHost(new String(uri.substring(start, next)));
				start = next + 1;
				next = uri.indexOf("/", start);
				if (next < 0) {
					port = new String(uri.substring(start));
				} else {
					port = new String(uri.substring(start, next));
					start = next + 1;

				}
			} else {
				if (uri.indexOf("/") == 0) {
					start = 1;
				}
				host = "broadcast";
				port = "0";
				next=0;
			}

			if (next > -1) {
				String[] slices = uri.substring(start).split("/");
				if (slices.length == 0) {
					processor = uri;
				} else {
					processor = slices[0];
					if (slices.length == 2) {
						method = slices[1];
					}
				}
			}
			computeHashCode();
		} catch (NullPointerException ex) {
		}
	}
	
	/**
	 * Initializes this object with the specified URI
	 * 
	 * @param url
	 *            source URL
	 */
	public void setURL(String url) {
		int start = 0;
		processor = null;
		method = null;
		try {
			if (url.startsWith("https://")) {
				start = 8;
				preffix = "https";
			} else {
				preffix = "http";
				if (url.startsWith("http://")) {
					start = 7;
				}
			}

			int next = url.indexOf(":", start);
			if (next > -1) {
				setHost(new String(url.substring(start, next)));
				start = next + 1;
			}
			next = url.indexOf("/", start);
			if (next < 0) {
				port = new String(url.substring(start));
			} else {
				port = new String(url.substring(start, next));
			}
			computeHashCode();
		} catch (NullPointerException ex) {
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(); 
		sb.append(preffix);
		sb.append(":");
		sb.append(postfix);
		sb.append(":");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append("/");
		sb.append(processor);
		sb.append("/");
		sb.append(method);
		sb.append("/");
		sb.append(type);
		sb.append("/");
		sb.append(name);
		return sb.toString();
	}
	
	public void unlock() {
		bLocked = false;
	}
}
