package org.amg.node.serverutils;

public class Mutex {
	Thread owner = null;

	public String toString() {
		String name;

		if (owner == null) {
			name = "null";
		} else {
			name = owner.getName();
		}

		return ("<" + super.toString() + "owner:" + name + ">");
	}

	public synchronized void lock() {
		boolean interrupted = false;

		while (owner != null) {
			try {
				wait();
			} catch (InterruptedException ie) {
				interrupted = true;
			}
		}

		owner = Thread.currentThread();

		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	public synchronized void unlock() {
		if (owner != Thread.currentThread()) {
			throw new IllegalMonitorStateException("Not owner");
		}
		owner = null;
		notify();
	}

}
