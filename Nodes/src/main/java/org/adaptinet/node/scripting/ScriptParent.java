package org.adaptinet.node.scripting;

import java.util.Enumeration;

public interface ScriptParent {

	public void insertChild(ScriptElement child);

	public Enumeration<ScriptElement> children();

	public boolean isBlock();
}