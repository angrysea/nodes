package org.amg.node.xmltools.xmlutils;

import java.util.*;

final public class XMLElement {
	public String getBeginEvent() {
		return beginEvent;
	}

	public String getDataEvent() {
		return dataEvent;
	}

	public String getEndEvent() {
		return endEvent;
	}

	public void setBeginEvent(String strBeginEvent) {
		if (strBeginEvent != null) {
			bHasEvent = true;
			beginEvent = strBeginEvent;
		}
	}

	public void setDataEvent(String strDataEvent) {
		if (strDataEvent != null) {
			bHasEvent = true;
			dataEvent = strDataEvent;
		}
	}

	public void setEndEvent(String strEndEvent) {
		if (strEndEvent != null) {
			bHasEvent = true;
			endEvent = strEndEvent;
		}
	}

	public void put(String sName, XMLElement element) {
		if (debug == true) {
			element.name = sName;
		}
		tags.put(sName, element);
	}

	public XMLElement get(String sName) {
		return get(sName, 99999);
	}

	public XMLElement get(String sName, int level) {
		XMLElement element = null;
		if (sName.equals(name) == true) {
			return this;
		}
		element = (XMLElement) tags.get(sName);
		if (element == null) {
			if (level > 0) {
				Collection<XMLElement> c = tags.values();
				Iterator<XMLElement> it = c.iterator();
				while (it.hasNext() == true) {
					element = ((XMLElement) it.next()).get(sName, --level);
					if (element != null) {
						break;
					}
				}
			}
		}
		return element;
	}

	public void clear() {
		tags.clear();
	}

	public boolean hasEvent() {
		return bHasEvent;
	}

	private HashMap<String, XMLElement> tags = new HashMap<String, XMLElement>();
	private String name;
	private String beginEvent;
	private String dataEvent;
	private String endEvent;
	private boolean bHasEvent = false;
	private boolean debug = true;
}
