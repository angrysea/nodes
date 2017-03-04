package org.adaptinet.node.logging.logserver;

public class ServerState extends java.lang.Object implements java.io.Serializable {

	private int m_iLastMsgHandle = -1;
	private boolean m_bCleanShutdown = false;

	public ServerState(int iLastMsg, boolean bCleanShutdown) {
		m_iLastMsgHandle = iLastMsg;
		m_bCleanShutdown = bCleanShutdown;
	}

	public ServerState() {
		m_iLastMsgHandle = -1;
		m_bCleanShutdown = false;
	}

	public int getLastMsgHandle() {
		return m_iLastMsgHandle;
	}

	public boolean getLastServerShutdownState() {
		return m_bCleanShutdown;
	}

	public void setServerShutdownState(boolean bShutdown) {
		m_bCleanShutdown = true;
	}

	public void setLastMsgHandle(int iLastMsgHandle) {
		m_iLastMsgHandle = iLastMsgHandle;
	}

	public void getLastServerShutdownState(boolean bCleanShutdown) {
		m_bCleanShutdown = bCleanShutdown;
		;
	}

	public String toString() {
		return "[ServerState:m_iLastMsgHandle=" + this.m_iLastMsgHandle + ",m_bCleanShutdown=" + this.m_bCleanShutdown
				+ "]";
	}
}
