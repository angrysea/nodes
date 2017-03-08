package org.amg.node.xmltools.xmlutils;

import java.util.HashMap;
import java.util.Map;

public class AliasProcessor {

	static private Map<String, AliasProcessor> classMap = null;
	private Map<String, String> toAliases = new HashMap<String, String>();
	private Map<String, String> fromAliases = new HashMap<String, String>();
	
	static public final Map<String, AliasProcessor> getClassMap() {
		return classMap;
	}
	
	static public void setAlias(String className, String property, String alias) {
		if (classMap == null) {
			classMap = new HashMap<String, AliasProcessor>();
		}
		AliasProcessor aliasProcessor = classMap.get(className);
		if(aliasProcessor==null) {
			aliasProcessor = new AliasProcessor();
			classMap.put(className, aliasProcessor);
		}
		aliasProcessor.addAlias(property, alias);
	}
	
	static public String getAlias(String className, String property) {
		String alias = null;
		if (classMap != null) {
			AliasProcessor aliasProcessor = classMap.get(className);
			if(aliasProcessor!=null) {
				alias = aliasProcessor.getAlias(property);
			}
		}
		return alias==null?property:alias;
	}
	
	static public String getProperty(String className, String alias) {
		String property = alias;
		if (classMap != null) {
			AliasProcessor aliasProcessor = classMap.get(className);
			if(aliasProcessor!=null) {
				property = aliasProcessor.getProperty(property);
			}
		}
		return property==null?alias:property;
	}
	
	public void addAlias(String property, String alias) {
		toAliases.put(property, alias);
		fromAliases.put(alias, property);
	}

	public String getAlias(String property) {
		return toAliases.get(property);
	}

	public String getProperty(String alias) {
		return fromAliases.get(alias);
	}

}
