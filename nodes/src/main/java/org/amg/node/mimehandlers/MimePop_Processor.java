package org.amg.node.mimehandlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.amg.node.exception.AMGException;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.MessageParser;
import org.amg.node.processoragent.ProcessorAgent;
import org.amg.node.processoragent.ProcessorFactory;
import org.amg.node.server.IServer;
import org.amg.node.xmltools.parser.InputSource;
import org.amg.node.xmltools.parser.XMLReader;


public class MimePop_Processor {

	static private String servername = null;

	private String username = null;

	private String password = null;

	private int polltime = 600000;

	private int port = 110;

	private boolean delete = false;

	private boolean securelogin = false;

	private Socket sock;

	private IServer server = IServer.getServer();

	private Thread mailThread = null;

	private boolean bContinue = true;

	private ProcessorAgent processor = null;

	private boolean debug = true;

	private BufferedReader in;

	private PrintStream out;

	public static MimePop_Processor startMailReader() {

		MimePop_Processor pop = null;
		try {
			Properties properties = new Properties();
			File file = IServer.findFile("mail.properties");

			if (properties.getProperty("activate", "false").equals("true")) {
				properties.load(new FileInputStream(file));
				pop = new MimePop_Processor();
				pop.setUsername(properties.getProperty("Username", "scott"));
				pop.setPassword(properties.getProperty("password", "tiger"));
				pop.setServer(properties.getProperty("mailserver",
						MimePop_Processor.servername));
				pop.setPolltime(Integer.parseInt(properties.getProperty(
						"polltime", "60000")));
				pop.setPort(Integer.parseInt(properties.getProperty("port",
						"110")));
				pop.setDelete(Boolean.getBoolean(properties.getProperty(
						"delete", "true")));
				pop.setSecurelogin(Boolean.getBoolean(properties.getProperty(
						"securelogin", "false")));
				pop.mailReader();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return pop;
	}

	public MimePop_Processor() {
		MimePop_Processor.servername = "mail.amg.net";
	}

	public MimePop_Processor(String server, int port) {
		MimePop_Processor.servername = server;
		this.port = port;
	}

	public void stop() {
		bContinue = false;
	}

	public void mailReader() {

		try {
			if (mailThread == null) {
				mailThread = new Thread() {
					public void run() {
						try {
							while (bContinue) {
								// Read the mail see what we get
								readMail();

								synchronized (mailThread) {
									try {
										mailThread.wait(polltime);
									} catch (InterruptedException ex) {
										break;
									}
								}
							}
						} catch (Exception e) {
							AMGException ae = new AMGException(
									AMGException.SEVERITY_ERROR,
									AMGException.GEN_MESSAGE);
							ae.logMessage(e);
							e.printStackTrace();
						}
					}
				};
			}
			if (mailThread.isAlive() == false)
				mailThread.start();
		} catch (Exception e) {
		}
	}

	public void readMail() {

		try {
			open();
			if (login() == false)
				return;

			String data = read();
			write("STAT");
			data = read();

			if (data.startsWith("+OK") == false) {
				printDebug("Error:" + data);
				sock.close();
				return;
			}

			int i = data.lastIndexOf(' ');
			String numberMessages = data.substring(4, i);
			printDebug("You have " + numberMessages
					+ " message(s) in your mailbox");

			int n = Integer.parseInt(numberMessages);
			for (int msg = 1; msg <= n; msg++) {
				printDebug("Retreaving message " + msg);
				write("RETR " + msg);
				data = read();
				if (!data.startsWith("+OK")) {
					printDebug("Error: " + data);
					sock.close();
					return;
				}

				i = data.lastIndexOf(' ');

				StringBuffer buffer = new StringBuffer();
				data = in.readLine();

				while (data.compareTo(".") != 0) {

					printDebug(data);
					buffer.append(data);
					data = in.readLine();
				}

				if (delete) {
					write("DELE " + msg);
					read();
				}

				if (buffer.length() > 0)
					process(buffer.toString());
			}

			write("QUIT");
			read();
			close();
		} catch (IOException e) {
			System.err.println("IOException : " + e);
		} catch (Exception e) {
			System.err.println("IOException : " + e);
		}
	}

	private void open() throws Exception {
		try {
			sock = new Socket(servername, port);
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
		} catch (Exception e) {
			printDebug("Error connecting to server - " + server + " Error "
					+ e.getMessage());
			throw e;
		}
	}

	private boolean login() {

		String data = null;
		try {
			if (securelogin) {
				/*
				 * String timeStamp = data.substring(4,data.length());
				 * printDebug(timeStamp); md5 mdc = new md5(timeStamp +
				 * password); printDebug(timeStamp + password); mdc.calc();
				 * write("APOP " + username + " " + mdc); read(); if
				 * (data.startsWith("+OK")) { return true; }
				 */
			} else {
				write("USER " + username);
				if (read().startsWith("+OK")) {
					out.println("PASS " + password);
					data = read();
					if (data.startsWith("+OK")) {
						return true;
					}
				}
			}
			printDebug("Invalid username/password, disconnecting from mail server");
			sock.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void close() {
		try {
			sock.close();
		} catch (Exception e) {
		}
	}

	private void process(String xml) {

		Envelope env = null;

		try {
			XMLReader parser = new XMLReader();
			parser.setContentHandler(new MessageParser());
			env = (Envelope) parser.parse(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			String name = env.getHeader().getMessage().getAddress().getProcessor();
			processor = (ProcessorAgent) server.getAvailableProcessor(name);

			if (processor != null) {
				if (name.equals(ProcessorFactory.MAIN) == true)
					processor.preProcess(ProcessorFactory.MAINCLASS);
				else if (name.equals(ProcessorFactory.MAINTENANCE) == true)
					processor.preProcess(ProcessorFactory.MAINTENANCECLASS);
				else if (name.equals(ProcessorFactory.SERVICE) == true)
					processor.preProcess(ProcessorFactory.SERVICECLASS);

				AMGException exMessage = new AMGException(
						AMGException.SEVERITY_SUCCESS,
						AMGException.GEN_MESSAGE);
				exMessage.logMessage("Processor received Name: "
						+ processor.getName());

				// String type = env.getHeader().getMessage().getAddress()
				// .getType();
				processor.pushMessage(env);
				server.run(processor);
			} else {
				AMGException exMessage = new AMGException(
						AMGException.SEVERITY_ERROR,
						AMGException.GEN_BASE);
				exMessage.logMessage("Unable to load find available processor: ");
			}

			AMGException exMessage = null;
			exMessage = new AMGException(
					AMGException.SEVERITY_SUCCESS,
					AMGException.GEN_MESSAGE);
			exMessage.logMessage("Processor successfully executed Name: "
					+ processor.getName());
		} catch (Exception e) {
			AMGException exMessage = new AMGException(
					AMGException.GEN_MESSAGE,
					AMGException.SEVERITY_SUCCESS);
			exMessage.logMessage("Execution failed reason: " + e.getMessage());
			try {
				out
						.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><processor>unknown</processor><code>1</code><desc>"
								+ e.getMessage()
								+ "</desc><timestamp>"
								+ (new java.util.Date(System
										.currentTimeMillis()).toString()) + "</timestamp></status>")
								.getBytes());
			} catch (IOException ioe) {
			}
		} finally {
			processor = null;
		}
	}

	private void write(String s) throws IOException {
		printDebug("write: " + s);
		out.println(s);
	}

	private String read() throws IOException {
		String s = in.readLine();
		printDebug("read: " + s);
		return s;
	}

	private void printDebug(String s) {
		if (debug) {
			if (s != null) {
				System.out.println(s);
			}
		}
	}

	public Object getObject() {
		return processor;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setServer(String server) {
		MimePop_Processor.servername = server;
	}

	public void setPolltime(int polltime) {
		this.polltime = polltime;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public void setSecurelogin(boolean securelogin) {
		this.securelogin = securelogin;
	}
}
