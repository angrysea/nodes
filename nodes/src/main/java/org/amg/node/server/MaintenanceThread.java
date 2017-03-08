package org.amg.node.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.amg.node.node.NodeRoot;
import org.amg.node.processoragent.MaintenanceProcessor;
import org.amg.node.processoragent.MaintenanceWorker;

public class MaintenanceThread implements Runnable {

	private boolean bConnected = false;
	private boolean bStopped = false;
	private Thread runner = null;
	private NodeRoot root = null;
	private IResetEvent resetEvent = null;
	private Map<String, String> timings = Collections
			.synchronizedMap(new HashMap<String, String>(87));

	public MaintenanceThread(NodeRoot root, IResetEvent resetEvent) {
		this.root = root;
		this.resetEvent = resetEvent;
	}

	void start() {
		if(runner!=null) {
			runner = new Thread(this);
			runner.start();
		}
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
			// Just wait let stuff shake out.
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}

			MaintenanceProcessor.clear();
			synchronized (root) {
				if (root.count() > 0 && bConnected) {
					root.maintenance();
				}
			}

			while (!bStopped) {
				synchronized (this) {
					/**
					 * Wait for the nodes to respond if they have not responded
					 * in 5 seconds we should assume the are gone.
					 */
					try {
						runner.wait(5000);
					} catch (InterruptedException ex) {
						break;
					}
				}

				try {
					if (bConnected) {
						for(MaintenanceWorker worker : MaintenanceProcessor.workers()) {
							worker.doPing();
							if (!worker.getResponded()) {
								try {
									/** Don't think this is need lets hold it for now.
									Node workerNode = root.get(worker
											.getAddress());
									if (workerNode.count(root) > 1) {
										root.doDisconnect(workerNode);
									}
									**/
									root.remove(worker.getAddress());
								} catch (Exception exx) {
									exx.printStackTrace();
								}
							}
							timings.put(worker.getAddress().getURL(),
									Long.toString(worker.getEndTime()));
						}
					} else {
						return;
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				root.optimize();
				// Run the maintenance loop every 2 minutes
				synchronized (runner) {
					try {
						runner.wait(120000);
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
			resetEvent.reset();
			runner.interrupt();
			runner.join();
			runner=null;
		}
	}

	public final void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

}
