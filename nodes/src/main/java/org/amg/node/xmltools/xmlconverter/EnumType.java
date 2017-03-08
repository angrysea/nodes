package org.amg.node.xmltools.xmlconverter;

import java.util.*;

import org.amg.node.xmltools.xmlutils.NameMangler;


public class EnumType extends XmlBase {

	private static HashMap<String, String> dataTypes = new HashMap<String, String>();
	private ArrayList<String> enumvalues;
	private String ret;

	public EnumType() {
		enumvalues = new ArrayList<String>();
		ret = null;
	}

	public final void putEnumvalues(String value) {
		try {
			if (value != null)
				enumvalues.add(value);
		} catch (Exception exception) {
		}
	}

	public final Iterator<String> getEnums() {
		return enumvalues.iterator();
	}

	public final String generateCode() {
		return generateCode(true);
	}

	public final String generateCode(boolean bUseJava2) {

		String base = null;
		try {
			String name = super.properties.getProperty("name");
			if (name != null) {
				int size = 0;
				name = NameMangler.encode(name);
				base = getProperty("base");
				base = NameMangler.encode(base);
				ret = new String();
				ret = String.valueOf(String.valueOf(ret)).concat(
						"import java.io.Serializable;\n");
				ret = String.valueOf(String.valueOf(ret)).concat(
						"\nimport java.util.*;\n");
				ret = String.valueOf(String.valueOf(ret)).concat(
						"\npublic class ");
				ret = String.valueOf(ret) + String.valueOf(name);
				ret = String.valueOf(String.valueOf(ret)).concat(
						" implements Serializable ");
				if (base != null) {
					ret = String.valueOf(String.valueOf(ret)).concat(
							", extends ");
					ret = String.valueOf(ret) + String.valueOf(base);
				}
				ret = String.valueOf(String.valueOf(ret)).concat(" {\n");
				size = enumvalues.size();
				if (size > 0) {
					ret = String.valueOf(String.valueOf(ret)).concat(
							"\tstatic {\n");
					for (int i = 0; i < size; i++) {
						String v = (String) enumvalues.get(i);
						if (v != null)
							ret = String.valueOf(ret)
									+ String.valueOf(String.valueOf(String
											.valueOf((new StringBuffer(
													"\tvalidValues.add("))
													.append(name)
													.append(");\n"))));
					}

					ret = String.valueOf(String.valueOf(ret)).concat("\t}\n");
				}
				ret = String
						.valueOf(String.valueOf(ret))
						.concat(
								"\tpublic String getContentData() {\n\t\treturn _contentData;\n\t}\n");
				ret = String
						.valueOf(String.valueOf(ret))
						.concat(
								"\tpublic void setContentData(String newValue) {\n\t\t_contentData = newValue;\n\t}\n");
				ret = String.valueOf(ret)
						+ String
								.valueOf(bUseJava2 ? "\tprivate " : "\tpublic ");
				ret = String.valueOf(String.valueOf(ret)).concat(
						"String _contentData;\n");
				ret = String.valueOf(String.valueOf(ret)).concat("}\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return ret;
	}

	public final String getName() {
		return super.properties.getProperty("name");
	}

	public static final void insertType(String simpleType, String xmlType) {
		try {
			dataTypes.put(xmlType, xmlType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
