package org.amg.node.cache;

import org.amg.node.messaging.Address;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.processoragent.ProcessorFactory;
import org.amg.node.processoragent.Worker;


public class CacheWorker extends Worker {

	static final private String PUT = "/cache/put";
	static final private String REMOVE = "/cache/remove";
	static final private String SETLOCKED = "/cache/setLocked";

	public CacheWorker(Address address) {
		super(address);
	}

	public void put(final String name, final String key, final String value) {
		try {
			final Message message = new Message(address.getURL() + PUT);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key, value);
		} catch (final Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void remove(final String name, final String key) {
		try {
			final Message message = new Message(address.getURL() + REMOVE);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key);
		} catch (final Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void setLocked(final String name, final boolean b) {
		try {
			final Message message = new Message(address.getURL() + SETLOCKED);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, new Boolean(b));
		} catch (final Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void doCacheCheck() {
		try {
			Message message = new Message(address.getURL()
					+ "/CacheProcessor/cacheCheck");
			message.getReplyTo().setProcessor("CacheProcessor");
			bresponded = false;
			starttime = System.currentTimeMillis();
			CacheProcessor.getNamedCaches().entrySet().forEach(e -> Messenger.postMessage(message, e.getKey()));
		} catch (final Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}
}
