package org.adaptinet.node.xmltools.xmlutils;

public class XMLOutputSerializer implements IXMLOutputSerializer {

	private Exception lastErrorException = null;
	private String pi = null;

	public XMLOutputSerializer(String pi) {
		this.pi = pi;
	}

	public XMLOutputSerializer() {
		this("version=\"1.0\" encoding=\"UTF-8\"");
	}

	public String get(Object o) throws Exception {
		return get(o, true, null);
	}

	public String get(Object o, boolean bXMLPI) throws Exception {
		return get(o, bXMLPI, null);
	}

	public String get(Object o, boolean bXMLPI, String agentPIAttribs)
			throws Exception {

		String ret = null;
		XMLElementDeserializer deserializer = new XMLElementDeserializer();

		try {
			ret = deserializer.get(o, pi, bXMLPI, agentPIAttribs);
		} catch (Exception e) {
			lastErrorException = e;
			throw e;
		}

		return ret;

	}

	public Exception getLastError() {
		return lastErrorException;
	}

	public boolean hasError() {
		return (lastErrorException != null);
	}
}
