package org.adaptinet.node.server;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.BaseException;
import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.exception.ServerException;
import org.adaptinet.node.logging.logserver.LogServer;
import org.adaptinet.node.mimehandlers.MimePop_Processor;
import org.adaptinet.node.processoragent.ProcessorAgent;
import org.adaptinet.node.processoragent.ProcessorFactory;
import org.adaptinet.node.registry.ProcessorFile;
import org.adaptinet.node.socket.BaseSocketServer;
import org.adaptinet.node.socket.PropData;

public class Server extends IServer {

	static public final String TRUE = "true";
	static public final String FALSE = "false";

	// Configuration defines
	static protected final String PROP_ROOT_DIR = "root";

	static protected final boolean REDIRECT_OUTPUT_STREAMS = false;

	protected int clientPriority = Thread.NORM_PRIORITY;
	protected boolean bUseProxy = false;
	protected BaseSocketServer socketServer = null;
	protected BaseSocketServer adminSocketServer = null;
	protected BaseSocketServer secureSocketServer = null;
	protected LogServer logger;
	protected ProcessorFactory processorFactory = null;
	protected boolean bRestarting = false;
	protected boolean bFinishing = false;
	protected int nPort = 8082;
	protected int nAdminPort = 0;
	protected int nSecurePort = 0;
	protected String host = "localhost";
	protected String classpath = null;
	protected String identifier = "Server";
	protected URL url = null;
	protected int nMaxClients = 0;
	protected int maxnodes = 10;
	protected int nodelevels = 4;
	protected int nMaxConnections = 0;
	protected boolean verbose = false;
	protected boolean autoconnect = false;
	protected boolean standalone = false;
	protected boolean showconsole = false;
	protected ProcessorFile processorFile = null;
	protected NetworkAgent networkAgent = null;
	protected int timeout = 0;
	protected String fileName = null;
	protected String nodefilename = null;
	protected String connectType = null;
	protected String processorfile = null;
	protected String httpRoot = null;
	protected String webRoot = null;
	protected String SMTPHost = null;
	protected String proxyAddress = null;
	protected String keyStore = null;
	protected String keyStorePass = null;
	protected String socketType = null;
	protected String socketServerClass = null;
	private String logFile = null;
	protected int messageCacheSize = 5000;

	protected ServerCommandLine serverCmdLine = null;
	protected Thread shutdownHookThread;
	protected MimePop_Processor pop = null;
	static final List<String> stat_arr = Arrays.asList("Idle", "Busy", "Free", "Killed", "Finished");

	public Server() throws Exception {
		super();
	}

