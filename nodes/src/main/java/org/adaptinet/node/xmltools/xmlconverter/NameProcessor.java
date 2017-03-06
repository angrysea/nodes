package org.adaptinet.node.xmltools.xmlconverter;

import java.util.*;

public class NameProcessor {

	private HashMap<String, String> nametable;

	public NameProcessor() {
		nametable = new HashMap<String, String>();
	}

	public final void setProperty(ElementType elementType, String name) {
		String temp = null;
		if (name.indexOf('-') > -1) {
			temp = name;
			name = temp.replace('-', '_');
			nametable.put(name, temp);
		}
		elementType.setProperty("name", name);
	}

	public final String generateCode() {
		StringBuffer ret = new StringBuffer();
		if (nametable.size() > 0) {
			ret.append("\n\n\tpublic String getElementName(String name) {\n");
			ret.append("\t\treturn (String)nametable.get(name);\n\t}\n\n");
			ret = ret
					.append("\tstatic private Hashtable nametable = new Hashtable();\n\n");
			ret = ret.append("\tstatic {\n");
			Set<String> keys = nametable.keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				String value = nametable.get(key);
				ret.append("\t\tnametable.put(");
				ret.append(key);
				ret.append(", ");
				ret.append(value);
				ret.append(");\n");
			}
			ret.append("\t}\n\n");
		}
		return ret.toString();
	}
}
