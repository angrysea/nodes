package org.amg.node.socket;

import org.amg.node.serverutils.CachedThread;

public class CallbackSocketServer extends HttpSocketServer {

	private CallbackSocketListener listener = null;

	protected CallbackSocketServer() {
		super();
	}

	public void setSocketListener(CallbackSocketListener listener) {
		this.listener = listener;
	}

	protected SocketState addSocket() {

		CachedThread t = threadcache.getThread(true);
		CallbackSocket socket = new CallbackSocket(listener, type, server,
				this, t);
		SocketState state = socket.getState();
		socketList.add(state);
		state.setStatus(SocketState.FREE);
		freeList.add(state);

		return state;
	}
}
