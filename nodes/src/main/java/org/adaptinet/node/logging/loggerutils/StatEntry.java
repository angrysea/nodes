package org.adaptinet.node.logging.loggerutils;

import java.io.PrintStream;

import org.adaptinet.node.server.IServer;

public class StatEntry {
	private long endTime = 0;
	private String handlerName = "";
	private PrintStream ps = null;
	private long startTime = 0;
	private String state = "Completed";
	private String xmlIn = "";
	private String xmlOut = "";

	public StatEntry(String handler) {
		this.handlerName = handler;
		this.startTime = System.currentTimeMillis();
	}

	public void log() {
		try {
			endTime = System.currentTimeMillis();
			String xml = toXML();
			if (ps == null)
				ps = IServer.getServer().getConsoleStream();

			ps.println(xml);

			if (IServer.bVerbose)
				println();
		} catch (Exception e) {
			System.err.println("Exception during StatEntry.log()=" + e);
		}
	}

	public void println() {
		System.out.println();
		System.out.println("** Handler: " + handlerName + " started=" + Long.toString(startTime) + " ended="
				+ Long.toString(endTime));
		// System.out.println(" xmlRequest="+xmlIn);
		// System.out.println(" xmlResponse="+xmlOut);
		System.out.println("   state=" + state);
		System.out.println();
	}

	public void setState(Exception e) {
		if (e != null)
			state = e.getMessage();
	}

	public void setXmlIn(String xml) {
		xmlIn = xml;
		if (xmlIn.startsWith("<?xml ")) {
			int pastpi = xmlIn.indexOf("?>");
			xmlIn = xmlIn.substring(pastpi + 2);
		}
	}

	public void setXmlOut(String xml) {
		xmlOut = xml;
		if (xmlOut.startsWith("<?xml ")) {
			int pastpi = xmlOut.indexOf("?>");
			xmlOut = xmlOut.substring(pastpi + 2);
		}
	}

	private String toXML() {
		StringBuffer strBuff = new StringBuffer(512);
		strBuff.append("<Stat>");
		strBuff.append("<TimeStamp endtime=\"" + Long.toString(endTime) + "\" starttime=\"" + Long.toString(startTime)
				+ "\" />");
		strBuff.append("<Handler name=\"" + handlerName + "\" />");
		// strBuff.append("<XMLIn>" + xmlIn + "</XMLIn>");
		// strBuff.append("<XMLOut>" + xmlOut + "</XMLOut>");
		strBuff.append("<State>" + state + "</State>");
		strBuff.append("</Stat>");
		String retval = strBuff.toString();
		return retval.replace('\n', ' '); // Newlines not allowed in xml
	}
}