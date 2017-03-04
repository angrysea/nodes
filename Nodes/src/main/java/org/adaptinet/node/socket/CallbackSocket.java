package org.adaptinet.node.socket;

import java.io.DataOutputStream;
import java.io.InputStream;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.serverutils.CachedThread;



public class CallbackSocket extends ClientSocket {

	private CallbackSocketListener listener = null;

	public CallbackSocket(CallbackSocketListener listener, String type,
			IServer server, BaseSocketServer baseSocketServer,
			CachedThread thread) {
		super(type, server, baseSocketServer, thread);
		this.listener = listener;
	}

	synchronized protected boolean startConnection(InputStream in,
			DataOutputStream out) {

		input = in;
		output = out;

		try {
			StringBuffer b = new StringBuffer();
			while (in.available() > 0) {
				b.append(in.read());
			}

			listener.OnReceive(b.toString());
			input.close();
			input = null;

			output.flush();
			output.close();
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed");
		}
		return interrupted;
	}
}
