package org.adaptinet.node.processoragent;

import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.server.IServer;


abstract public class ProcessorRoot {

	protected IServer server = null;

	protected Message msg = null;

	protected ProcessorAgent agent = null;

	public ProcessorRoot() {
		server = IServer.getServer();
	}

	public abstract void init();

	public abstract void cleanup();

	public abstract Object sendMessage(Message message, Object ... args)
	throws ProcessorException;
	
	public abstract void broadcastMessage(String toUri, Object... args)
			throws ProcessorException;

	public abstract void broadcastMessage(String toUri,
			IServer replyServer, Object... args)
			throws ProcessorException;

	public abstract void postMessage(Message message) throws ProcessorException;

	public abstract void postMessage(Message message, Object... args)
			throws ProcessorException;

	public abstract void localPostMessage(Message message, Object... args)
			throws ProcessorException;

	public abstract void broadcastMessage(Message message, Object... args)
			throws ProcessorException;

	final void setCurrentMessage(Message msg) {
		this.msg = msg;
	}

	final void setAgent(ProcessorAgent agent) {
		this.agent = agent;
	}

	final void setServer(IServer server) {
		this.server = server;
	}

	public void unloadProcessor() {
		IServer.getServer().killRequest(agent.getName(), true);
	}

	public void shutdownServer() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				try {
					server.shutdown();
					System.exit(1);
				} catch (Exception e) {
				}
			}
		}).start();

	}
}
