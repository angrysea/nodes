package org.adaptinet.node.cache;

public interface ICacheListener {

	public void putValue(String name, String key, String value);
	public void removeValue(String name, String key);
	public void cacheLocked(String name, boolean locked);
}
