package org.amg.node.logging.logserver;

public class ServerState extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private boolean m_bCleanShutdown = false;
	private int m_iLastMsgHandle = -1;

	public ServerState() {
		m_iLastMsgHandle = -1;
		m_bCleanShutdown = false;
	}

	public ServerState(int iLastMsg, boolean bCleanShutdown) {
		m_iLastMsgHandle = iLastMsg;
		m_bCleanShutdown = bCleanShutdown;
	}

	public int getLastMsgHandle() {
		return m_iLastMsgHandle;
	}

	public boolean getLastServerShutdownState() {
		return m_bCleanShutdown;
	}

	public void getLastServerShutdownState(boolean bCleanShutdown) {
		m_bCleanShutdown = bCleanShutdown;
	}

	public void setLastMsgHandle(int iLastMsgHandle) {
		m_iLastMsgHandle = iLastMsgHandle;
	}

	public void setServerShutdownState(boolean bShutdown) {
		m_bCleanShutdown = true;
	}

	@Override
	public String toString() {
		return "[ServerState:m_iLastMsgHandle=" + this.m_iLastMsgHandle + ",m_bCleanShutdown=" + this.m_bCleanShutdown
				+ "]";
	}
}
