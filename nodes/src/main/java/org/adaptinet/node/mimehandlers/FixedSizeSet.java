package org.adaptinet.node.mimehandlers;


import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

class FixedSizeSet<E> extends AbstractSet<E> { 
    private final LinkedHashMap<E, E> contents; 
 
    FixedSizeSet(final int maxCapacity) { 
        contents = new LinkedHashMap<E, E>(maxCapacity * 4 /3, 0.75f, false) { 
			private static final long serialVersionUID = 1L;
			@Override 
            protected boolean removeEldestEntry(java.util.Map.Entry<E, E> eldest) { 
                return size() == maxCapacity; 
            } 
        };       
    } 
 
    @Override 
    public Iterator<E> iterator() { 
        return contents.keySet().iterator(); 
    } 
 
    @Override 
    public int size() { 
        return contents.size(); 
    } 
 
    public boolean add(E e) { 
        boolean hadNull = false; 
        if (e == null) { 
            hadNull = contents.containsKey(null); 
        } 
        E previous = contents.put(e, e); 
        return e == null ? hadNull : previous != null; 
    } 
 
    @Override 
    public boolean contains(Object o) { 
        return contents.containsKey(o); 
    } 
} 

