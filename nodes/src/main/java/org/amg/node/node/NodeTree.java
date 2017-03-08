package org.amg.node.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.amg.node.messaging.Address;
import org.amg.node.messaging.Message;

public class NodeTree {

	private Map<Address, Node> nodes = (Map<Address, Node>) Collections
			.synchronizedMap(new HashMap<Address, Node>());

	private Node parent;

	public NodeTree(Node parent) {
		this.parent = parent;
	}

	public Node insert(NodeEntry nodeEntry) {
		Node node = new Node(nodeEntry);
		nodes.put(nodeEntry.getAddress(), node);
		return node;
	}

	public void insert(Node node) {
		nodes.put(node.getEntry().getAddress(), node);
	}

	public Node getConnectionNode(int inc, Node link) {

		Node node = null;
		Iterator<Node> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				if (node.getConnectionNode(inc, parent) != null)
					break;
		}

		return node;
	}

	public void computeAverage(Node link) {

		Node node = null;
		Node.average = (Node.average + nodes.size()) / 2;
		Iterator<Node> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.computeAverage(parent);
		}
	}

	public void balance(Node link) {
		Node node = null;
		if (nodes.size() > Node.average) {
			// TODO: balance
		}
		Iterator<Node> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.balance(parent);
		}
	}

	public void optimize(Node link) {

		Node node;
		Iterator<Node> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.optimize(parent);
		}
	}

	public void setAlive(Node link, boolean bAlive) {

		for(Node node : nodes.values()) {
			if (!link.equals(node)) {
				node.setAlive(parent, bAlive);
			}
		}
	}

	public int size() {
		return nodes.size();
	}

	public long count(Node link) {
		long l = nodes.size();
		for(Node node : nodes.values()) {
			if (link != null && (link.equals(node) == false))
				l += node.count(parent);
		}
		return l;
	}

	public Node get(Address tag) {
		return nodes.get(tag);
	}

	public Node find(String tag, Node link) {
		return find(new Address(tag), link);
	}

	public Node find(Address tag, Node link) {
		Node node = null;
		try {
			node = nodes.get(tag);
			if (node == null) {
				for(Node value : nodes.values()) {
					if (link.equals(value) == false)
						if ((node = value.find(tag, parent)) != null)
							break;
				}
			}
		} catch (NullPointerException e) {
			node = null;
		}
		return node;
	}

	public boolean remove(String tag, Node link) {
		return remove(new Address(tag), link);
	}

	public boolean remove(Address address, Node link) {
		if (nodes.remove(address) != null)
			return true;

		for(Node node : nodes.values()) {
			if (link.equals(node) == false)
				if (node.remove(address, parent) == true)
					return true;
		}
		return false;
	}

	public void write(StringBuffer buffer) {
		write(buffer, null);
	}

	public void write(StringBuffer buffer, Node link) {

		Node node = null;
		Iterator<Node> it = nodes.values().iterator();
		if (it.hasNext()) {
			// Lets check to see if we have any nodes to write.
			int count = 0;
			if (link != null) {
				do {
					node = it.next();
					if (link.equals(node) == false) {
						count++;
						break;
					}
				} while (it.hasNext());
				// If we don't find any exist
				if (count == 0)
					return;
				it = nodes.values().iterator();
			}
			do {
				node = it.next();
				if (link == null || link.equals(node) == false)
					node.write(buffer, parent);
			} while (it.hasNext());

		}
	}

	public void display(Node link) {

		Node node = null;
		Iterator<Node> it = nodes.values().iterator();
		if (it.hasNext()) {
			// Lets check to see if we have any nodes to write.
			int count = 0;
			if (link != null) {
				do {
					node = it.next();
					if (link.equals(node) == false) {
						count++;
						break;
					}
				} while (it.hasNext());
				// If we don't find any exit
				if (count == 0)
					return;
				it = nodes.values().iterator();
			}

			do {
				node = it.next();
				if (link == null || link.equals(node) == false)
					node.display(parent);
			} while (it.hasNext());
		}
	}

	public void clear(Node link) {

		for(Node child : nodes.values()) {
			if (link == null || link.equals(child) == false) {
				child.clear(parent);
			}
		}
		nodes.clear();
	}

	public void flatten(List<Node> list, Node link) {

		for(Node child : nodes.values()) {
			if (link.equals(child) == false) {
				list.add(child);
				child.flatten(list, parent);
			}
		}
	}

	public boolean disconnected(Node node) {

		if (node != null) {
			nodes.remove(node.getEntry().getAddress());
			return true;
		}
		return false;
	}

	public void getLeaves(List<Node> list, Node link) {
		for(Node child : nodes.values()) {
			if (link.equals(child) == false) {
				if (child.isLeaf()) {
					list.add(child);
					continue;
				}
				child.getLeaves(list, parent);
			}
		}
	}

	public int getRoute(String tag, List<Node> list, Node link) {
		return getRoute(new Address(tag), list, link);
	}

	public int getRoute(Address tag, List<Node> list, Node link) {
		int nRet = 0;

		Node node = nodes.get(tag);
		if (node != null) {
			list.add(node);
			nRet = 1;
		} else {
			for(Node child : nodes.values()) {
				if (link.equals(child) == false) {
					if ((nRet = child.getRoute(tag, list, parent)) != 0) {
						nRet++;
						list.add(child);
						break;
					}
				}
			}
		}
		return nRet;
	}

	public int getPath(String tag, String sPath, Node link) {
		return getPath(new Address(tag), sPath, link);
	}

	public int getPath(Address address, String sPath, Node link) {
		int nRet = 0;
		Node node = nodes.get(address);
		if (node != null) {
			sPath += address.getURI();
			nRet = 1;
		} else {
			Node child = null;
			Iterator<Node> it = nodes.values().iterator();
			Iterator<Address> itKeys = nodes.keySet().iterator();
			while (it.hasNext()) {
				child = it.next();
				if (link.equals(child) == false) {
					if ((nRet = child.getPath(address, sPath, parent)) != 0) {
						nRet++;
						sPath += itKeys.next();
						sPath += ";";
						break;
					}
				}
				itKeys.next();
			}
		}
		return nRet;
	}

	protected final void broadcastMessage(String processor, String request,
			Object args[]) {
		try {
			Message message = new Message();
			message.getAddress().setProcessor(processor);
			message.getAddress().setMethod(request);
			broadcastMessage(message, args);
		} catch (Exception e) {
		}
	}

	protected final void broadcastMessage(Message message, Object args[]) {

		try {
			List<Node> list = new ArrayList<Node>(10);
			flatten(list, parent);
			Address sender = message.getReplyTo();
			for(Node node : list) {
				if(!sender.equals(node.getAddress())) {
					node.postMessage(message, args);
				}
			}
		} catch (Exception e) {
		}
	}

	public Iterator<Node> values() {
		return nodes.values().iterator();
	}

	public void getNodes(List<Node> list) {
		list.addAll(nodes.values());
	}
}