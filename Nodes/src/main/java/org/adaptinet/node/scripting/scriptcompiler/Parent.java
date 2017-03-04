package org.adaptinet.node.scripting.scriptcompiler;

import java.util.Enumeration;

public interface Parent
{
    public void insertChild(Element child);
    public Enumeration<Element> children();
    public boolean isBlock();
}