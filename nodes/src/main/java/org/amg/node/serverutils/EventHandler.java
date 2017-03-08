package org.amg.node.serverutils;

public interface EventHandler {
	void handleTimerEvent(Object data, long time);
}