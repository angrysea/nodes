package org.amg.node.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.amg.node.cache.CacheProcessor;
import org.amg.node.messaging.Address;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.processoragent.MaintenanceProcessor;

public class Node {

	static int average = 0;
	static public short LEFT = -1;
	static int levels = 5;
	static int max = 10;
	static Messenger messenger = null;
	static int min = 5;
	static public short RIGHT = 1;
	
	static private String normalize(String s) {
		StringBuffer str = new StringBuffer();

		int len = (s != null) ? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '<': {
				str.append("&lt;");
				break;
			}
			case '>': {
				str.append("&gt;");
				break;
			}
			case '&': {
				str.append("&amp;");
				break;
			}
			case '"': {
				str.append("&quot;");
				break;
			}
			default: {
				str.append(ch);
			}
			}
		}
		return str.toString();
	}
	
	static public void setLevels(int levels) {
		Node.levels = levels;
	}

	static public void setMax(int max) {
		Node.max = max;
		min = max / 2;
	}

	protected NodeEntry nodeEntry = null;

	protected NodeTree tree = null;

	public Node() {
		tree = new NodeTree(this);
	}

	public Node(NodeEntry nodeEntry) {
		this.nodeEntry = nodeEntry;
		tree = new NodeTree(this);
	}

	public void balance(Node link) {
		try {
			tree.balance(link);
		} catch (Exception e) {
		}
	}

	public void broadcastMessage(Message message, Object args[]) {
		try {
			tree.broadcastMessage(message, args);
		} catch (Exception e2) {
		}
	}

	final public void broadcastMessage(String processor, String request, Object args[]) {
		try {
			Message message = new Message(nodeEntry.getURL());
			message.getAddress().setProcessor(processor);
			message.getAddress().setMethod(request);
			broadcastMessage(message, args);
		} catch (Exception e2) {
		}
	}

	public final void clear(Node link) {
		try {
			tree.clear(link);
			tree = new NodeTree(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void computeAverage(Node link) {
		try {
			tree.computeAverage(link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final long count(Node link) {
		try {
			return tree.count(link);
		} catch (Exception e) {
		}
		return 0;
	}

	public void display(Node link) {
		try {
			System.out.println("<Node>");
			System.out.println("<URI>");
			System.out.println(nodeEntry.getNameURL());
			System.out.println("</URI>");
			System.out.println("<Name>");
			System.out.println(nodeEntry.getName());
			System.out.println("</Name>");
			System.out.println("<Time>");
			System.out.println(nodeEntry.getTime());
			System.out.println("</Time>");
			tree.display(link);
			System.out.println("</Node>");
		} catch (Exception e) {
		}
	}

	public final void doCache() {

		try {
			CacheProcessor.doCacheCheck(nodeEntry.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void doMaintenance() {

		try {
			MaintenanceProcessor.doPing(nodeEntry.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object object) {

		if (this == object)
			return true;
		else if (object == null)
			return false;

		try {
			return nodeEquals((Node) object);
		} catch (Exception e) {
		}
		return false;

	}

	public final Node find(Address tag) {
		if (tag.equals(nodeEntry.getAddress()))
			return this;
		return tree.find(tag, this);
	}

	public final Node find(Address tag, Node link) {
		try {
			return tree.find(tag, link);
		} catch (Exception e) {
		}
		return null;
	}

	// Find a node through out the known network
	public final Node find(String tag) {
		return find(new Address(tag));
	}

	// Find a node through out the known network
	public final Node find(String tag, Node link) {
		try {
			return tree.find(tag, link);
		} catch (Exception e) {
		}
		return null;
	}

	public final void flatten(List<Node> list, Node link) {
		try {
			tree.flatten(list, link);
		} catch (Exception e) {
		}
	}

	public final Node get(Address tag) {
		try {
			return tree.get(tag);
		} catch (Exception e) {
		}
		return null;
	}

	// Find a local node
	public final Node get(String tag) {
		try {
			return get(new Address(tag));
		} catch (Exception e) {
		}
		return null;
	}

	final public Address getAddress() {
		return nodeEntry.getAddress();
	}

	public final void getAdjacent(List<Node> list) {
		tree.getNodes(list);
		list.remove(this);
	}

	public final Node getConnectionNode(int inc, Node link) {

		try {
			if(isAlive()) {
				long s = count(link);
				if (s < min || s < (average + inc))
					return this;
			}
			return tree.getConnectionNode(inc, link);
		} catch (Exception e) {
		}
		return null;
	}
	
	final public NodeEntry getEntry() {
		return nodeEntry;
	}
	
	public int getKey() {
		try {
			return nodeEntry.getKey();
		} catch (Exception e) {
			return 0;
		}
	}

	public final void getLeaves(List<Node> list, Node link) {
		try {
			tree.getLeaves(list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public final int getPath(Address tag, String sPath, Node link) {
		try {
			return tree.getPath(tag, sPath, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getPath(String tag, String sPath, Node link) {
		try {
			return tree.getPath(tag, sPath, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getRoute(Address tag, List<Node> list, Node link) {
		try {
			return tree.getRoute(tag, list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getRoute(String tag, List<Node> list, Node link) {
		try {
			return tree.getRoute(tag, list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	final public String getURL() {
		return nodeEntry.getURL();
	}

	/*
	 * Insert a node in the current tree
	 */
	public final Node insert(Node node) {

		try {
			// Insert into current tree.
			tree.insert(node);
			// Insert this node into the insert node reverse spin
			node.link(this);
			return node;
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * Insert a entry in the current tree
	 */
	public final Node insert(NodeEntry entry) {

		try {
			// Insert into current tree.
			Node node = tree.insert(entry);
			// Insert this node into the insert node reverse spin
			node.link(this);
			return node;
		} catch (Exception e) {
		}
		return null;
	}

	public final boolean isAlive() {
		return nodeEntry.isAlive();
	}

	final boolean isLeaf() {
		boolean ret = true;
		if (tree != null) {
			if (tree.size() > 0) {
				ret = false;
			}
		}
		return ret;
	}

	public boolean keyEquals(int value) {
		try {
			final int key = nodeEntry.getKey();
			if(value!=0 && key==value) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/*
	 * Link a node in the current tree
	 */
	public final void link(Node node) {

		try {
			// Insert into current tree.
			tree.insert(node);
		} catch (Exception e) {
		}
	}

	public boolean nodeEquals(Node node) {
		return nodeEntry.getAddress().hashCode() == node.getEntry()
				.getAddress().hashCode();
	}

	public void notifyInserted(String address) {

		List<Node> list = new ArrayList<Node>(10);
		tree.flatten(list, this);

		Object args[] = new Object[2];
		args[0] = new String(getEntry().getURL());
		args[1] = new String(address);
		list.stream()
			.filter(e -> e.isAlive())
			.forEach(e -> e.sendNotifyInserted(args));
	}

	public final void notifyRemoved(Object args[]) {
		postMessage("main", "remove", args);
	}

	public final void optimize(Node link) {

		try {
			tree.optimize(link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final void postMessage(Message message, Object args[]) {
		try {
			if(isAlive()) {
				message.getAddress().setHost(nodeEntry.getAddress().getHost());
				message.getAddress().setPort(nodeEntry.getAddress().getPort());
				// System.out.println("Sending Message : " + message.getAddress().getURI());
				Messenger.postMessage(message, args);
			}
			/*
			else {
				 System.out.println("Not Alive : " +
				 message.getAddress().getURI());

			}
			*/
		} catch (Exception e) {
		}
	}

	final void postMessage(String processor, String request, Object args[]) {
		try {
			if(isAlive()) {
				Message message = new Message(nodeEntry.getAddress());
				message.getAddress().setProcessor(processor);
				message.getAddress().setMethod(request);
				Messenger.postMessage(message, args);
			}
		} catch (Exception e) {
		}
	}

	public final boolean remove(Address tag) {
		return tree.remove(tag, this);
	}
	
	public final boolean remove(Address tag, Node link) {
		try {
			return tree.remove(tag, link);
		} catch (Exception e) {
		}
		return false;
	}

	public final boolean remove(NodeEntry entry) {
		return tree.remove(entry.getAddress(), this);
	}
	
	public final boolean remove(NodeEntry entry, Node link) {
		try {
			return tree.remove(entry.getAddress(), link);
		} catch (Exception e) {
		}
		return false;
	}

	public final boolean remove(String tag) {
		return tree.remove(tag, this);
	}

	public final boolean remove(String tag, Node link) {

		try {
			return tree.remove(tag, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public final void sendNotifyInserted(Object args[]) {
		postMessage("main", "insert", args);
	}

	public void setAlive(Node link, boolean bAlive) {
		if (!link.equals(this)) {
			nodeEntry.setAlive(bAlive);
			tree.setAlive(link, bAlive);
		}
	}

	final public void setEntry(NodeEntry nodeEntry) {
		this.nodeEntry = nodeEntry;
	}

	final public int size() {
		return tree.size();
	}

	public String toEscapedString() {
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<Nodes>");
		write(buffer, this);
		buffer.append("</Nodes>");
		//System.out.println(buffer.toString());
		return normalize(buffer.toString());
	}

	public void toScreen() {
		System.out.println("<Nodes>");
		tree.display(this);
		System.out.println("</Nodes>");
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<Nodes>");
		write(buffer, this);
		buffer.append("</Nodes>");
		//System.out.println(buffer.toString());
		return buffer.toString();
	}

	public final Iterator<Node> values() {
		return tree.values();
	}

	public void write(StringBuffer buffer, Node link) {
		try {
			buffer.append("<Node>");
			buffer.append("<URI>");
			buffer.append(nodeEntry.getURL());
			buffer.append("</URI>");
			buffer.append("<Name>");
			buffer.append(nodeEntry.getName());
			buffer.append("</Name>");
			buffer.append("<Time>");
			buffer.append(nodeEntry.getTime());
			buffer.append("</Time>");
			tree.write(buffer, link);
			buffer.append("</Node>");
		} catch (Exception e) {
		}
	}
}
