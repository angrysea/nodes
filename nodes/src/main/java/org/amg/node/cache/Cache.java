package org.amg.node.cache;

import java.util.HashMap;
import java.util.Map;

public class Cache extends HashMap<String, String> {

	static private final long serialVersionUID = -2323837462860727180L;
	private boolean locked = false;
	private String name = null;
	private ICacheListener listener = null;
	
	static public final Cache getNamedCache(String key) {
		return CacheProcessor.getNamedCache(key);
	}
	
	public Cache(ICacheListener listener, String name, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name, int initialCapacity) {
		super(initialCapacity);
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name) {
		super();
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name, Map<? extends String, ? extends String> m) {
		super(m);
		this.name = name;
		this.listener = listener;
	}

	public String put(String key, String value) {
		String ret = super.put(key, value);
		listener.putValue(name, key, value);
		return ret;
	}

	public String putNoEvent(String key, String value) {
		String ret = super.put(key, value);
		return ret;
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		super.putAll(m);
		m.entrySet().stream()
					.forEach(e -> listener.putValue(name, e.getKey(), e.getValue()));
	}

	public String remove(String key) {
		final String ret = super.remove(key);
		listener.removeValue(name, key);
		return ret;
	}

	public String removeNoEvent(String key) {
		return super.remove(key);
	}

	public final void setLocked(boolean locked) {
		this.locked = locked;
		listener.cacheLocked(name, locked);
	}

	public final void setLockedNoEvent(boolean locked) {
		this.locked = locked;
	}

	public final boolean isLocked() {
		return locked;
	}

	public final String getName() {
		return name;
	}
}
