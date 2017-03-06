package org.adaptinet.node.server;

import java.util.Iterator;

import org.adaptinet.node.cache.CacheProcessor;
import org.adaptinet.node.cache.CacheWorker;
import org.adaptinet.node.node.NodeRoot;

public class CacheThread implements Runnable {

	private boolean bConnected = false;
	private boolean bStopped = false;
	private Thread runner = null;
	private NodeRoot root = null;

	public CacheThread(NodeRoot root) {
		this.root = root;
	}

	void start() {
		runner = new Thread(this);
		runner.start();
	}

	public void join() throws InterruptedException {
		runner.join();
	}

	public void join(long wait) throws InterruptedException {
		runner.join(wait);
	}

	public boolean isAlive() {
		return runner.isAlive();
	}

	public void run() {
		try {
			/**
			 * Just wait let stuff shake out.
			 */
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}

			synchronized (root) {
				if (root.count() > 0 && bConnected) {
					root.cache();
				}
			}

			while (!bStopped) {
				try {
					if (bConnected) {
						CacheWorker worker = null;
						Iterator<CacheWorker> it = CacheProcessor.iterator();
						while (it.hasNext()) {
							worker = it.next();
							try {
								worker.doCacheCheck();
							} catch (Exception exx) {
								exx.printStackTrace();
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				// Run the maintenance loop every minute
				synchronized (runner) {
					try {
						runner.wait(60000);
					} catch (InterruptedException ex) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void stop() throws InterruptedException {
		bStopped = true;
		if (runner != null) {
			runner.interrupt();
			runner.join();
		}
	}

	public final void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

}
