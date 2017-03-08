package org.amg.node.server;

import java.io.PrintStream;

import org.amg.node.exception.AMGException;
import org.amg.node.exception.ServerException;


public class ServerCommandLine {
	static public String findConfigFile(String[] args) {
		if (args == null) {
			return null;
		}

		for (int ii = 0; ii < args.length; ii++) {
			if (args[ii].equalsIgnoreCase("-config")) {
				return args[++ii];
			}
		}
		return null;
	}

	static public boolean processCommandLine(String[] args,
			IServer server) {
		if (args == null) {
			return false;
		}

		// Re-process the command line normally for any overrides.
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h")
					|| args[i].equalsIgnoreCase("-help")
					|| args[i].equalsIgnoreCase("-?")) {
				usage();
			} else if (args[i].equalsIgnoreCase("-httproot")) {
				server.setHTTPRoot(args[++i]);
			} else if (args[i].equalsIgnoreCase("-classpath")) {
				server.setClasspath(args[++i]);
			} else if (args[i].equalsIgnoreCase("-logpath")) {
				server.setLogPath(args[++i]);
			} else if (args[i].equalsIgnoreCase("-type")) {
				server.setSocketType(args[++i]);
			} else if (args[i].equalsIgnoreCase("-socketserverclass")) {
				server.setSocketServerClass(args[++i]);
			} else if (args[i].equalsIgnoreCase("-registry")) {
				// server.set(args[++i]);
			} else if (args[i].equalsIgnoreCase("-port")) {
				try {
					server.setPort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-adminport")) {
				try {
					server.setAdminPort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-secureport")) {
				try {
					server
							.setSecurePort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-messagecachesize")) {
				try {
					int cacheSize = new Integer(args[++i]).intValue();
					if(cacheSize>500) { 
						server.setMessageCacheSize(cacheSize);
					}
					else {
						ServerException serverex = new ServerException(
								AMGException.SEVERITY_FATAL,
								ServerException.TCV_INVALIDMESSAGECACHESIZE);
						serverex
								.logMessage("invalid message cache size to small [" + 
												args[i] + "] defaulting to 5000");
					}
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMESSAGECACHESIZE);
					serverex
							.logMessage("invalid message cache size [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-maxclients")
					&& (i + 1 < args.length)) {
				try {
					server
							.setMaxClients(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMAXCLIENTS);
					System.out.println("invalid number for the max clients ["
							+ args[i] + "]");
					serverex.logMessage("invalid number for the max clients ["
							+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-connecttimeout")
					&& (i + 1 < args.length)) {
				try {
					server.setConnectionTimeOut(new Integer(args[++i])
							.intValue());
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid number for the connection timeout ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid number for the connection timeout ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-verbose")) {
				try {
					server.setVerboseFlag(true);
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid boolean format for verbose mode ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid boolean format for verbose mode ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-autoconnect")) {
				try {
					server.setAutoConnectFlag(true);
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid boolean format for verbose mode ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid boolean format for verbose mode ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-standalone")) {
				try {
					server.setStandAloneFlag(true);
				} catch (NumberFormatException ex) {
					ServerException serverex = new ServerException(
							AMGException.SEVERITY_FATAL,
							ServerException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid boolean format for verbose mode ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid boolean format for verbose mode ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-host")
					&& (i + 1 < args.length)) {
				server.setHost(args[++i]);
			} else {
				continue;
			}
		}

		return true;
	}

	static final public void usage() {
		PrintStream o = System.out;
		o.println("usage: Server [OPTIONS]");
		o.println("-socketType <type>         : Type of processor handlers(Processor|COMProcessor.");
		o.println("-socketServerClass <type>  : Java class for custom socket server.");
		o.println("-httpcfg <http file>       : Name including path of the http configuration.");
		o.println("-registry <registry file>  : Name including path of the registry file.");
		o.println("-connecttimeout <number>   : Timeout for connection.");
		o.println("-id <identifier>           : Name used to identify server.");
		o.println("-port <number>             : Listen on the given port number.");
		o.println("-adminport <number>        : Listen on the given port number for admin requests.");
		o.println("-host <host>               : Full name of host running the server.");
		o.println("-maxclients <number>       : Maximum number of clients.");
		o.println("-maxconnections <number>   : Maximum number of connections.");
		o.println("-registry <registry file>  : Name including path of registry file.");
		o.println("-autoconnect               : Use adaptive networking protocol.");
		o.println("-standalone                : Start as a stand alone server that does not connect to any network.");
		o.println("-verbose                   : Turn on verbose output.");
		o.println("-classpath <classpath>     : Additional CLASSPATH");
		o.println("-messagecachesize <number> : Size of the message cache to prevent echoing.");
		System.exit(1);
	}
}
