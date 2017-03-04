package org.adaptinet.node.xmltools.parser;

import java.util.*;

//@SuppressWarnings("unchecked")
public class XMLDOMWriter {

	public XMLDOMWriter(boolean canonical) {
		try {
			xmlBuffer = new StringBuffer();
		} catch (java.lang.StringIndexOutOfBoundsException oobexcep) {
		}
		this.canonical = canonical;
	}

	public String writeXML(XMLDocument document) {
		String strReturn = null;
		try {
			write(document, false);
			strReturn = xmlBuffer.toString();
		} catch (Exception e) {
			System.err.println(e);
		}
		return strReturn;
	}

	private void write(XMLNode node, boolean bRootNode) {

		if (node == null) {
			return;
		}

		int type = node.getNodeType();
		switch (type) {
		case XMLNode.DOCUMENT_NODE: {
			if (canonical) {
				List<XMLNode> children = node.getChildNodes();
				if(children != null) {
					for (int i = 0; i < children.size(); i++) {
						XMLNode childNode = (XMLNode) children.get(i);
						if (childNode.getNodeType() == XMLNode.PROCESSING_INSTRUCTION_NODE) {
							String name = childNode.getNodeName();
							String val = childNode.getNodeValue();
							xmlBuffer.append("<?" + name + " " + val + "?>");
						}
					}
				}
			}
			write(((XMLDocument) node).getDocumentElement(), true);
			break;
		}

		case XMLNode.ELEMENT_NODE: {
			xmlBuffer.append('<');
			xmlBuffer.append(node.getNodeName());
			XMLAttr attrs[] = sortAttributes(node.getAttributes());
			if(bRootNode) {
				xmlBuffer.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
			}
			
			for (int i = 0; i < attrs.length; i++) {
				XMLAttr attr = attrs[i];
				xmlBuffer.append(' ');
				xmlBuffer.append(attr.getNodeName());
				xmlBuffer.append("=\"");
				xmlBuffer.append(normalize(attr.getNodeValue()));
				xmlBuffer.append('"');
			}
			xmlBuffer.append('>');
			List<XMLNode> children = node.getChildNodes();
			if (children != null) {
				int len = children.size();
				for (int i = 0; i < len; i++) {
					write(children.get(i), false);
				}
			}
			break;
		}

		case XMLNode.ENTITY_REFERENCE_NODE: {
			if (canonical) {
				List<XMLNode> children = node.getChildNodes();
				if (children != null) {
					int len = children.size();
					for (int i = 0; i < len; i++) {
						write(children.get(i), false);
					}
				}
			} else {
				xmlBuffer.append('&');
				xmlBuffer.append(node.getNodeName());
				xmlBuffer.append(';');
			}
			break;
		}

		case XMLNode.CDATA_SECTION_NODE:
		case XMLNode.TEXT_NODE: {
			xmlBuffer.append(normalize(node.getNodeValue()));
			break;
		}

		case XMLNode.PROCESSING_INSTRUCTION_NODE: {
			xmlBuffer.append("<?");
			xmlBuffer.append(node.getNodeName());
			String data = node.getNodeValue();
			if (data != null && data.length() > 0) {
				xmlBuffer.append(' ');
				xmlBuffer.append(data);
			}
			xmlBuffer.append("?>");
			break;
		}
		}

		if (type == XMLNode.ELEMENT_NODE) {
			xmlBuffer.append("</");
			xmlBuffer.append(node.getNodeName());
			xmlBuffer.append('>');
		}
	}

	private String normalize(String s) {
		StringBuffer str = new StringBuffer();

		int len = (s != null) ? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '<': {
				str.append("&lt;");
				break;
			}
			case '>': {
				str.append("&gt;");
				break;
			}
			case '&': {
				str.append("&amp;");
				break;
			}
			case '"': {
				str.append("&quot;");
				break;
			}
			case '\r':
			case '\n': {
				if (canonical) {
					str.append("&#");
					str.append(Integer.toString(ch));
					str.append(';');
					break;
				}
			}
			default: {
				str.append(ch);
			}
			}
		}
		return str.toString();
	}

	private XMLAttr[] sortAttributes(List<XMLAttr>attrs) {
		int len = (attrs != null) ? attrs.size() : 0;
		XMLAttr array[] = new XMLAttr[len];
		for (int i = 0; i < len; i++) {
			array[i] = attrs.get(i);
		}

		for (int i = 0; i < len - 1; i++) {
			String name = array[i].getNodeName();
			int index = i;
			for (int j = i + 1; j < len; j++) {
				String curName = array[j].getNodeName();
				if (curName.compareTo(name) < 0) {
					name = curName;
					index = j;
				}
			}
			if (index != i) {
				XMLAttr temp = array[i];
				array[i] = array[index];
				array[index] = temp;
			}
		}

		return array;
	}

	private StringBuffer xmlBuffer;
	private boolean canonical;
}
