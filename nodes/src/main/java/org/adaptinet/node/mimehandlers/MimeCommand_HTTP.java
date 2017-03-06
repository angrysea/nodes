package org.adaptinet.node.mimehandlers;

import java.util.StringTokenizer;

import org.adaptinet.node.http.HTTP;
import org.adaptinet.node.http.Request;
import org.adaptinet.node.server.IServer;

import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;


public class MimeCommand_HTTP implements MimeBase {
	public MimeCommand_HTTP() {
	}

	public void init(String u, IServer s) {
	}

	public String getContentType() {
		return null;
	}

	public ByteArrayOutputStream process(IServer server,
			Request request) {
		
		String strCommand = null;
		String strRequest = request.getRequest();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			boolean bAdminPort = (request.getPort() == server
					.getAdminPort());
			status = isAuthorized(server, request.getUsername(), request
					.getPassword());
			int begin = strRequest.indexOf("=");
			int end = strRequest.indexOf("&");
			if (end == -1)
				end = strRequest.length();

			if (begin != -1)
				strCommand = strRequest.substring(begin + 1, end);
			else
				strCommand = strRequest;

			strCommand = strCommand.toLowerCase();

			status = 200;
			if (strCommand.startsWith("serverconfig")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					ServerConfiguration.setConfiguration(server,
							strRequest);
					out.write(MimeHTML_HTTP.getConfiguration(server)
							.getBytes());
				} catch (Exception e) {
					// status = 500;
					try {
						out.write("<H3>Error retrieving configuration settings</H3>"
										.getBytes());
					} catch (IOException ioe) {
					}
				}
			} else if (strCommand.equalsIgnoreCase("nodesave")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String nodeName = NodeXML.nodeSave(server, strRequest);
					try {
						out.write(("<H3>Successful save of " + nodeName + "</H3>")
										.getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating node entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.startsWith("nodedelete")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String nodeName = NodeXML.nodeDelete(server,
							strRequest);
					try {
						out.write(("<H3>Successful delete of " + nodeName + "</H3>")
										.getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating node entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.equalsIgnoreCase("processorsave")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String processorName = ProcessorXML.processorSave(server,
							strRequest);
					try {
						out.write(("<H3>Successful update of "
									+ processorName + "</H3>").getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating processor entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.equalsIgnoreCase("processordelete")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String processorName = ProcessorXML.processorDelete(server,
							strRequest);
					try {
						out.write(("<H3>Successful delete of "
										+ processorName + "</H3>").getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating processor entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.startsWith("shutdown")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				serverInterface = server;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						try {
							MimeCommand_HTTP.serverInterface.shutdown();
						} catch (Exception e) {
						}
					}
				}).start();
				try {
					out.write(("<H3>" + server.getHost() + ":"
									+ server.getPort() + " shutting down" + "</H3>")
									.getBytes());
				} catch (IOException e) {
					status = 500;
				}
			} else if (strCommand.startsWith("restart")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				serverInterface = server;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						try {
							MimeCommand_HTTP.serverInterface.restart();
						} catch (Exception e) {
						}
					}
				}).start();

				try {
					out.write(("<H3>" + server.getHost() + ":"
							+ server.getPort() + " restarting" + "</H3>")
							.getBytes());
				} catch (IOException e) {
					status = 500;
				}
			} else if (strCommand.startsWith("killrequest")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
				int size = tokenizer.countTokens() * 2;
				String token = null;
				Properties properties = new Properties();
				for (int i = 0; i < size; i += 2) {
					if (tokenizer.hasMoreTokens()) {
						token = tokenizer.nextToken();
						int loc = token.indexOf('=');
						if (token.endsWith("Submit")
								|| token.endsWith("killrequest"))
							continue;
						properties.setProperty(token.substring(0, loc), token
								.substring(loc + 1, token.length()));
					}
				}
				String id = properties.getProperty("SYSTEMID");
				String name = properties.getProperty("NAME");
				String force = properties.getProperty("FORCE");
				boolean b = false;
				if (force != null) {
					b = Boolean.getBoolean(force);
				}
				short s = Short.parseShort(id);
				if (server.killRequest(s, b)) {
					try {
						out.write(("<H3>" + "Request " + name
								+ " Successfully Stopped on "
								+ server.getHost() + ":"
								+ server.getPort() + "</H3>").getBytes());
					} catch (IOException e) {
						status = 500;
					}
				} else {
					try {
						out.write(("<H3>" + "Unable to stop Request " + name
								+ " on " + server.getHost() + ":"
								+ server.getPort() + "</H3>").getBytes());
					} catch (IOException e) {
						status = 500;
					}
				}
			} else {
				String token = null;
				MimeBase mimeXML = (MimeBase) Class.forName(
						"org.adaptinet.sdk.http.MimeXML").newInstance();
				StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");

				StringBuffer buffer = new StringBuffer(
						"<?xml version=\"1.0\"?>");
				buffer.append("<");
				buffer.append(strCommand);
				buffer.append(">");
				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int i = token.indexOf('=');
					int l = token.length();
					if (i > 0) {
						buffer.append("<");
						buffer.append(token.substring(0, i - 1));
						buffer.append(">");
						buffer.append(token.substring(i + 1, l));
						buffer.append("</");
						buffer.append(token.substring(0, i - 1));
						buffer.append(">");
					}
				}

				buffer.append("</");
				buffer.append(strCommand);
				buffer.append(">");
				request.putRequest(buffer.toString().getBytes(), "", null);
				return mimeXML.process(server, request);
			}
		} catch (Exception c360e) {
			c360e.printStackTrace();
		}
		return out;
	}

	public int getStatus() {
		return status;
	}

	private int isAuthorized(IServer s, String username, String password) {
		try {
			String urlBase = s.getHTTPRoot();
			if (urlBase == null || urlBase.equals("."))
				urlBase = System.getProperty("user.dir", "");
			if (!urlBase.endsWith(File.separator))
				urlBase += File.separator;

			String authfile = urlBase + ".xbpasswd";
			File file = new File(authfile);
			if (!file.isFile())
				return HTTP.OK;

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String userpass = br.readLine();
			br.close();

			StringTokenizer st = new StringTokenizer(userpass, ":");
			String user = st.nextToken();
			String pass = st.nextToken();

			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest = new String(md.digest(password.getBytes()));

			if (user.equals(username) && pass.equals(digest))
				return HTTP.OK;
		} catch (IOException ioe) {
		} catch (NullPointerException npe) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HTTP.UNAUTHORIZED;
	}

	private int status = HTTP.UNAUTHORIZED;

	static private IServer serverInterface = null;
}
