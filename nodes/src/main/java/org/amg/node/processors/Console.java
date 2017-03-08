package org.amg.node.processors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.amg.node.messaging.Address;
import org.amg.node.messaging.Message;
import org.amg.node.node.Node;
import org.amg.node.node.NodeEntry;
import org.amg.node.processoragent.Processor;
import org.amg.node.server.NetworkAgent;

public class Console extends Processor {
	boolean packFrame = false;
	ConsoleFrame frame = null;
	java.util.Map<String, Long> pingMap = Collections
			.synchronizedMap(new HashMap<String, Long>());
	NetworkAgent nodes = null;

	public Console() {
	}

	public void nodeUpdate() {
		if (frame != null) {
			frame.clearNodes();
			if (nodes != null) {
				for (Node node : nodes.getNodes()) {
					try {
						NodeEntry entry = node.getEntry();
						frame.insertNode(entry.getAddress().getHost() + ":"
								+ entry.getAddress().getPort());
					} catch (NullPointerException e) {						
					}
				}
			}
		}
	}

	public void init() {
		frame = new ConsoleFrame();

		if (packFrame)
			frame.pack();
		else
			frame.validate();
		frame.setVisible(true);
		frame.setProcessor(this);

		// Grab the nodes out of the node list
		NodeEntry entry = null;
		nodes = (NetworkAgent) server.getService("networkagent");
		Iterator<Node> it = nodes.getValues(true);
		while (it.hasNext()) {
			entry = it.next().getEntry();
			try {
				frame.insertNode(entry.getAddress().getHost() + ":"
						+ entry.getAddress().getPort());
			} catch (NullPointerException e) {
			}
		}
		frame.init(server.getHost() + ":"
				+ Integer.toString(server.getPort()));
	}

	public void cleanup() {
	}

	public void error(String uri, String msg) {
		Address address = new Address(uri);
		String text = "Error pinging " + address.getURL() + " reason " + msg;
		frame.appendStatus(text);
	}

	public void ping() {
		Message message = null;
		try {
			String text = "Ping from: " + msg.getAddress().getURL();
			frame.appendStatus(text);
			message = Message.createReply(msg);
			message.setMethod("pong");
			postMessage(message);
		} catch (Exception e) {
			frame.appendStatus("Node " + message.getAddress().getURI()
					+ " not reachable during ping reply.");
		}
	}

	public void pong() {
		try {
			String text = null;
			long pingtime = System.currentTimeMillis()
					- pingMap.get(msg.getReplyTo().getURL()).longValue();
			text = "Pong from: " + msg.getURL() + " milliseconds: "
					+ Long.toString(pingtime);
			frame.appendStatus(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPing(String to) {
		Message message = null;
		pingMap.clear();

		try {
			if (to.startsWith("http://") == false)
				to = "http://" + to;
			message = new Message(to + "/Console/ping", server);
			message.getReplyTo().setProcessor("Console");
			pingMap.put(message.getURL(), new Long(System.currentTimeMillis()));
			/*
			 * Object args[] = new Object[1]; String data[] = new String[3];
			 * data[0] = "String 1"; data[1] = "String 2"; data[2] = "String 3";
			 * args[0] = data;
			 */
			postMessage(message);
		} catch (Exception e) {
			frame.appendStatus("Node " + message.getAddress().getURI()
					+ " not reachable during ping.");
		}
	}
}