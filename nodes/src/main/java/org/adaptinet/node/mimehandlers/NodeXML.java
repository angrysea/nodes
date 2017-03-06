package org.adaptinet.node.mimehandlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.adaptinet.node.node.Node;
import org.adaptinet.node.node.NodeEntry;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.server.NetworkAgent;


public class NodeXML {

	static NetworkAgent nodes = null;
	
	static {
		nodes = (NetworkAgent) IServer.getServer().getService(
				"networkagent");
	}

	static public String nodeSave(IServer server, String strRequest)
			throws Exception {
		String nodeURI = null;
		try {

			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			HashMap<String, String> properties = new HashMap<String, String>(5);

			for (int i = 0; i < size; i += 2) {
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.put(token.substring(0, loc), token.substring(
							loc + 1, token.length()));
				}
			}

			NodeEntry re = null;
			nodeURI = properties.get("Address");

			boolean bInsert = false;
			if ((re = nodes.findEntry(nodeURI)) == null) {
				re = new NodeEntry();
				re.setAddress(nodeURI);
				bInsert = true;
			}

			re.setName(properties.get("Name"));
			re.setType(properties.get("Type"));
			if (bInsert == true) {
				nodes.connect(re);
			}
			nodes.setDirty(true);
		} catch (Exception e) {
			throw e;
		}
		return nodeURI;
	}

	static public String nodeDelete(IServer server, String strRequest)
			throws Exception {

		String nodeURI = null;
		try {
			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			HashMap<String, String> properties = new HashMap<String, String>();

			for (int i = 0; i < size; i += 2) {

				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.put(token.substring(0, loc), token.substring(
							loc + 1, token.length()));
				}
			}

			nodeURI = properties.get("Address");
			if (nodeURI == null)
				throw new Exception("Nodeistry entry not found.");

			NodeEntry re = nodes.findEntry(nodeURI);

			if (re != null) {
				nodes.remove(nodeURI);
			} else {
				throw new Exception(nodeURI + " not found in the registry.");
			}
		} catch (Exception e) {
			throw e;
		}
		return nodeURI;
	}

	static public String getEntries(IServer server) {
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\" link=\"#000080\" vlink=\"#000090\">");
			buffer.append("<form method=\"GET\" action=\"nodes/node\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" >");
			buffer.append("<input type=\"hidden\" name=\"entry\" value=\"\"/>");
			buffer.append("<input type=\"Submit\" value=\"Add Node\"/><br><br>");
			buffer.append("<tr valign=\"top\" class=\"header\">");
			buffer.append("  <th>");
			buffer.append("   Name");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("Address");
			buffer.append("  </th>");
			buffer.append("</tr>");

			Iterator<Node> it = nodes.getValues(true);
			int i = 0;
			while (it.hasNext()) {

				i++;
				NodeEntry entry = it.next().getEntry();
				String url = entry.getAddress().getURL();
				if (url != null) {
					buffer.append("<TR");
					if (i % 2 > 0)
						buffer.append(" bgcolor=#ffe4b5 ");
					buffer.append("><TD>");
					buffer.append("<a href=nodes/node?entry=");
					buffer.append(url);
					buffer.append(">");
					if (entry.getName() != null)
						buffer.append(entry.getName());
					buffer.append("</a>&nbsp;");
					buffer.append("</TD><TD>");
					buffer.append(url);
					buffer.append("&nbsp;");
					buffer.append("</TD>");
					buffer.append("</TR>");
				}
			}

			buffer.append("</TABLE></TD></TR></TABLE>");
			buffer.append("</form>");
			buffer.append(MimeHTML_HTTP.footer);
			buffer.append("</BODY>");
			buffer.append("</HTML>");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}

	static public String getEntry(IServer server, String uri) {

		StringBuffer buffer = new StringBuffer();
		try {
			NodeEntry entry = nodes.findEntry(uri);

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"../style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\" >");
			buffer.append("<FORM ACTION=\"/\" METHOD=\"POST\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<INPUT type=\"hidden\" name=\"command\" value=\"nodeSave\"/>");
			buffer.append("<INPUT type=\"submit\" value=\" Save \" />&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\"Delete\" onClick=\"if (confirm('Click OK to delete this entry.')==false) return; command.value='nodedelete';form.submit();\"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\" Help \" onClick=\"window.open('nodehelp.html');\"/>&nbsp;<h1>");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("<h1/><table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Address</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Address");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getAddress().getURI());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Name</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Name");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Type</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Type");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				if (entry.getType() != null)
					buffer.append(entry.getType());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("</TABLE></TD></TR></TABLE>");
			buffer.append("</form>");
			buffer.append(MimeHTML_HTTP.footer);
			buffer.append("</BODY>");
			buffer.append("</HTML>");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}

}