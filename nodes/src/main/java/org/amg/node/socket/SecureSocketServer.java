package org.amg.node.socket;

import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.amg.node.server.IServer;
import org.amg.node.server.ServerConfig;

public class SecureSocketServer extends HttpSocketServer {

	protected ServerSocket createServerSocket() {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");

			String keyFile = IServer.getServer().getConfigFromFile().getProperty(ServerConfig.KEY_STORE,
					"XMLAgentKeyStore");
			if (keyFile == null)
				throw new Exception("KeyStore for SSL is not in the configuration of Node Server.");

			File fileKeyStore = new File(keyFile);
			if (!fileKeyStore.exists())
				throw new Exception("KeyStore " + fileKeyStore.getAbsolutePath() + " does not exist.");

			char[] passphrase = (IServer.getServer().getConfigFromFile().getProperty(ServerConfig.KEY_STORE_PASSPHRASE,
					"seamaster")).toCharArray();
			ks.load(new FileInputStream(fileKeyStore), passphrase);
			kmf.init(ks, passphrase);

			System.out.print("Starting SSL Listener...");
			ctx.init(kmf.getKeyManagers(), null, null);
			System.out.println("done.");

			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
			ssf.createServerSocket(port, Math.max(128, sockets));
		} catch (Exception e) {
		}
		return null;
	}
}
