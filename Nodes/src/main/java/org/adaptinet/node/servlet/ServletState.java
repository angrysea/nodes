package org.adaptinet.node.servlet;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.server.IServer;

public class ServletState {
	public ServletState(IServer requestBroker) {
		server = requestBroker;
		id = nextId();
	}

	final public String getName() {
		return server.getIdentifier();
	}

	final public void startTimer() {
		start = System.currentTimeMillis();
	}

	final public long getStartTime() {
		return start;
	}

	final public boolean kill(boolean now) {
		try {
			server.shutdown();
		} catch (AdaptinetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	final public IServer getServletBroker() {
		return server;
	}

	final public void reset() {
		try {
			server.restart();
		} catch (AdaptinetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final public int getStatus() {
		return status;
	}

	final public void setStatus(int s) {
		status = s;
	}

	final public int getId() {
		return id;
	}

	final public int getTimeOut() {
		return timeout;
	}

	final public void setTimeOut(int i) {
		timeout = i;
	}

	static final synchronized int nextId() {
		return nextid++;
	}

	public boolean isTimedOut() {
		boolean ret = false;
		if (timeout > 0) {
			if (status == TIMEDOUT) {
				ret = true;
			} else {
				long current = System.currentTimeMillis();
				if (current > start + timeout) {
					ret = true;
				}
			}
		}
		return ret;
	}

	static final int IDLE = 0;
	static final int BUSY = 1;
	static final int FREE = 2;
	static final int KILL = 3;
	static final int FIN = 4;
	static final int TIMEDOUT = 5;
	static final int SIGNALED = 6;
	static final int INTERRUPTED = 7;

	private static int nextid = 0;
	private IServer server = null;
	private int status = IDLE;
	private int id = 0;
	private long start = 0;
	private int timeout = -1;
}
