package org.amg.node.server;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.amg.node.exception.AMGException;
import org.amg.node.socket.PropData;

public abstract class IServer {

	static private IServer server = null;
	static public boolean bVerbose = false;
	static String key = null;

	public IServer() throws Exception {
		if (server == null) {

			synchronized (IServer.class) {
				if (server == null) {
					server = this;
				} else {
					throw new Exception("Can not instantiate second instance of IServer");
				}
			}
		} else {
			throw new Exception("Can not instantiate second instance of IServer");
		}
	}

	static final public IServer getServer() {
		return server;
	}

	static final public Object getNamedService(String name) {
		return server.getService(name);
	}

	static public String getKey() {
		return key;
	}

	static protected void setKey() {
		try {
			key = Integer.toString((server.getHost() + Integer.toString(server.getPort())).hashCode());
		} catch (Exception e) {
		}
	}

	static public String getCertificate() {
		return null;
	}

	static public Object getProcessor(String name) {
		return server.getAvailableProcessor(name);
	}

	public static File findFile(String name) {
		final String classpath = System.getProperty("java.class.path");
		File file = null;
		try {
			final String classToken[] = classpath.split(File.pathSeparator);
			for (String filename : classToken) {
				final String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip") || filenameLower.endsWith(".jar")) {
					try {
						final JarFile jarFile = new JarFile(filename);
						final JarEntry j = jarFile.getJarEntry(name);
						if (j != null) {
							file = new File(filename);
							break;
						}
						jarFile.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (!filename.endsWith(File.separator)) {
						filename += File.separatorChar;
					}

					final File f = new File(filename + name);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public abstract void initialize(Properties properties) throws AMGException;

	public abstract void initialize(String configFile) throws AMGException;

	public abstract PrintStream getConsoleStream();

	public abstract PrintStream getLogStream();

	public abstract void initialize(String[] args) throws AMGException;

	public abstract void start() throws AMGException;

	public abstract URL getURL() throws AMGException;

	public abstract int getLocalPort();

	public abstract String getIdentifier();

	public abstract InetAddress getInetAddress();

	public abstract void shutdown() throws AMGException;

	public abstract void restart() throws AMGException;

	public abstract boolean killRequest(short key, boolean bForce);

	public abstract boolean killRequest(String key, boolean bForce);

	public abstract List<PropData> requestList() throws AMGException;

	public abstract Object getAvailableProcessor(String name);

	public abstract Object getAvailableHandler(String name);

	public abstract Object getAvailableServlet();

	public abstract Object getAvailableServlet(String name);

	public abstract void run(Runnable runner);

	public abstract void setProperty(String name, String property);

	public abstract String getProperty(String name);

	public abstract int getConnectionTimeOut();

	public abstract void setConnectionTimeOut(int newValue);

	public abstract String getHost();

	public abstract void setHost(String host);

	public abstract int getPort();

	public abstract void setPort(int nPort);

	public abstract int getAdminPort();

	public abstract void setAdminPort(int nPort);

	public abstract int getSecurePort();

	public abstract void setSecurePort(int nSecurePort);

	public abstract Object getRegistryFile();

	public abstract Object getRegistryDirectory();

	public abstract boolean getVerboseFlag();

	public abstract void setVerboseFlag(boolean verbose);

	public abstract void setAutoConnectFlag(boolean connect);

	public abstract void setStandAloneFlag(boolean standalone);

	public abstract Thread getThread();

	public abstract int getClientThreadPriority();

	public abstract String getHTTPRoot();

	public abstract void setHTTPRoot(String httpRoot);

	public abstract String getXSLPath();

	public abstract String getWebRoot();

	public abstract String getClasspath();

	public abstract void setClasspath(String classpath);

	public abstract int getMaxClients();

	public abstract void setMaxClients(int nMaxClients);

	public abstract int getMaxConnections();

	public abstract void setMaxConnections(int nMaxConnections);

	public abstract String getLogPath();

	public abstract void setLogPath(String path);

	public abstract String getSocketType();

	public abstract void setSocketType(String type);

	public abstract String getSocketServerClass();

	public abstract void setSocketServerClass(String type);

	public abstract Properties getConfig();

	public abstract Properties getConfigFromFile();

	public abstract void saveConfig();

	public abstract void saveConfig(Properties properties);

	public abstract String getServletClasspath();

	public abstract String getSMTPHost();

	public abstract Object getFaultTolDBMgr();

	public abstract boolean usesFaultTolerance();

	public abstract boolean useProxy();

	public abstract String getProxyAddress();

	public abstract Object getService(String name);

	public abstract Object getSetting(String name);

	public abstract int getMessageCacheSize();

	public abstract void setMessageCacheSize(int nMessageCacheSize);
}
