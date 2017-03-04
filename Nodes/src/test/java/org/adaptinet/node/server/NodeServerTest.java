package org.adaptinet.node.server;

import org.junit.Test;

public class NodeServerTest {

	@Test
	void ServerConfigTest() {
		try {
			String args[] = new String[2];
			args[0] = new String("-config");
			Server server = new Server();
			server.loadSettings(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
