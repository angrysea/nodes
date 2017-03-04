package org.adaptinet.node.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.adaptinet.node.exception.BaseException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Address;
import org.adaptinet.node.messaging.Body;
import org.adaptinet.node.messaging.Envelope;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.messaging.Messenger;
import org.adaptinet.node.processoragent.SystemProcessor;
import org.adaptinet.node.registry.ProcessorEntry;

public final class CacheProcessor extends SystemProcessor implements ICacheListener {

	static final public String PUT = "put";
	static final public String REMOVE = "remove";
	static final public String SETLOCKED = "setLocked";
	static final public String GETNAMEDCACHE = "getNamedCache";
	static final public String REMOVENAMEDCACHE = "removeNamedCache";
	static final public String CACHECHECK = "cacheCheck";

	static private Map<String, CacheWorker> workers = Collections.synchronizedMap(new HashMap<String, CacheWorker>(10));
	static private HashMap<String, Cache> namedCaches = new HashMap<String, Cache>();
	static private CacheProcessor cacheProcessor = null;

	public CacheProcessor() {
		cacheProcessor = this;
	}

	static public final Cache getNamedCache(Object key) {
		return getNamedCache(key, true);
	}

	static public final Cache getNamedCache(Object key, boolean bNew) {

		Cache namedCache = namedCaches.get(key);
		if (namedCache == null && bNew) {
			namedCache = new Cache(cacheProcessor, key.toString());
			namedCaches.put((String) key, namedCache);
		}
		return namedCache;
	}

	static public final HashMap<String, Cache> getNamedCaches() {
		return namedCaches;
	}

	@Override
	public void startProcessor(ProcessorEntry entry) throws Exception {
	}

	@Override
	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	@Override
	public Object process(Envelope env) throws Exception {

		try {
			if (env.isMethod(PUT)) {
				put(env);
			} else if (env.isMethod(REMOVE)) {
				remove(env);
			} else if (env.isMethod(SETLOCKED)) {
				setLocked(env);
			} else if (env.isMethod(GETNAMEDCACHE)) {
				getNamedCache(env);
			} else if (env.isMethod(CACHECHECK)) {
				cacheCheck(env);
			} else if (env.isMethod(REMOVENAMEDCACHE)) {
				removeNamedCache(env);
			}
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(BaseException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex.logMessage("Method not supported by Cache Processor. " + e.getMessage());
			throw e;
		}
		return null;
	}

	public void put(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 3)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.putNoEvent((String) args[1], (String) args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.removeNoEvent((String) args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLocked(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.setLockedNoEvent(((Boolean) args[1]).booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public void getNamedCache(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			if (!cache.isEmpty()) {
				Address sender = env.getReplyTo();
				Message message = new Message("http://" + sender.getHost() + ":" + sender.getPort() + "/cache/" + PUT);
				for (Map.Entry entry : cache.entrySet()) {
					Messenger.postMessage(message, entry.getKey(), entry.getValue());
				}
			}
			cache.setLockedNoEvent(((Boolean) args[1]).booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cacheCheck(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);

			if (cache == null) {
				Address sender = env.getReplyTo();
				Message message = new Message(
						"http://" + sender.getHost() + ":" + sender.getPort() + "/cache/" + GETNAMEDCACHE);
				Messenger.postMessage(message, args[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeNamedCache(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			namedCaches.remove(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String process(String xml) throws Exception {

		ProcessorException agentex = new ProcessorException(BaseException.SEVERITY_FATAL,
				ProcessorException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	static public void doCacheCheck(Address address) {

		CacheWorker worker = new CacheWorker(address);
		workers.put(address.getURL(), worker);
	}

	static public Iterator<CacheWorker> iterator() {
		return workers.values().iterator();
	}

	static public void remove(String url) {
		workers.remove(url);
	}

	static public void clear() {
		workers.clear();
	}

	public void putValue(String name, String key, String value) {

		CacheWorker worker = null;
		Iterator<CacheWorker> it = CacheProcessor.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.put(name, key, value);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}

	public void removeValue(String name, String key) {

		CacheWorker worker = null;
		Iterator<CacheWorker> it = CacheProcessor.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.remove(name, key);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}

	public void cacheLocked(String name, boolean locked) {

		CacheWorker worker = null;
		Iterator<CacheWorker> it = CacheProcessor.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.setLocked(name, locked);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}
}
