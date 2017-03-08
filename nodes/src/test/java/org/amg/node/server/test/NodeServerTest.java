package org.amg.node.server.test;

import org.amg.node.server.Server;
import org.junit.Test;

public class NodeServerTest {



	@Test
	public void ServerLoadSettingsTest() {
		try {
			String  args[] = new String[2];
			args[0] = "-config";
			args[1] = "d:/git/nodes/target/test-classes/server1.properties";
			Server server = new Server();
			server.loadSettings(args);
			String host = new String();
			host = server.getHost();
			assert(!host.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void ServerCycleTest() {
		try {
			Server server = new Server();
			server.initialize("d:/git/nodes/target/test-classes/server1.properties");
			Thread.sleep(1000);
			server.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
}
