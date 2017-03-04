package org.adaptinet.node.logging.logserver;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Vector;

public class Reader extends Thread {
	public Reader(LogServer server, Vector vBdctList, Socket s) {
		numberOfClient++;
		clientNumber = numberOfClient;
		socket = s;
		logServer = server;
		broadcastList = vBdctList;
	}

	/*
	 * public void run() { int iHandle; try { ois = new ObjectInputStream(
	 * socket.getInputStream() ); Object o = ois.readObject();
	 * 
	 * if (o.getClass().getName().endsWith("LogEntry")) { LogEntry le =
	 * (LogEntry) o; logServer.writeToLog(le); broadcastLogEntry(le); } else
	 * if(o.getClass().getName().endsWith("BroadcastMessage")) {
	 * logServer.updateBdcstList( (BroadcastMessage) o ); } else if
	 * (o.getClass().getName().endsWith("ServerMessage")) {
	 * logServer.processServerMsg( (ServerMessage) o); } ois.close(); }
	 * catch(Exception se) { System.err.println(se); }
	 * logServer.RemoveReader(this); }
	 * 
	 * private void broadcastLogEntry(LogEntry logEntry) { Broadcast br = new
	 * Broadcast(logEntry,broadcastList); br.start();
	 * 
	 * try { br.join(); } catch(InterruptedException e) { System.err.println(e);
	 * } }
	 */

	public static int numberOfClient = 0;
	private int clientNumber;
	private LogServer logServer;
	private ObjectInputStream ois;
	private Socket socket;
	private Vector broadcastList;
}
