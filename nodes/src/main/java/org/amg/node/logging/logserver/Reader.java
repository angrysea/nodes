package org.amg.node.logging.logserver;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

import org.amg.node.logging.loggerutils.BroadcastMessage;
import org.amg.node.logging.loggerutils.LogEntry;
import org.amg.node.logging.loggerutils.ServerMessage;

public class Reader extends Thread {
	public static int numberOfClient = 0;

	private List<BroadcastMessage> broadcastList;
	@SuppressWarnings("unused")
	private int clientNumber;
	private LogServer logServer;
	private ObjectInputStream ois;
	private Socket socket;

	public Reader(LogServer logServer, List<BroadcastMessage> broadcastList, Socket socket) {
		this.clientNumber = ++numberOfClient;
		this.socket = socket;
		this.logServer = logServer;
		this.broadcastList = broadcastList;
	}

	public void run() {
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			Object o = ois.readObject();

			if (o.getClass().getName().endsWith("LogEntry")) {
				LogEntry le = (LogEntry) o;
				logServer.writeToLog(le);
				broadcastLogEntry(le);
			} else if (o.getClass().getName().endsWith("BroadcastMessage")) {
				logServer.updateBroadcastList((BroadcastMessage) o);
			} else if (o.getClass().getName().endsWith("ServerMessage")) {
				logServer.processServerMessage((ServerMessage) o);
			}
			ois.close();
		} catch (Exception se) {
			System.err.println(se);
		}
		logServer.RemoveReader(this);
	}

	private void broadcastLogEntry(LogEntry logEntry) {
		final Broadcast br = new Broadcast(logEntry, broadcastList);
		br.start();

		try {
			br.join();
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
}
