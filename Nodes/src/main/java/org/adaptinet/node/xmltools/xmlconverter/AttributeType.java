package org.adaptinet.node.xmltools.xmlconverter;

import java.util.HashMap;

import org.adaptinet.node.xmltools.xmlutils.NameMangler;


public class AttributeType extends XmlBase {

	public AttributeType() {
	}

	public final void generateCode(StringBuffer ret) {
		try {
			String name = super.properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String type = convertType(super.properties.getProperty("type",
						"char[]"), false);
				if (type.equalsIgnoreCase("char[]")) {
					ret.append("\tpublic String get");
					ret.append(name);
					ret.append("() {\n\t\tif(_");
					ret.append(name);
					ret.append("!=null) {\n\t\t\treturn new String(_");
					ret.append(name);
					ret.append(");\n\t\t}\n\t\telse {\n\t\t\treturn null;\n\t\t}\n\t}\n");
					ret.append("\tpublic void set");
					ret.append(name);
					ret.append("(String newValue) {\n\t\t");
					ret.append("_");
					ret.append(name);
					ret.append(" = newValue!=null ? newValue.toCharArray() : null;\n\t}\n");
				} else {
					ret.append("\tpublic ");
					ret.append(type);
					ret.append(" get");
					ret.append(name);
					ret.append("() {\n\t\treturn ");
					ret.append("_");
					ret.append(name);
					ret.append(";\n\t}\n");
					ret.append("\tpublic void set");
					ret.append(name);
					ret.append("(");
					ret.append(type);
					ret.append(" newValue) {\n\t\t");
					ret.append("_");
					ret.append(name);
					ret.append(" = newValue;\n\t}\n");
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateProperty(StringBuffer ret) {
		try {
			String name = super.properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String type = convertType(super.properties.getProperty("type",
						"char[]"), false);
				type = NameMangler.encode(type);
				ret.append("\tprivate ");
				String defaultValue = getMemberDefault(type);
				ret.append(type);
				ret.append(" _");
				ret.append(name);
				
				if (defaultValue != null && defaultValue.length()>0) {
					ret.append(" = ");
					ret.append(defaultValue);
					if (type.equals("char[]"))
						ret.append(".toCharArray()");
				}
				ret.append(";\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private final String getMemberDefault(String type) {
		StringBuffer ret = new StringBuffer();
		String defaultValue = super.properties.getProperty("default");
		if (defaultValue != null) {
			ret.append(defaultValue);
			if (type.equals("char[]") || type.equals("char")) {
				ret.append("\"\"");
			}
			else if (type.equals("boolean") && defaultValue!=null) {
				if(defaultValue.equalsIgnoreCase("1")) {
					ret.append("true");
				}
				else {
					ret.append(defaultValue);
				}
			}
		}
		
		return ret.toString();
	}

	public final String convertType() {
		String inType = super.properties.getProperty("type");
		return convertType(inType, true);
	}

	public final String convertType(boolean b) {
		String inType = super.properties.getProperty("type");
		return convertType(inType, b);
	}

	public static final String convertType(String inType) {
		return convertType(inType, true);
	}

	public static final String convertType(String inType, boolean b) {
		String outType = null;
		try {
			if (inType != null) {
				int colon = inType.indexOf(':');
				String type = null;
				if (colon > -1)
					type = inType.substring(colon + 1);
				else
					type = inType;
				outType = (String) dataTypes.get(type);
				if (outType != null && b && outType.equals("char[]"))
					outType = "String";
			}
			if (outType == null)
				if (b)
					outType = "String";
				else
					outType = "char[]";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outType;
	}

	public static final void insertType(String simpleType, String xmlType) {
		try {
			dataTypes.put(xmlType, convertType(xmlType));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static final boolean isString(String type) {
		return type.equals("char[]");
	}

	public final void generateWrite(StringBuffer ret) {
		try {
			String name = properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String inType = convertType(super.properties.getProperty("type","char[]"), false);
				String type = writeMethods.get(inType); 
				ret.append("\t\t");
				if(type==null) {
					ret.append("out.writeObject(");
				}
				else {
					ret.append(type);
				}
				ret.append("_");
				ret.append(name);
				ret.append(");\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateRead(StringBuffer ret) {
		try {
			String name = properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String inType = convertType(super.properties.getProperty("type","char[]"), false);
				String type = readMethods.get(inType); 
				ret.append("\t\t_");
				ret.append(name);
				ret.append(" = ");
				if(type==null) {
					ret.append("(");
					ret.append(inType);
					ret.append(")");
					ret.append("in.readObject(");
				}
				else {
					ret.append(type);
				}
				ret.append(");\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public final void generateCacheWrite(StringBuffer ret) {
		try {
			String name = properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String inType = convertType(super.properties.getProperty("type","char[]"), false);
				String type = cacheTypes.get(inType); 
				ret.append("\t\t");
				if(type==null) {
					type = "DataItem";
				}
				ret.append("item.put");
				ret.append(type);
				ret.append("(_");
				ret.append(name);
				ret.append(");\n");				
				ret.append("\t\tda.setDataItem(sa_idx++, item);\n");							
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateCacheRead(StringBuffer ret) {
		try {
			String name = properties.getProperty("name");
			if (name != null) {
				int start = name.indexOf(':');
				if (start > -1)
					name = name.substring(start + 1);
				name = NameMangler.encode(name);
				String inType = convertType(super.properties.getProperty("type","char[]"), false);
				String type = cacheTypes.get(inType); 
				ret.append("\t\tin.getDataItem(sa_idx++, item);\n");
				ret.append("\t\t_");
				ret.append(name);
				ret.append(" = item.get");
				if(type==null) {
					ret.append("DataItem");
				}
				else {
					ret.append(type);
				}
				ret.append("();\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static HashMap<String, String> dataTypes;
	protected static HashMap<String, String> writeMethods;
	protected static HashMap<String, String> readMethods;
	protected static HashMap<String, String> cacheTypes;

	static {
		dataTypes = new HashMap<String, String>();
		dataTypes.put("boolean", "boolean");
		dataTypes.put("string.ansi", "char[]");
		dataTypes.put("fixed.14.4", "float");
		dataTypes.put("float", "float");
		dataTypes.put("i4", "int");
		dataTypes.put("long", "long");
		dataTypes.put("unsignedLong", "long");
		dataTypes.put("short", "short");
		dataTypes.put("unsignedShort", "short");
		dataTypes.put("double", "double");
		dataTypes.put("date", "Date");
		dataTypes.put("dateTime", "Date");
		dataTypes.put("dateTime.iso8601", "Date");
		dataTypes.put("dateTime.iso8601.tz", "Date");
		dataTypes.put("date.iso8601", "Date");
		dataTypes.put("time", "Date");
		dataTypes.put("time.iso8601", "Date");
		dataTypes.put("time.iso8601.tz", "Date");
		dataTypes.put("number", "char[]");
		dataTypes.put("int", "int");
		dataTypes.put("integer", "int");
		dataTypes.put("i1", "byte");
		dataTypes.put("i2", "short");
		dataTypes.put("i4", "int");
		dataTypes.put("i8", "long");
		dataTypes.put("ui1", "byte");
		dataTypes.put("ui2", "short");
		dataTypes.put("ui4", "int");
		dataTypes.put("positiveInteger", "int");
		dataTypes.put("nonPositiveInteger", "int");
		dataTypes.put("negativeInteger", "int");
		dataTypes.put("nonNegativeInteger", "int");
		dataTypes.put("ui8", "long");
		dataTypes.put("r4", "float");
		dataTypes.put("r8", "double");
		dataTypes.put("decimal", "double");
		dataTypes.put("float.IEEE.754.32", "float");
		dataTypes.put("float.IEEE.754.64", "double");
		dataTypes.put("char", "char");
		dataTypes.put("bin.hex", "char[]");
		dataTypes.put("uri", "char[]");
		dataTypes.put("uuid", "char[]");
		dataTypes.put("uriReference", "char[]");

		cacheTypes = new HashMap<String, String>();
		cacheTypes.put("boolean", "Boolean");
		cacheTypes.put("char[]", "CharArray");
		cacheTypes.put("float", "Float");
		cacheTypes.put("int", "Int");
		cacheTypes.put("long", "Long");
		cacheTypes.put("short", "Short");
		cacheTypes.put("double", "Double");
		cacheTypes.put("Date", "Date");
		cacheTypes.put("byte", "Byte");
		cacheTypes.put("char", "Byte");
		
		writeMethods = new HashMap<String, String>();
		writeMethods.put("boolean", "out.writeBoolean(");
		writeMethods.put("char[]", "writeString(out, ");
		writeMethods.put("float", "out.writeFloat(");
		writeMethods.put("int", "out.writeInt(");
		writeMethods.put("long", "out.writeLong(");
		writeMethods.put("short", "out.writeShort(");
		writeMethods.put("double", "out.writeDouble(");
		writeMethods.put("Date", "writeDate(out, ");
		writeMethods.put("byte", "out.writeChar(");
		writeMethods.put("char", "out.writeChar(");
		
		readMethods = new HashMap<String, String>();
		readMethods.put("boolean", "in.readBoolean(");
		readMethods.put("char[]", "readString(in");
		readMethods.put("float", "in.readFloat(");
		readMethods.put("int", "in.readInt(");
		readMethods.put("long", "in.readLong(");
		readMethods.put("short", "in.readShort(");
		readMethods.put("double", "in.readDouble(");
		readMethods.put("Date", "readDate(in");
		readMethods.put("byte", "in.readChar(");
		readMethods.put("char", "in.readChar(");
	}
}
