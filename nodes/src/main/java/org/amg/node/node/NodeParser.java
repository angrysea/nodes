package org.amg.node.node;

import java.util.Stack;

import org.amg.node.xmltools.parser.Attributes;
import org.amg.node.xmltools.parser.DefaultHandler;


final public class NodeParser extends DefaultHandler {

	private static final int NONE = 0;

	private static final int NODEENTRY = 1;

	private static final int IDENTITY = 2;

	private static final int NAME = 3;

	private static final int EMAIL = 4;

	private static final int TYPE = 5;

	private static final int TIME = 6;

	private NodeEntry nodeEntry = null;

	private Node node = null;

	private Node current = null;

	private NodeRoot root = null;

	private Stack<Node> nodeStack = new Stack<Node>();

	private int state = 0;

	public NodeParser(Node root) {
		super();
		current = root;
	}

	public NodeParser(NodeRoot root) {
		super();
		this.root = root;
		current = root;
	}

	public void startElement(String uri, String tag, String qtag,
			Attributes attrs) {

		if (tag.equals("Node")) {
			if (node != null) {
				nodeStack.push(current);
				current = node;
			}

			if (nodeEntry != null) {
				node = current.insert(nodeEntry);
			}

			nodeEntry = new NodeEntry();
			state = NODEENTRY;
		} else if (tag.equals("URI")) {
			state = IDENTITY;
		} else if (tag.equals("Name")) {
			state = NAME;
		} else if (tag.equals("Email")) {
			state = EMAIL;
		} else if (tag.equals("Type")) {
			state = TYPE;
		} else if (tag.equals("Time")) {
			state = TIME;
		}
	}

	public void characters(char buffer[], int start, int length) {

		switch (state) {
		case IDENTITY:
			nodeEntry.setURL(new String(buffer, start, length));
			break;

		case NAME:
			nodeEntry.setName(new String(buffer, start, length));
			break;

		case EMAIL:
			nodeEntry.setEmail(new String(buffer, start, length));
			break;

		case TYPE:
			nodeEntry.setType(new String(buffer, start, length));
			break;

		case TIME:
			nodeEntry.setTime(new String(buffer, start, length));
			break;

		default:
			break;
		}
		state = NONE;
	}

	public void endElement(String uri, String name, String qname) {

		if (name.equals("Node")) {
			if (nodeEntry == null) {
				if (nodeStack.empty() == false) {
					current = nodeStack.peek();
					nodeStack.pop();
				}
			} else {
				if (node != null) {
					nodeStack.push(current);
					current = node;
				}

				try {
					if (root.isConnected(nodeEntry) == null) {
						current.insert(nodeEntry);
					}
				} catch (Exception e) {
					// not sure why this would happen but best just to ignore.
				}
			}
			nodeEntry = null;
		}
	}

}