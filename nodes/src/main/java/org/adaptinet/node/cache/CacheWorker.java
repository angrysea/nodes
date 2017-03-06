package org.adaptinet.node.cache;

import java.util.Map.Entry;

import org.adaptinet.node.messaging.Address;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.messaging.Messenger;
import org.adaptinet.node.processoragent.ProcessorFactory;
import org.adaptinet.node.processoragent.Worker;


public class CacheWorker extends Worker {

	static final private String PUT = "/cache/put";
	static final private String REMOVE = "/cache/remove";
	static final private String SETLOCKED = "/cache/setLocked";

	public CacheWorker(Address address) {
		super(address);
	}

	public void put(String name, String key, String value) {
		Message message = null;

		try {
			message = new Message(address.getURL() + PUT);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key, value);
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void remove(String name, String key) {
		Message message = null;

		try {
			message = new Message(address.getURL() + REMOVE);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key);
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void setLocked(String name, boolean b) {
		Message message = null;

		try {
			message = new Message(address.getURL() + SETLOCKED);
			message.getReplyTo().setProcessor(ProcessorFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, new Boolean(b));
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void doCacheCheck() {
		Message message = null;

		try {
			for (Entry<String, Cache> entry : CacheProcessor.getNamedCaches().entrySet()) {
				message = new Message(address.getURL()
						+ "/CacheProcessor/cacheCheck");
				message.getReplyTo().setProcessor("CacheProcessor");
				bresponded = false;
				starttime = System.currentTimeMillis();
				Messenger.postMessage(message, entry.getKey());
			}
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}
	
}
