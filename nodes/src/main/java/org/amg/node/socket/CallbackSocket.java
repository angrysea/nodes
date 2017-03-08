package org.amg.node.socket;

import java.io.DataOutputStream;
import java.io.InputStream;

import org.amg.node.exception.AMGException;
import org.amg.node.server.IServer;
import org.amg.node.serverutils.CachedThread;



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
			AMGException exMessage = new AMGException(
					AMGException.GEN_MESSAGE,
					AMGException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed");
		}
		return interrupted;
	}
}
