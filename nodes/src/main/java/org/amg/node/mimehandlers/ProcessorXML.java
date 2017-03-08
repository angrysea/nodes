package org.amg.node.mimehandlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.amg.node.registry.ProcessorEntry;
import org.amg.node.registry.ProcessorFile;
import org.amg.node.server.IServer;


public class ProcessorXML {

	private static ProcessorFile processorFile = null;

	static {
		processorFile = (ProcessorFile) IServer.getServer().getService(
				"ProcessorFile");
	}

	static public String processorSave(IServer server, String strRequest)
			throws Exception {

		String processorName = null;
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

			processorName = properties.get("Name");
			ProcessorEntry processor = processorFile.findEntry(processorName);
			boolean bInsert = false;
			if (processor == null) {
				processor = new ProcessorEntry();
				processor.setName(processorName);
				bInsert = true;
			}

			processor.setDescription(properties.get("Description"));
			processor.setType(properties.get("Type"));
			processor.setClasspath(properties.get("Classpath"));
			processor.setPreload(properties.get("Preload"));

			if (bInsert == true)
				processorFile.insert(processor);
			processorFile.setDirty(true);
		} catch (Exception e) {
			throw e;
		}
		return processorName;
	}

	static public String processorDelete(IServer server,
			String strRequest) throws Exception {
		
		String processorName = null;
		try {

			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			Properties properties = new Properties();

			for (int i = 0; i < size; i += 2) {
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.setProperty(token.substring(0, loc), token
							.substring(loc + 1, token.length()));
				}
			}
			processorName = properties.getProperty("HANDLERNAME", null);
			if (processorName == null)
				throw new Exception("Processor entry not found.");

			ProcessorEntry re = processorFile.findEntry(processorName);

			if (re != null) {
				processorFile.remove(re);
			} else {
				throw new Exception(processorName + " not found in the registry.");
			}
		} catch (Exception e) {
			throw e;
		}
		return processorName;
	}

	static public String getEntries(IServer server) {

		StringBuffer buffer = new StringBuffer();
		try {

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer
					.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"style.css\">");
			buffer.append("</head>");
			buffer
					.append("<BODY bgcolor=\"white\" link=\"#000080\" vlink=\"#000090\">");
			buffer.append("<form method=\"GET\" action=\"processors/processor\">");
			buffer
					.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"images/empty.gif\" width=30 border=0></TD><td>");
			buffer
					.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" >");
			buffer.append("<input type=\"hidden\" name=\"entry\" value=\"\"/>");
			buffer
					.append("<input type=\"Submit\" value=\"Add Processor\"/><br><br>");

			buffer.append("<tr valign=\"top\" class=\"header\">");
			buffer.append("  <th>");
			buffer.append("   Name");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("Class");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("    Description");
			buffer.append("  </th>");
			buffer.append("</tr>");

			Iterator<ProcessorEntry> it = processorFile.getValues();
			int i = 0;
			while (it.hasNext()) {

				i++;
				ProcessorEntry entry = it.next();
				buffer.append("<TR class = \"text\"");
				if (i % 2 > 0)
					buffer.append(" bgcolor=#ffe4b5 ");
				buffer.append("><TD>");
				buffer.append("<a href=processors/processor?entry=");
				buffer.append(entry.getName());
				buffer.append(">");
				buffer.append(entry.getName());
				buffer.append("</a>&nbsp;");
				buffer.append("</TD><TD>");
				buffer.append(entry.getType());
				buffer.append("</TD><TD>");
				buffer.append(entry.getDescription());
				buffer.append("</TD></TR>");
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

	static public String getEntry(IServer server, String name) {
		StringBuffer buffer = new StringBuffer();
		try {

			ProcessorEntry entry = processorFile.findEntry(name);

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"../style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\">");
			buffer.append("<FORM ACTION=\"/\" METHOD=\"POST\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<INPUT type=\"hidden\" name=\"command\" value=\"processorsave\"/>");
			buffer.append("<INPUT type=\"submit\" value=\" Save \"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\"Delete\" onClick=\"if (confirm('Click OK to delete this entry.')==false) return; command.value='processordelete';form.submit();\"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\" Help \" onClick=\"window.open('processorhelp.html');\"/>&nbsp;<h1>");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("<h1/><table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");

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
			buffer.append("<TD><font class=\"header\">Description</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Description");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getDescription());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Class</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Type");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getType());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Classpath</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Classpath");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getClasspath());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Preload</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Preload");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getPreload());
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