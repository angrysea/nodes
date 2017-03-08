package org.amg.node.cache;

public interface ICacheListener {

	public void putValue(final String name, final String key, final String value);
	public void removeValue(final String name, final String key);
	public void cacheLocked(final String name, final boolean locked);
}
