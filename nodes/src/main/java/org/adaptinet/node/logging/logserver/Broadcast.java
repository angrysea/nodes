package org.adaptinet.node.logging.logserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.adaptinet.node.logging.loggerutils.BroadcastMessage;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class Broadcast extends Thread {
	private LogEntry logEntry;
	private List<BroadcastMessage> broadcastList;

	public Broadcast(LogEntry le, List<BroadcastMessage> bList) {
		logEntry = le;
		broadcastList = bList;
	}
	
	public void run() {
		broadcastList.stream()
					 .filter(e -> e.getFacility() == logEntry.facility)
					 .filter(e -> e.getSeverity() == logEntry.severity)
					 .forEach(e -> broadcast_message(e));
	}

	private void broadcast_message(BroadcastMessage br) {
		try {
			final Socket socket = new Socket(br.getReturnHost(), br.getReturnPort());
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(logEntry);
			objectOutputStream.flush();
			socket.close();
		} catch (UnknownHostException uh) {
			broadcastList.remove(br);
		} catch (IOException ioe) {
			broadcastList.remove(br);
		}
	}
}