	@Override
	public void initialize(String[] args) throws AdaptinetException {

		try {
			startSequence();
			loadSettings(args);
			logger.initServer(logFile);
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	@Override
	public void initialize(String configFile) throws AdaptinetException {

		try {
			startSequence();
			fileName = configFile;
			if (fileName != null) {
				loadConfig();
			}
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	@Override
	public void initialize(Properties properties) throws AdaptinetException {

		try {
			startSequence();
			loadConfig(properties);
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	public void startSequence() throws Exception {

		int start = 0;
		try {
			final InetAddress iaddr = InetAddress.getLocalHost();
			host = iaddr.toString();
			if ((start = host.indexOf('/')) > -1) {
				host = host.substring(start + 1);
			}
			identifier = iaddr.getHostName();
		} catch (Exception e) {
			try {
				identifier = "localhost";
				host = InetAddress.getByName("localhost").toString();
				if ((start = host.indexOf('/')) > -1) {
					host = host.substring(start + 1);
				}
			} catch (Exception ee) {
				throw ee;
			}
		}
	}

	public void loadSettings(String[] args) throws AdaptinetException {

		fileName = ServerCommandLine.findConfigFile(args);
		if (fileName != null) {
			loadConfig();
		}
		ServerCommandLine.processCommandLine(args, this);
	}

	@Override
	public void setProperty(String name, String property) {
		ServerInfo.properties.setProperty(name, property);
	}

	@Override
	public String getProperty(String name) {
		return ServerInfo.properties.getProperty(name);
	}

	@Override
	public void start() throws AdaptinetException {

		try {
			IServer.setKey();
			shutdownHookThread = new Thread() {
				@Override
				public void run() {
					try {
						shutdown();
					} catch (Exception e) {
						if (ServerInfo.bVerbose) {
							e.printStackTrace();
						}
					}
				}
			};

			Runtime.getRuntime().addShutdownHook(shutdownHookThread);

			// Create the network agent and do all of the initialization
			if (!standalone) {
				networkAgent = new NetworkAgent(this, nodefilename, autoconnect, connectType, maxnodes, nodelevels);
			}
			initializeSocketServer();
			intializeFactory();
			processorFile.preload();
			if (!standalone) {
				networkAgent.start();
			}
			pop = MimePop_Processor.startMailReader();

		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	protected void initializeSocketServer() throws AdaptinetException {

		try {
			socketServer = BaseSocketServer.createInstance(socketType, socketServerClass);
			socketServer.initialize(this, nPort, nMaxConnections);

			final AdaptinetException serverex = new AdaptinetException(BaseException.SEVERITY_SUCCESS,
					BaseException.GEN_MESSAGE);
			serverex.logMessage(ServerInfo.VERSION + " starting.");

			socketServer.start(identifier);

			if (nAdminPort > 0) {
				adminSocketServer = BaseSocketServer.createInstance("HTTP");
				adminSocketServer.initialize(this, nAdminPort, nMaxConnections);
				adminSocketServer.start(identifier + "admin");
			}

			if (nSecurePort > 0) {
				secureSocketServer = BaseSocketServer.createInstance("TLS" + socketType);
				secureSocketServer.initialize(this, nSecurePort, nMaxConnections);
				secureSocketServer.start(identifier + "SSL");
			}
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}

	}

	public void intializeFactory() throws AdaptinetException {

		try {
			if (processorfile != null) {
				processorFile = new ProcessorFile(processorfile);
			} else {
				processorFile = new ProcessorFile();
			}

			processorFactory = new ProcessorFactory(classpath, verbose);
			processorFactory.initialize(this, nMaxClients);
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	@Override
	public boolean killRequest(String id, boolean force) {
		return processorFactory.killProcessor(id, force);
	}

	@Override
	public boolean killRequest(short id, boolean force) {
		return false;
	}

	@Override
	public List<PropData> requestList() throws AdaptinetException {
		try {
			return processorFactory.getProcessorAgents()
					.values()
					.stream()
					.peek(s -> {
						if (s.getProcessorAgent().getName().isEmpty())
							s.getProcessorAgent().setName("no processor");
					})
					.map(s -> new PropData(	s.getProcessorAgent().getName(), 
											Integer.toString(s.getId()),
											stat_arr.get(s.getStatus())))
					.collect(Collectors.toList());
		} catch (Exception e) {
			final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
					ServerException.TCV_INITFAILDED);
			serverex.logMessage(e);
			throw serverex;
		}
	}

	@Override
	public URL getURL() throws AdaptinetException {

		if (url == null) {
			try {
				if (nPort != 80) {
					url = new URL("http", host, nPort, "/");
				} else {
					url = new URL("http", host, "/");
				}
			} catch (Exception e) {
				final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL,
						ServerException.TCV_URLFAILDED);
				serverex.logMessage(e);
				throw serverex;
			}
		}
		return url;
	}

	@Override
	public synchronized void restart() throws AdaptinetException {
		try {
			shutdown(true);
		} catch (AdaptinetException e) {
			throw e;
		}
	}

	@Override
	public synchronized void shutdown() throws AdaptinetException {
		try {
			shutdown(false);
		} catch (AdaptinetException e) {
			throw e;
		}
	}

	public synchronized void shutdown(boolean restart) throws AdaptinetException {
		try {
			networkAgent.disconnect();
		} catch (Exception e) {
		}

		boolean bAbruptShutdown = shutdownHookThread != null && shutdownHookThread.isAlive();
		try {
			if (pop != null) {
				pop.stop();
			}
		} catch (Exception e) {
		}

		if (!bAbruptShutdown) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
			} catch (Exception e) {
			}
		} else {
			final ServerException se = new ServerException(BaseException.SEVERITY_WARNING,
					ServerException.TCV_ABRUPTSHUTDOWN);
			se.logMessage("It is recommended that the Server be shut down from SWS Administration pages. "
					+ "Failure to do so could result in the loss of system resources.");
		}

		bFinishing = true;
		bRestarting = restart;

		try {
			socketServer.shutdown();
			if (adminSocketServer != null) {
				adminSocketServer.shutdown();
			}
			if (secureSocketServer != null) {
				secureSocketServer.shutdown();
			}

			processorFile.closeFile();
			networkAgent.closeFile();

			try {
				socketServer.join(10000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (!restart) {
				if (!bAbruptShutdown) {
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(BaseException.SEVERITY_FATAL, 999);
		}
	}

	protected void cleanup(boolean restart) throws AdaptinetException {
		try {
			if (socketServer != null) {
				socketServer.shutdown();
			}
			socketServer = null;

			if (adminSocketServer != null) {
				adminSocketServer.shutdown();
			}
			adminSocketServer = null;

			if (secureSocketServer != null) {
				secureSocketServer.shutdown();
			}
			secureSocketServer = null;
			if (logger != null) {
				logger.killServer();
			}
			logger = null;

			bRestarting = false;
			bFinishing = false;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " In cleanup");
		}

		if (restart) {
			try {
				loadConfig();
				logger = new LogServer();
				try {
					logger.initServer(logFile);
				} catch (LoggerException le) {
					le.printStackTrace(System.err);
				}

				start();
			} catch (AdaptinetException e) {
				throw e;
			} catch (Exception e) {
				final ServerException serverex = new ServerException(BaseException.SEVERITY_FATAL, 999);
				serverex.logMessage(e);
				throw serverex;
			}
		}
	}

	@Override
	public void run(Runnable runner) {
		try {
			if (runner instanceof ProcessorAgent) {
				processorFactory.run((ProcessorAgent) runner);
			}
		} catch (ClassCastException cce) {
			final ServerException xse = new ServerException(BaseException.SEVERITY_ERROR, BaseException.GEN_TYPEMISMATCH,
					cce.getMessage());
			xse.logMessage();
		}
	}

	@Override
	public void saveConfig() {
		try {
			Properties serverProps = getConfig();
			serverProps.store(new java.io.FileOutputStream(fileName), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveConfig(Properties serverProps) {
		try {
			serverProps.store(new java.io.FileOutputStream(fileName), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadConfig() {

		try {
			ServerInfo.properties = ServerProperties.getInstance("org.adaptinet.node.server.SimpleProperties");
			ServerInfo.properties.load(new java.io.FileInputStream(fileName));
			loadConfig(ServerInfo.properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadConfig(Properties properties) {

		try {
			String s = null;
			socketType = properties.getProperty(ServerConfig.TYPE, "Processor");
			if (socketType.equals("CUSTOM")) {
				socketServerClass = properties.getProperty(ServerConfig.SOCKETSERVERCLASS);
			} else {
				socketServerClass = null;
			}
			nMaxConnections = Integer.parseInt(properties.getProperty(ServerConfig.MAX_CONNECTIONS, "30"));
			nPort = Integer.parseInt(properties.getProperty(ServerConfig.PORT, "0"));
			nAdminPort = Integer.parseInt(properties.getProperty(ServerConfig.ADMINPORT, "0"));
			maxnodes = Integer.parseInt(properties.getProperty(ServerConfig.MAXNODES, "0"));
			nodelevels = Integer.parseInt(properties.getProperty(ServerConfig.LEVELS, "0"));

			nMaxClients = Integer.parseInt(properties.getProperty(ServerConfig.MAX_CLIENTS, "30"));
			nSecurePort = Integer.parseInt(properties.getProperty(ServerConfig.SECUREPORT, "1443"));

			timeout = Integer.parseInt(properties.getProperty(ServerConfig.CONNECTION_TIMEOUT, "0"));
			timeout *= 1000;

			classpath = properties.getProperty(ServerConfig.CLASSPATH);

			logFile = properties.getProperty(ServerConfig.LOG_FILE);

			s = properties.getProperty(ServerConfig.VERBOSE);
			if (s != null && s.equals(TRUE)) {
				verbose = true;
			} else {
				verbose = false;
			}

			s = properties.getProperty(ServerConfig.AUTOCONNECT);
			if (s != null && s.equals(TRUE)) {
				autoconnect = true;
			} else {
				autoconnect = false;
			}

			s = properties.getProperty(ServerConfig.STANDALONE);
			if (s != null && s.equals(TRUE)) {
				standalone = true;
			} else {
				standalone = false;
			}

			s = properties.getProperty(ServerConfig.SHOWCONSOLE, FALSE);
			if (s != null && s.equals(TRUE)) {
				showconsole = true;
			} else {
				showconsole = false;
			}

			s = properties.getProperty(ServerConfig.USEPROXY, FALSE);
			if (s != null && s.equals(TRUE)) {
				bUseProxy = true;
			} else {
				bUseProxy = false;
			}

			httpRoot = properties.getProperty(ServerConfig.HTTP_ROOT);
			webRoot = properties.getProperty(ServerConfig.WEB_ROOT);
			nodefilename = properties.getProperty(ServerConfig.NODE_FILE);
			connectType = properties.getProperty(ServerConfig.CONNECTTYPE);
			processorfile = properties.getProperty(ServerConfig.PROCESSOR_FILE);
			webRoot = properties.getProperty(ServerConfig.WEB_ROOT);
			httpRoot = properties.getProperty(ServerConfig.HTTP_ROOT, ".");
			SMTPHost = properties.getProperty(ServerConfig.SMTPHOST, "localhost");
			host = properties.getProperty(ServerConfig.HOST, host);
			proxyAddress = properties.getProperty(ServerConfig.PROXYADDRESS, proxyAddress);

			nSecurePort = Integer.parseInt(properties.getProperty(ServerConfig.SECUREPORT, "0"));
			if (nSecurePort > 0) {
				keyStore = properties.getProperty(ServerConfig.KEY_STORE, "KeyStore");
				keyStorePass = properties.getProperty(ServerConfig.KEY_STORE_PASSPHRASE, "seamaster");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSMTPHost() {
		return SMTPHost;
	}

	@Override
	public Properties getConfigFromFile() {
		final Properties serverProps = new Properties();

		try {
			serverProps.load(new java.io.FileInputStream(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serverProps;
	}

	@Override
	public Properties getConfig() {
		ServerInfo.properties = ServerProperties.getInstance("org.adaptinet.node.server.SimpleProperties");

		try {
			ServerInfo.properties.setProperty(ServerConfig.HTTP_ROOT, httpRoot);
			ServerInfo.properties.setProperty(ServerConfig.CLASSPATH, classpath);
			ServerInfo.properties.setProperty(ServerConfig.WEB_ROOT, webRoot);
			ServerInfo.properties.setProperty(ServerConfig.SMTPHOST, SMTPHost);
			ServerInfo.properties.setProperty(ServerConfig.PROXYADDRESS, proxyAddress);
			ServerInfo.properties.setProperty(ServerConfig.HOST, host);
			ServerInfo.properties.setProperty(ServerConfig.LOG_FILE, logFile);
			ServerInfo.properties.setProperty(ServerConfig.MAX_CLIENTS, Integer.toString(nMaxClients));
			ServerInfo.properties.setProperty(ServerConfig.MAX_CONNECTIONS, Integer.toString(nMaxConnections));
			ServerInfo.properties.setProperty(ServerConfig.MAXNODES, Integer.toString(maxnodes));
			ServerInfo.properties.setProperty(ServerConfig.LEVELS, Integer.toString(nodelevels));
			ServerInfo.properties.setProperty(ServerConfig.PORT, Integer.toString(nPort));
			ServerInfo.properties.setProperty(ServerConfig.TYPE, socketType);
			ServerInfo.properties.setProperty(ServerConfig.SOCKETSERVERCLASS, socketServerClass);

			if (timeout > 0) {
				ServerInfo.properties.setProperty(ServerConfig.CONNECTION_TIMEOUT, Integer.toString(timeout / 1000));
			}
			if (nSecurePort > 0) {
				ServerInfo.properties.setProperty(ServerConfig.SECUREPORT, Integer.toString(nSecurePort));
			}
			if (nAdminPort > 0) {
				ServerInfo.properties.setProperty(ServerConfig.ADMINPORT, Integer.toString(nAdminPort));
			}
			if (processorFile != null) {
				ServerInfo.properties.setProperty(ServerConfig.PROCESSOR_FILE, processorFile.getName());
			}
			if (networkAgent != null) {
				ServerInfo.properties.setProperty(ServerConfig.NODE_FILE, networkAgent.getName());
			}
			if (verbose == true) {
				ServerInfo.properties.setProperty(ServerConfig.VERBOSE, TRUE);
			} else {
				ServerInfo.properties.setProperty(ServerConfig.VERBOSE, FALSE);
			}
			if (autoconnect == true) {
				ServerInfo.properties.setProperty(ServerConfig.AUTOCONNECT, TRUE);
			} else {
				ServerInfo.properties.setProperty(ServerConfig.AUTOCONNECT, FALSE);
			}
			if (standalone == true) {
				ServerInfo.properties.setProperty(ServerConfig.STANDALONE, TRUE);
			} else {
				ServerInfo.properties.setProperty(ServerConfig.STANDALONE, FALSE);
			}
			if (bUseProxy == true) {
				ServerInfo.properties.setProperty(ServerConfig.USEPROXY, TRUE);
			} else {
				ServerInfo.properties.setProperty(ServerConfig.USEPROXY, TRUE);
			}
			if (showconsole == true) {
				ServerInfo.properties.setProperty(ServerConfig.SHOWCONSOLE, TRUE);
			} else {
				ServerInfo.properties.setProperty(ServerConfig.SHOWCONSOLE, FALSE);
			}
			if (nSecurePort > 0) {
				ServerInfo.properties.setProperty(ServerConfig.KEY_STORE, keyStore);
				ServerInfo.properties.setProperty(ServerConfig.KEY_STORE_PASSPHRASE, keyStorePass);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ServerInfo.properties;
	}

	@Override
	public ProcessorAgent getAvailableProcessor(String name) {
		return processorFactory.getAvailableProcessor(name);
	}

	@Override
	public Object getAvailableHandler(String name) {
		return null;
	}

	public Object getAvailableBroker(String name) {
		return null;
	}

	@Override
	public Object getAvailableServlet() {
		return null;
	}

	@Override
	public Object getAvailableServlet(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int getLocalPort() {
		return socketServer.getLocalPort();
	}

	public int getSecureLocalPort() {
		return secureSocketServer.getLocalPort();
	}

	public int getAdminLocalPort() {
		return adminSocketServer.getLocalPort();
	}

	@Override
	public InetAddress getInetAddress() {
		return socketServer.getInetAddress();
	}

	public InetAddress getAdminInetAddress() {
		return adminSocketServer.getInetAddress();
	}

	public InetAddress getSecureInetAddress() {
		return secureSocketServer.getInetAddress();
	}

	@Override
	final public int getConnectionTimeOut() {
		return timeout;
	}

	@Override
	final public void setConnectionTimeOut(int newValue) {
		timeout = newValue;
	}

	@Override
	final public String getHost() {
		return host;
	}

	@Override
	final public String getProxyAddress() {
		return proxyAddress;
	}

	@Override
	final public void setHost(String host) {
		this.host = host;
	}

	@Override
	final public int getPort() {
		return nPort;
	}

	@Override
	final public void setPort(int nPort) {
		this.nPort = nPort;
	}

	@Override
	final public String getSocketType() {
		return socketType;
	}

	@Override
	final public void setSocketType(String socketType) {
		this.socketType = socketType;
	}

	@Override
	final public String getSocketServerClass() {
		return socketServerClass;
	}

	@Override
	final public void setSocketServerClass(String socketServerClass) {
		this.socketServerClass = socketServerClass;
	}

	@Override
	final public int getAdminPort() {
		return nAdminPort;
	}

	@Override
	final public void setAdminPort(int nAdminPort) {
		this.nAdminPort = nAdminPort;
	}

	@Override
	final public int getSecurePort() {
		return nSecurePort;
	}

	@Override
	final public int getMessageCacheSize() {
		return messageCacheSize;
	}

	@Override
	final public void setMessageCacheSize(int nMessageCacheSize) {
		this.messageCacheSize = nMessageCacheSize;
	}

	@Override
	final public void setSecurePort(int nSecurePort) {
		this.nSecurePort = nSecurePort;
	}

	@Override
	public Object getRegistryFile() {
		return null;
	}

	@Override
	public Object getRegistryDirectory() {
		return null;
	}

	@Override
	final public boolean getVerboseFlag() {
		return verbose;
	}

	@Override
	final public void setVerboseFlag(boolean verbose) {
		this.verbose = verbose;
	}

	final public boolean getAutoConnectFlag() {
		return autoconnect;
	}

	@Override
	final public void setAutoConnectFlag(boolean autoconnect) {
		this.autoconnect = autoconnect;
	}

	final public boolean getStandAloneFlag() {
		return standalone;
	}

	@Override
	final public void setStandAloneFlag(boolean standalone) {
		this.standalone = standalone;
	}

	@Override
	final public Thread getThread() {
		return null;
	}

	@Override
	final public int getClientThreadPriority() {
		return clientPriority;
	}

	@Override
	final public String getHTTPRoot() {
		if (httpRoot != null && !httpRoot.endsWith(File.separator)) {
			httpRoot += File.separator;
		}
		return httpRoot;
	}

	@Override
	final public void setHTTPRoot(String httpRoot) {
		this.httpRoot = httpRoot;
	}

	@Override
	final public String getWebRoot() {
		return this.webRoot;
	}

	@Override
	final public String getClasspath() {
		return classpath;
	}

	@Override
	public String getServletClasspath() {
		return null;
	}

	@Override
	final public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	@Override
	final public int getMaxClients() {
		return nMaxClients;
	}

	@Override
	final public void setMaxClients(int nMaxClients) {
		this.nMaxClients = nMaxClients;
	}

	@Override
	final public int getMaxConnections() {
		return nMaxConnections;
	}

	@Override
	final public void setMaxConnections(int nMaxConnections) {
		this.nMaxConnections = nMaxConnections;
	}

	@Override
	public String getXSLPath() {
		return null;
	}

	@Override
	public Object getFaultTolDBMgr() {
		return null;
	}

	@Override
	public final boolean usesFaultTolerance() {
		return false;
	}

	@Override
	public final boolean useProxy() {
		return bUseProxy;
	}

	@Override
	public Object getSetting(String name) {
		if (name.equalsIgnoreCase(ServerConfig.SHOWCONSOLE)) {
			return new Boolean(showconsole);
		}
		return null;
	}

	@Override
	public Object getService(String name) {

		if (name.equalsIgnoreCase(ServerConfig.NETWORKAGENT)) {
			return networkAgent;
		} else if (name.equalsIgnoreCase(ServerConfig.NODE_FILE)) {
			return nodefilename;
		} else if (name.equalsIgnoreCase(ServerConfig.PROCESSOR_FILE)) {
			return processorFile;
		} else if (name.equalsIgnoreCase(ServerConfig.PROCESSORFACTORY)) {
			return processorFactory;
		}
		return null;
	}

	@Override
	public void setLogPath(String path) {
		logFile = path;
	}

	@Override
	public String getLogPath() {
		return logFile;
	}

	@Override
	public PrintStream getConsoleStream() {
		return logger.getConsoleStream();
	}

	@Override
	public PrintStream getLogStream() {
		return logger.getConsoleStream();
	}

	public static void main(String args[]) {

		Server server = null;

		try {
			do {
				System.out.println(args[0]);
				try {
					server = new Server();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

				server.initialize(args);
				System.out.println(ServerInfo.VERSION + " listening on port [" + server.nPort + "] started...");
				if (server.getAdminPort() > 0) {
					System.out.println("Administration listening on port [" + server.nAdminPort
							+ "], web serving from [" + server.getWebRoot() + "]...");
				}
			} while (server.bRestarting == true);
		} catch (Exception e) {
			try {
				if (server != null) {
					server.cleanup(false);
				}
			} catch (AdaptinetException ce) {
				ce.printStackTrace();
			}
			System.err.println(ServerInfo.VERSION + " is shutting down..." + "Exception=[" + e.getMessage() + "]");
			System.exit(1);
		}
	}
}
