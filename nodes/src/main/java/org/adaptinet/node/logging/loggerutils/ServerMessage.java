package org.adaptinet.node.logging.loggerutils;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.adaptinet.node.exception.LoggerException;

public class ServerMessage implements Serializable {
	private static final long serialVersionUID = 1656426725126934239L;
	int m_iMessage = -1;
	int m_iMessageID = -1;
	int m_iPort = 5050;
	String m_strHost = new String("localhost");
	public static final int MIN_LOGGER_SERVER_MSG = 9000;
	public static final int MAX_LOGGER_SERVER_MSG = 9000;
	public static final int LOGGER_SERVER_SHUTDOWN = 9000;

	public ServerMessage(int iMessage, int port, String strHost) throws LoggerException {
		try {
			init(iMessage, port, strHost);
		} catch (LoggerException e) {
			throw e;
		}
	}

	public ServerMessage(int iMessage) throws LoggerException {
		try {
			init(iMessage, m_iPort, m_strHost);
		} catch (LoggerException e) {
			throw e;
		}
	}

	private void init(int iMessage, int port, String strHost) throws LoggerException {
		if (iMessage >= MIN_LOGGER_SERVER_MSG && iMessage <= MAX_LOGGER_SERVER_MSG) {
			m_iMessage = iMessage;
			m_iPort = port;
			m_strHost = strHost;
		} else {
			throw new LoggerException(1, 1, "Invalid Server Message");
		}
	}

	public int GetServerMessage() {
		return m_iMessage;
	}

	public void send(int iPort, String sHost, int iMessage) throws LoggerException {

		try {
			init(iMessage, iPort, sHost);
		} catch (LoggerException e) {
			throw e;
		}

		try {
			sendMsg();
		} catch (LoggerException e) {
			throw e;
		}
	}

	public void sendMsg() throws LoggerException {

		try {
			final Socket socket = new Socket(m_strHost, m_iPort);
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(this);
			objectOutputStream.flush();
			objectOutputStream.close();
			socket.close();
		} catch (Exception e) {
			throw new LoggerException(1, 1, "Socket Error [" + e.getMessage() + "]");
		}
	}

}
