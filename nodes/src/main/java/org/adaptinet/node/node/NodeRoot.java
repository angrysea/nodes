package org.adaptinet.node.node;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.adaptinet.node.messaging.Address;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.messaging.Messenger;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.xmltools.parser.InputSource;
import org.adaptinet.node.xmltools.parser.XMLReader;

final public class NodeRoot extends Node {

	public NodeRoot(IServer server) {
		super();
		this.nodeEntry = new NodeEntry(server, false);
		this.nodeEntry.setName(server.getHost());
	}

	public NodeRoot(NodeEntry nodeEntry) {
		super(nodeEntry);
	}

	public NodeRoot() {
		super();
		this.nodeEntry = new NodeEntry();
	}

	static public void initRoot() {
		messenger = new Messenger();
	}

	public Node isConnected(NodeEntry entry) throws Exception {

		if (entry.getURL().equals(getURL())) {
			return null;
		}
		// Check to see if this node already exists in our segment.
		return tree.find(entry.getURL(), this);
	}

	/**
	 * When finding a node to insert a lot has to be determined.
	 */
	public Node doConnect(NodeEntry entry) throws Exception {

		/**
		 * Now we need to find the node that is willing to accept the request
		 */
		try {
			/**
			 * First try to find a node that is within the average and increment
			 * up until max;
			 */
			Node node = getConnectionNode();

			// Check to see if we are the insertion node.
			if (node == this) {
				return insert(entry);
			}

			/**
			 * If no one volunteers to take this node we will have to go outside
			 * our segment. Two help balance lets find a leave with the lowest
			 * count.
			 */
			if (node == null) {
				List<Node> list = new ArrayList<Node>();
				tree.getLeaves(list, this);
				Iterator<Node> it = list.iterator();
				int lowest = Integer.MAX_VALUE;
				Node temp = null;
				while (it.hasNext()) {
					temp = it.next();
					if (lowest > temp.size()) {
						lowest = temp.size();
						node = temp;
					}
				}
				// The node with the lowest count should fall through
			}

			// Most likely candidate for accepting the request.
			if (node != null) {
				Object args[] = new Object[3];
				args[0] = new String(entry.getURL());
				args[1] = new String(entry.getName());
				args[2] = new Boolean(true);
				node.postMessage("main", "connect", args);
			}
		} catch (Exception e) {
			// Need to do something better but this will due for debugging
			e.printStackTrace();
			throw e;
		}
		return null;
	}

	public final Node getConnectionNode() {

		try {
			int inc = 0;
			Node node = null;
			while (inc + average <= max) {
				/**
				 * Before we go crazy maybe I'm the insertion node Its a simple
				 * Check so lets do it first;
				 */
				if (inc + average <= count()) {
					return this;
				}
				/**
				 * This must be full so We'll have to check to see who can do
				 * the connection.
				 */
				if ((node = tree.getConnectionNode(inc, this)) != null)
					return node;
				inc++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final void maintenance() {

		try {
			if (isAlive()) {
				List<Node> list = new ArrayList<Node>(10);
				tree.flatten(list, this);
				for (Node node : list) {
					node.doMaintenance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void cache() {

		try {
			if (isAlive()) {
				List<Node> list = new ArrayList<Node>(10);
				tree.flatten(list, this);
				for (Node node : list) {
					node.doCache();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refuseConnection(String node) {

		// Check to see if this node is in our segment anyway.
		if (tree.find(node, this) != null)
			return;

		Message message = new Message(node);
		message.getAddress().setProcessor("main");
		message.getAddress().setMethod("remove");
		Object args[] = new Object[1];
		args[0] = new String(nodeEntry.getAddress().getURL());
		Messenger.postMessage(message, args);
	}

	/**
	 * This is called through the NetworkAgent and is initiated by the
	 * server form shutdown to disconnect from the network
	 */
	public final void doDisconnect() throws Exception {

		try {
			// Get the message ready.
			Object args[] = null;
			args = new Object[1];
			args[0] = new String(getEntry().getURL());
			tree.broadcastMessage("main", "remove", args);
		} catch (Exception e) {
			// TODO: add some reporting change exception type
			throw e;
		}
	}

	/**
	 * This is called through the NetworkAgent to inform this node that an
	 * adjacent node has dropped out of the node network and we are stepping in
	 */
	final public void doReplace(String address, String xml) throws Exception {

		try {
			Address replaceAt = new Address(address);
			remove(replaceAt);
			Iterator<Node> it = tree.values();

			// First we need to find our replacement
			while (it.hasNext()) {
				Node node = (Node) it.next();
				// Make sure we are not sending it back to who
				// we are replacing.
				// if(replaceAt.hashCode()==node.getKey())
				// continue;

				Message message = new Message(node.getEntry().getURL());
				message.getAddress().setProcessor("main");
				message.getAddress().setMethod("replace");
				message.setKey(nodeEntry.getKey());

				remove(node.getAddress());
				// Prepare the parameters
				Object args[] = new Object[2];
				args[0] = new String(nodeEntry.getURL());
				args[1] = toString();

				// Post message to our sub
				node.postMessage(message, args);
				break;
			}
			//
			// Update our new structure
			parseNodes(xml.getBytes());

			Message message = new Message();
			message.getAddress().setProcessor("main");
			message.getAddress().setMethod("update");
			message.setKey(nodeEntry.getKey());

			// Prepare the parameters
			Object args[] = new Object[2];
			args[0] = address;
			args[1] = new String(nodeEntry.getURL());
			broadcastMessage(message, args);

		} catch (Exception e) {
			// TODO: add some reporting change exception type
			throw e;
		}
	}

	/**
	 * This will be called by an adjacent node when it is dropping out of the
	 * network or being optimized out.
	 */
	public void parseNodes(byte bytes[]) throws Exception {

		try {
			if (bytes.length > 0) {
				XMLReader parser = new XMLReader();
				parser.setContentHandler(new NodeParser(this));
				parser.parse(new InputSource(new ByteArrayInputStream(bytes)));
			}
		} catch (Exception x) {
			throw x;
		}
	}
	
	public void parseNodes(String xml) throws Exception {

		try {
			if (xml.length() > 0) {
				XMLReader parser = new XMLReader();
				parser.setContentHandler(new NodeParser(this));
				xml = XMLReader.normalize(xml);
				parser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())));
			}
		} catch (Exception x) {
			throw x;
		}
	}
	
	public final void setAlive(boolean bAlive) {
		nodeEntry.setAlive(bAlive);
		tree.setAlive(this, bAlive);
	}
	
	public final long count() {
		return count(null);
	}

	public final int getRoute(String tag, List<Node> list) {
		return tree.getRoute(tag, list, this);
	}

	public final int getRoute(Address tag, List<Node> list) {
		return tree.getRoute(tag, list, this);
	}

	public final int getPath(String tag, String sPath) {
		return tree.getPath(tag, sPath, this);
	}

	public final int getPath(Address tag, String sPath) {
		return tree.getPath(tag, sPath, this);
	}

	public final void getLeaves(List<Node> list) {
		tree.getLeaves(list, this);
	}

	public final void computeAverage() {
		Node.average = (int) count();
		if (Node.average > 0) {
			Node.average /= 2;
		}
		tree.computeAverage(this);
	}

	public final void optimize() {
		tree.optimize(this);
	}

	public void balance() {
		tree.balance(this);
	}

	public final void flatten(List<Node> list) {
		tree.flatten(list, this);
	}
}
