package org.amg.node.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.amg.node.exception.BaseException;
import org.amg.node.exception.ProcessorException;
import org.amg.node.messaging.Address;
import org.amg.node.messaging.Body;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.processoragent.SystemProcessor;
import org.amg.node.registry.ProcessorEntry;

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

	static public final Cache getNamedCache(final Object key) {
		return getNamedCache(key, true);
	}

	static public final Cache getNamedCache(final Object key, final boolean bNew) {
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
	public void startProcessor(final ProcessorEntry entry) throws Exception {
	}

	@Override
	public boolean preProcessMessage(final Envelope env) {
		return true;
	}

	@Override
	public Object process(final Envelope env) throws Exception {
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
		} catch (final Exception e) {
			ProcessorException agentex = new ProcessorException(BaseException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex.logMessage("Method not supported by Cache Processor. " + e.getMessage());
			throw e;
		}
		return null;
	}

	public void put(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 3)
				throw new Exception("Out of Bounds");
			final Cache cache = getNamedCache(args[0]);
			cache.putNoEvent((String) args[1], (String) args[2]);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			final Cache cache = getNamedCache(args[0]);
			cache.removeNoEvent((String) args[1]);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void setLocked(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			final Cache cache = getNamedCache(args[0]);
			cache.setLockedNoEvent(((Boolean) args[1]).booleanValue());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void getNamedCache(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			final Cache cache = getNamedCache(args[0]);
			if (!cache.isEmpty()) {
				final Address sender = env.getReplyTo();
				final Message message = new Message(
						"http://" + sender.getHost() + ":" + sender.getPort() + "/cache/" + PUT);
				for (Map.Entry<String, String> entry : cache.entrySet()) {
					Messenger.postMessage(message, entry.getKey(), entry.getValue());
				}
			}
			cache.setLockedNoEvent(((Boolean) args[1]).booleanValue());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void cacheCheck(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			final Cache cache = getNamedCache(args[0]);

			if (cache == null) {
				final Address sender = env.getReplyTo();
				final Message message = new Message(
						"http://" + sender.getHost() + ":" + sender.getPort() + "/cache/" + GETNAMEDCACHE);
				Messenger.postMessage(message, args[0]);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void removeNamedCache(final Envelope env) {
		final Body body = env.getBody();
		try {
			final Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			namedCaches.remove(args[0]);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public String process(final String xml) throws Exception {
		final ProcessorException agentex = new ProcessorException(BaseException.SEVERITY_FATAL,
				ProcessorException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	static public void doCacheCheck(final Address address) {
		final CacheWorker worker = new CacheWorker(address);
		workers.put(address.getURL(), worker);
	}

	static public Iterator<CacheWorker> iterator() {
		return workers.values().iterator();
	}

	static public void remove(final String url) {
		workers.remove(url);
	}

	static public void clear() {
		workers.clear();
	}

	@Override
	public void putValue(final String name, final String key, final String value) {
		workers.values().stream().filter(e -> !e.getResponded()).forEach(e -> e.put(name, key, value));
	}

	@Override
	public void removeValue(final String name, final String key) {
		workers.values().stream().filter(e -> !e.getResponded()).forEach(e -> e.remove(name, key));
	}

	@Override
	public void cacheLocked(final String name, final boolean locked) {
		workers.values().stream().filter(e -> !e.getResponded()).forEach(e -> e.setLocked(name, locked));
	}
}
