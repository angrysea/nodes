package org.amg.node.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.amg.node.messaging.Address;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.node.Node;
import org.amg.node.node.NodeEntry;
import org.amg.node.node.NodeFile;
import org.amg.node.node.NodeRoot;
import org.amg.node.processoragent.ProcessorFactory;

public class Gateway implements Runnable {

	private static final String CONNECT = "/main/connect";
	private static final int STARTWAIT = 5000;
	private static final int CONNECTEDWAIT = 60000;

	private NodeRoot root = null;
	private String name = null;
	private NodeFile file = null;
	private Thread runner = null;
	private String node = null;
	private boolean bIsDirty = false;
	private boolean bStopped = true;
	private boolean bConnected = false;
	private boolean bHold = false;
	private int nWaitTime = 5000;

	public Gateway(NodeRoot root, String name) {
		this.root = root;
		this.name = name;
	}

	void enter(boolean autoconnect) {
		
		NodeRoot.initRoot();
		file = new NodeFile();
		bStopped = false;
		
		if (autoconnect) {
			try {
				runner = new Thread(this);
				runner.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				root.clear(null);
				file.load(name, root);
				root.computeAverage();
				root.setAlive(true);
				setDirty(true);
				((ProcessorFactory) IServer.getNamedService("processorfactory"))
							.postMessage("nodeUpdate", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void join() throws InterruptedException {
		runner.join();
	}

	public void join(long wait) throws InterruptedException {
		runner.join(wait);
	}

	public boolean isAlive() {
		return runner.isAlive();
	}

	public void run() {

		NodeRoot tmpRoot = null;
		Node child = null;
		boolean bAddedLastKnownNodes = false;

		try {
			tmpRoot = file.open(name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			IServer server = IServer.getServer();
			Object[] args = new Object[3];
			Message message = new Message();
			Address address = new Address();
			message.setAddress(address);

			args[0] = server.getHost() + ":" + server.getPort();
			args[1] = server.getIdentifier();
			args[2] = new Boolean(true);

			ArrayList<Node> list = null;

			// Nothing to connect do a scan instead.
			if (tmpRoot.count() < 1) {
				list = search();
				if (list.size() < 1) {
					return;
				}
			} else {
				list = new ArrayList<Node>(100);
				tmpRoot.flatten(list);
				tmpRoot.clear(null);
			}

			while (!bStopped && !bConnected) {
				if(bHold) {
					try {
						wait(500);
						Thread.interrupted();
					} catch (Exception e) {
						/*
						 */
					}
				}
				else {
					Iterator<Node> it = list.iterator();
					synchronized (this) {
						try {
							while (it.hasNext()) {
								if(bHold) {
									break;
								}
								if (bStopped) {
									return;
								}
								child = it.next();
								if (!child.isAlive() && !root.equals(child)) {
									try {
										address.setURI(child.getEntry().getURL()
												+ CONNECT);
										message.setTimeStamp();
										Messenger.postMessage(message, args);
										wait(200);
										Thread.interrupted();
									} catch (Exception e) {
										/*
										 * If we get an exception here we where
										 * unable to connect so we continue trying
										 * other nodes until one connects us.
										 */
									}
								}
							}
							if(bHold) {
								break;
							}
							if (bStopped || bConnected) {
								return;
							}
							if (bConnected) { 
								nWaitTime = CONNECTEDWAIT;		
								if(bIsDirty) {
									save();
									bIsDirty = false;
								}							
								root.computeAverage();
							}
							else {
								nWaitTime = STARTWAIT;
							}
							
							/**
							 * We where not able to connect so we will add the
							 * nodes we connected to last time and try everybody again.
							 */
							if (!bConnected && !bAddedLastKnownNodes) {
								try {
									bAddedLastKnownNodes = true;
									tmpRoot = file.open(name);
									file.setDefault(true);
									ArrayList<Node> templist = new ArrayList<Node>(100);
									tmpRoot.flatten(list);
									list.addAll(templist);
								} catch (Exception e) {
									/* No default node file to process. */
									continue;
								}
							}
						} catch (Exception e) {
						}
					}
	
					try {
						Thread.sleep(nWaitTime);
						Thread.interrupted();
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() throws InterruptedException {

		if (!bStopped) {
			if (isAlive()) {
				synchronized (this) {
					notifyAll();
					join();
				}
			}
		}
	}

	public void close() {
		try {
			if(file.isOpen())
				file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() throws Exception {

		if (bConnected) {
			String data = null;
			if (root.count() > 0) {
				synchronized (root) {
					data = root.toString();
				}

				if (name != null && name.length() > 0) {
					if (!file.isDefault()) {
						file.save(data);
					} else {
						NodeFile tmpFile = new NodeFile();
							tmpFile.open(name);
					}
				}
			}
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
				if (bConnected || bStopped) {
					break;
				}
				if (i == me) {
					continue;
				}
				String uri = baseaddr + Integer.toString(i) + ":"
						+ server.getPort();
				address.setURI(uri + "/Console/ping");
				try {
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

	public final NodeRoot getRoot() {
		return root;
	}

	public final void setRoot(NodeRoot root) {
		this.root = root;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

	public final void setHold(boolean bHold) {
		this.bHold = bHold;
	}

	public final NodeFile getFile() {
		return file;
	}

	public final void setFile(NodeFile file) {
		this.file = file;
	}

	public final String getNode() {
		return node;
	}

	public final void setNode(String node) {
		this.node = node;
	}

	public void setDirty(final boolean bIsDirty) {
		this.bIsDirty = bIsDirty;
	}
}
