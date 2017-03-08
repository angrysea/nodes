package org.amg.node.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.amg.node.exception.AMGException;
import org.amg.node.exception.ServerException;
import org.amg.node.serverutils.CachedThread;


public class HttpSocketServer extends BaseSocketServer {

	protected HttpSocketServer() {
		super();
	}

	protected ServerSocket createServerSocket() {

		try {
			if (bindAddr != null) {
				return new ServerSocket(port, sockets);
			} else {
				return new ServerSocket(port, sockets, bindAddr);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void start(String identifier) {

		socketThread = new Thread(new Runnable() {

			public void run() {
				while (running == true) {
					Socket ns = null;

					try {
						ns = serverSocket.accept();
						if (running == false)
							break;
						ns.setTcpNoDelay(true);
						handleConnection(ns);
					} catch (IOException ioe) {
						try {
							ns.close();
						} catch (Exception e) {
							new ServerException(
									AMGException.SEVERITY_FATAL, 999)
									.logMessage(e);
						}
					}
				}

				try {
					serverSocket.close();
					serverSocket = null;
				} catch (IOException ioe) {
				}

				try {
					cleanup(true);
				} catch (Exception e) {
				}
			}
		});

		if (identifier != null) {
			socketThread.setName(identifier);
		} else {
			socketThread.setName("Server");
		}
		socketThread.setPriority(Thread.MAX_PRIORITY);
		socketThread.start();
		doMaintenance();
	}

	protected synchronized void handleConnection(Socket socket) {

		try {
			SocketState state = null;
			int i = 0;
			int count = 0;
			// While no free threads keep trying for 1 minute.
			while(i==0 && count < 120) {
				count++;
				i = freeList.size() - 1;
				if (i > 0) {
					state = (SocketState) freeList.remove(i);
				}
				else {
					if (debug) {
						System.out.println("No free connections will retry in .5 seconds.");
					}
					Thread.sleep(500);
				}
			}
			
			if (debug) {
				System.out.println("Connection in slot "
						+ new Integer(state.getId()).toString()
						+ " is connecting.");
			}

			if (state != null) {
				state.setStatus(SocketState.BUSY);
				state.bind(socket);

				if (debug) {
					System.out.println("Connection in slot "
							+ new Integer(state.getId()).toString()
							+ " is connected.");
				}
			} else {
				try {
					socket.close();
					if (debug) {
						System.out.println("Connection in slot "
								+ new Integer(state.getId()).toString()
								+ " failed.");
					}
				} catch (IOException ex) {
					System.out.println(ex);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	final public void shutdown() {

		try {
			running = false;
			Socket unlock = null;
			unlock = new Socket(bindAddr, port);
			unlock.close();
		} catch (Exception e) {
		}
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return serverSocket.getInetAddress();
	}

	protected SocketState addSocket() {

		CachedThread t = threadcache.getThread(true);
		ClientSocket socket = new ClientSocket(type, server, this, t);
		SocketState state = socket.getState();
		socketList.add(state);

		state.setStatus(SocketState.FREE);
		freeList.add(state);

		return state;
	}
}
