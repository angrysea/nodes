package org.adaptinet.node.logging.logserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import org.adaptinet.node.logging.loggerutils.BroadcastMessage;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class Broadcast extends Thread
{
	public Broadcast(LogEntry le, Vector bList)
	{
		logEntry = le;
		broadcastList = bList;
	}

	public void run()
	{
		Enumeration vEnum = broadcastList.elements();
		while (vEnum.hasMoreElements())
		{
			BroadcastMessage br = (BroadcastMessage) vEnum.nextElement();
			if (br.getFacility() == logEntry.facility &&
				br.getSeverity() == logEntry.severity)
			{
				try
				{
					Socket socket = new Socket( br.getReturnHost(), br.getReturnPort());
					ObjectOutputStream objectOutputStream = new ObjectOutputStream( socket.getOutputStream());
					objectOutputStream.writeObject( logEntry );
					objectOutputStream.flush();
				}
				catch (UnknownHostException uh)
				{
					broadcastList.removeElement(br);
				}
				catch (IOException ioe)
				{
					broadcastList.removeElement(br);
				}
			}
		}
	}

	private LogEntry logEntry;
	private Vector broadcastList;
}


