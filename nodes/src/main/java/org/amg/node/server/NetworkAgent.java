package org.amg.node.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.amg.node.exception.AMGException;
import org.amg.node.exception.BaseException;
import org.amg.node.messaging.Address;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.node.Node;
import org.amg.node.node.NodeEntry;
import org.amg.node.node.NodeRoot;
import org.amg.node.processoragent.ProcessorFactory;

public final class NetworkAgent implements IResetEvent {

	private String networkAgentName;
	private MaintenanceThread maintenance = null;
	private CacheThread cache = null;
	private NodeRoot root = null;
	private String connectType = null;
	private boolean bConnected = false;
	private boolean autoconnect = false;
	private Gateway gateway = null;

	@SuppressWarnings("unused")
	private static int RETRIES = 3;

	public NetworkAgent(final IServer server, final String name, final boolean autoconnect, final String connectType,
			final int max, final int levels) {

		this.autoconnect = autoconnect;
		this.setConnectType(connectType);
		this.root = new NodeRoot(server);
		setConnected(false);
		Node.setLevels(levels);
		Node.setMax(max);

		gateway = new Gateway(root, name);

		maintenance = new MaintenanceThread(root, this);
		cache = new CacheThread(root);

		maintenance.start();
		cache.start();
	}

	public void start() {

		try {
			gateway.enter(autoconnect);
			if (!autoconnect) {
				this.setConnected(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean loadNodes(final String xml) throws Exception {

		boolean bRet = false;
		try {
			root.clear(null);
			root.parseNodes(xml);
			root.computeAverage();
			root.setAlive(true);
			bRet = true;
		} catch (Exception x) {
			throw x;
		}
		return bRet;
	}

	public void closeFile() {

		try {
			if (gateway != null) {
				gateway.close();
			}
			if (cache != null) {
				cache.stop();
			}
			if (maintenance != null) {
				maintenance.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public NodeEntry findEntry(final String name) {

		try {
			Node node = null;
			node = root.find(name);
			if (node != null) {
				return node.getEntry();
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Here are the steps to making a connection into the network
	 * 
	 * 1) A new node sends a connect request to its known nodes. once a node is
	 * found that can service this request it will process the request.
	 * 
	 * 2) A connected message is sent back to the the node with the topology of
	 * the network segment.
	 * 
	 * 3) All nodes in the network are sent a insert message to tell them to
	 * insert the new node into their copy of the topology.
	 */
	public void connect(final NodeEntry entry) {
		connect(entry, false);
	}

	public synchronized void connect(final NodeEntry entry, final boolean bAuto) {

		Node node = null;
		try {
			if (autoconnect == false && bAuto == false) {
				root.insert(entry);
			} else {
				boolean firstconnect = bConnected;

				Message message = new Message(entry.getURL(), IServer.getServer());
				message.getAddress().setProcessor("main");

				Object[] args = new Object[2];
				args[0] = root.getAddress().getURL();

				if ((node = root.isConnected(entry)) != null && node.isAlive()) {
					message.getAddress().setMethod("isconnected");
					args[0] = root.getAddress().getURL();
					args[1] = node.toEscapedString();
					Messenger.postMessage(message, args);
				} else if ((node = root.doConnect(entry)) != null) {
					if (gateway != null) {
						gateway.setHold(true);
					}
					entry.setAlive(true);
					message.getAddress().setMethod("connected");
					args[1] = root.toEscapedString();
					Messenger.postMessage(message, args);

					// We may not be connected either so let set it now
					if (!bConnected) {
						setConnected(true);
					}
					gateway.setDirty(true);
					nodeUpdate();
					if (gateway != null) {
						gateway.setHold(false);
					}
				} else {
					/**
					 * This a timing issue two nodes send a connect message too
					 * each other. One will get inserted first this will allow
					 * the one with the connected message to process.
					 */
					if (firstconnect == false) {
						setConnected(false);
					}
				}
			}
			// System.out.println("Connect: " + root.toString());
		} catch (Exception e) {
			if (gateway != null) {
				gateway.setHold(false);
			}
			e.printStackTrace();
		}
	}

	/**
	 * This is step 2 in the connection process I have asked to be connected and
	 * someone has responded back to me with the new topology.
	 */
	public synchronized void connected(final String node, final String xml) {

		if (autoconnect == true) {
			try {
				if (bConnected == true) {
					root.refuseConnection(node);
					return;
				}
				// Wake up the thread so we can load the nodes.
				// System.out.println("Connected Topology: "+xml);
				if (loadNodes(xml)) {
					// System.out.println("Root Topology: "+root.toString());
					root.notifyInserted(node);
					nodeUpdate();
					root.maintenance();
					// root.display(root);
					root.cache();
					if (!bConnected) {
						setConnected(true);
					}
				}
				System.out.println("Connected: " + root.toString());
			} catch (Exception e) {
				e.printStackTrace();
				// Something went wrong so we should try all over again.
				setConnected(false);
				gateway.enter(autoconnect);
			}
		}
	}

	/**
	 * If we are here there may have been some timing issues so we are just
	 * being informed that we are already connected.
	 */
	public synchronized void isconnected(final String node, final String xml) {

		if (autoconnect == true) {
			try {
				// If we are in here there may have been some sort of
				// problem so we just go on like we are getting connecting
				// again.
				connected(node, xml);
			} catch (Exception e) {
				e.printStackTrace();
				// Something went wrong so we should try all over again.
				setConnected(false);
				gateway.enter(autoconnect);
			}
		}
	}

	/**
	 * This is step 3 in the connection process I've been called by the node so
	 * I can update my copy of the topology.
	 */
	public synchronized void insert(final Envelope env) {

		// First see if this is an auto-connect otherwise we ignore this request
		if (autoconnect == true) {

			try {
				Object[] args = env.getBody().getcontentArray();
				if (args.length != 2) {
					return;
				}

				synchronized (root) {
					Node atNode = null;

					// First find the insertion point.
					Address connectAt = new Address((String) args[1]);
					if ((atNode = root.find(connectAt)) == null) {
						// If not found no need to continue,
						System.out.println("Cannot find : " + args[1]);
						return;
					}

					// Check to see if this node already exists in our segment.
					// if it does may have been in the node file so just update.
					Address entry = new Address((String) args[0]);
					NodeEntry pe = null;
					Node pn = atNode.find(entry);
					if (pn == null) {
						pe = new NodeEntry(entry);
						pn = atNode.insert(pe);
						nodeUpdate();
					} else {
						pe = pn.getEntry();
					}

					if (!pe.isAlive()) {
						pe.setAlive(true);
						gateway.setDirty(false);
						nodeUpdate();
						root.broadcastMessage(env.getHeader().getMessage(), args);
						pn.doMaintenance();
						pn.doCache();
					}
					if (!bConnected) {
						setConnected(true);
					}
				}
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This is one of the steps in the repair process we need to update everyone
	 * that knew about the drop-out node with the replacement node.
	 */
	public synchronized void update(final Envelope env) {

		// First see if this is an auto-connect otherwise we ignore these
		// problems
		if (autoconnect == true) {

			try {
				Object[] args = env.getBody().getcontentArray();
				if (args.length < 2) {
					return;
				}

				synchronized (root) {
					Node atNode = null;

					// First find the old node.
					Address connectAt = new Address((String) args[0]);
					if ((atNode = root.find(connectAt)) == null) {
						// If not found no need to continue,
						return;
					}

					Address entry = new Address((String) args[1]);
					// In case we have this node somewhere else we need
					// to get ride of it first.
					root.remove(entry);
					atNode.setEntry(new NodeEntry(entry));
					gateway.setDirty(false);
					nodeUpdate();
					root.broadcastMessage(env.getHeader().getMessage(), args);
				}
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notifies all processors that the nodes have changes.
	 */
	private void nodeUpdate() {
		try {
			gateway.setDirty(true);
			((ProcessorFactory) IServer.getNamedService("processorfactory")).postMessage("nodeUpdate", null);
		} catch (NullPointerException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is called by the server so that the network agent can inform all
	 * nodes it is directly connected too that it is dropping out of the
	 * network.
	 */
	public void disconnect() {
		if (autoconnect) {
			try {
				((ProcessorFactory) IServer.getNamedService("processorfactory")).cleanupProcessor();
				synchronized (root) {
					root.doDisconnect();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * This is received from a message post to inform this node that an adjacent
	 * node has dropped out of the node network and we are stepping in
	 */
	public void replace(final String address, final String xml) {
		if (autoconnect) {
			try {
				synchronized (root) {
					System.out.println("replace " + address + " nodes " + xml);
					root.doReplace(address, xml);
					if (root.count() < 1) {
						reconnect();
					} else {
						nodeUpdate();
					}
				}
			} catch (Exception e) {
				// For what ever reason we failed to
				// replace lets inform all our nodes and try a
				disconnected();
			}
		}
	}

	/**
	 * This is received from a message post to inform this node that an adjacent
	 * node has dropped out of the node network. If we received this message
	 * there was a problem and we will need to reconnect to the network to
	 * maintain integrity all of our nodes should reconnect.
	 */
	public void disconnected() {

		if (autoconnect == true) {
			// Remove this node so we don't send any more messages
			/**
			 * We have to try to rejoin the network. The way too do this is look
			 * at the back up node file because obviously the current one has a
			 * problem.
			 */
			try {
				synchronized (root) {
					root.doDisconnect();
				}
				reconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reconnect() throws Exception {
		try {
			if (autoconnect) {
				gateway.enter(autoconnect);
				nodeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(final String node) {
		try {
			synchronized (root) {
				if (root.remove(node)) {
					// If we removed this node we should continue to notify down
					// the line that we have done so.
					Object[] args = new Object[1];
					args[0] = new String(node);
					root.notifyRemoved(args);
					nodeUpdate();
					if (root.count() < 1) {
						reconnect();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ArrayList<Node> search() {
		ArrayList<Node> list = new ArrayList<Node>(100);

		IServer server = IServer.getServer();
		String baseaddr = server.getHost().substring(0, 12);
		int me = Integer.parseInt(server.getHost().substring(12));

		Message message = new Message();
		Address address = new Address();
		message.setAddress(address);

		try {
			for (int i = 0; i < 255; i++) {
				if (bConnected == true) {
					break;
				}
				if (i == me) {
					continue;
				}
				String uri = baseaddr + Integer.toString(i) + ":" + server.getPort();
				address.setURI(uri + "/Console/ping");
				try {
					System.out.println("Attempting to contact: " + uri);
					if (Messenger.testConnection(message)) {
						list.add(new Node(new NodeEntry(uri)));
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}

		return list;
	}

	public String getName() {
		return networkAgentName;
	}

	public void broadcastMessage(final Envelope env) throws AMGException {

		if (bConnected == false) {
			AMGException amgex = new AMGException(BaseException.SEVERITY_ERROR,
					BaseException.GEN_MESSAGE);
			amgex.logMessage("Can not broadcast message while not connected");
			throw amgex;
		}
		root.broadcastMessage(env.getHeader().getMessage(), env.getBody().getcontentArray());
	}

	public void broadcastMessage(final Message message, final Object[] args) throws AMGException {

		if (bConnected == false) {
			AMGException amgex = new AMGException(BaseException.SEVERITY_ERROR,
					BaseException.GEN_MESSAGE);
			amgex.logMessage("Can not broadcast message while not connected");
			throw amgex;
		}
		root.broadcastMessage(message, args);
	}

	public Iterator<Node> getValues(final boolean all) {
		ArrayList<Node> list = new ArrayList<Node>(100);
		if (all == true) {
			root.flatten(list);
		} else {
			root.getAdjacent(list);
		}
		return list.iterator();
	}

	public ArrayList<Node> getNodes() {
		ArrayList<Node> list = new ArrayList<Node>(100);
		root.flatten(list);
		return list;
	}

	public static void setRetries(final int retries) {
		NetworkAgent.RETRIES = retries;
	}

	private void setConnected(boolean b) {
		bConnected = b;
		if (gateway != null) {
			gateway.setConnected(bConnected);
		}
		if (cache != null) {
			cache.setConnected(bConnected);
		}
		if (maintenance != null) {
			maintenance.setConnected(bConnected);
		}
	}

	public void setDirty(final boolean bIsDirty) {
		gateway.setDirty(bIsDirty);
	}

	public void reset() {
		try {
			reconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getConnectType() {
		return connectType;
	}

	public void setConnectType(String connectType) {
		this.connectType = connectType;
	}
}
