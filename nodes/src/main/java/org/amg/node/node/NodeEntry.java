package org.amg.node.node;

import org.amg.node.messaging.Address;
import org.amg.node.server.IServer;


final public class NodeEntry {

	private Address address = null;
	private String name = null;
	private String type = null;
	private long time = 0;
	private boolean isAlive = false;

	public NodeEntry() {
		reset();
		address = new Address();
	}

	public NodeEntry(String url) {
		reset();
		address = new Address(url);
		time = System.currentTimeMillis();
	}

	public NodeEntry(Address address) {
		reset();
		this.address = new Address(address);
		time = System.currentTimeMillis();
	}

	public NodeEntry(NodeEntry pe) {
		reset();
		address = new Address(pe.address);
		name = pe.name;
		type = pe.type;
		time = pe.time;
	}

	public NodeEntry(IServer server, boolean bSecure) {
		try {
			StringBuffer buffer = new StringBuffer();
			if (bSecure == false)
				buffer.append("http");
			else
				buffer.append("https");
			buffer.append("://");
			buffer.append(server.getHost());
			buffer.append(":");
			buffer.append(Integer.toString(server.getPort()));
			this.address = new Address(buffer.toString());
			time = System.currentTimeMillis();
		} catch (Exception e) {
		}
	}

	private void reset() {
		address = null;
		name = null;
		type = null;
		time = 0;
	}

	public void setAddress(String address) {
		this.address = new Address(address);
	}

	public Address getAddress() {
		return address;
	}

	public int getKey() {

		try {
			return address.hashCode();
		} catch (Exception e) {
			return 0;
		}
	}

	public void setURL(String url) {
		address.setURL(url);
	}

	public String getURL() {
		return address.getURL();
	}

	public String getNameURL() {
		return address.getNameURL();
	}

	public void setName(String newValue) {
		name = newValue;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String newValue) {
		address.setEmail(newValue);
	}

	public String getEmail() {
		return address.getEmail();
	}

	public void setType(String newValue) {
		type = newValue;
	}

	public String getType() {
		return type;
	}

	public void setTime(String newValue) {
		if (newValue == null || newValue.length() == 0)
			return;
		setTime(Long.parseLong(newValue));
	}

	public void setTime(long newValue) {
		time = newValue;
	}

	public long getTime() {
		return time;
	}

	public final boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean bAlive) {
		isAlive=bAlive;
	}

	public String getTimeAsString() {
		try {
			return Long.toString(time);
		} catch (NumberFormatException nfe) {
			return "-1";
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o == null || getClass() != o.getClass())
			return false;

		NodeEntry p = (NodeEntry) o;
		return (address.equals(p.address) && name.equals(p.name) && type
				.equals(p.type));
	}
}
