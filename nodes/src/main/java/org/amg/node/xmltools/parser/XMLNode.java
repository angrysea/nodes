package org.amg.node.xmltools.parser;

import java.util.*;

//@SuppressWarnings("unchecked")
public class XMLNode {

	public static final int DOCUMENT_NODE = 0;
	public static final int ELEMENT_NODE = 1;
	public static final int PROCESSING_INSTRUCTION_NODE = 2;
	public static final int CDATA_SECTION_NODE = 3;
	public static final int TEXT_NODE = 4;
	public static final int ENTITY_REFERENCE_NODE = 5;

	protected XMLNode() {
	}

	protected XMLNode(String name, int type) {
		this.type = type;
		this.name = name;
	}

	protected XMLNode(int type) {
		this.type = type;
	}

	public final int getNodeType() {
		return type;
	}

	public final void setNodeType(int type) {
		this.type = type;
	}

	public final String getNodeName() {
		return name;
	}

	public final String getNodeValue() {
		return value;
	}

	public final void setNodeValue(String value) {
		this.value = value;
	}

	public void appendChild(XMLNode child) {

		if (children == null) {
			children = new Vector<XMLNode>();
		}
		children.add(child);
	}

	public List<XMLNode> getElementsByTagName(String tag) {
		XMLNode node = null;
		List<XMLNode> byname = new Vector<XMLNode>();
		Iterator<XMLNode> it = children.iterator();
		while (it.hasNext()) {
			node = it.next();
			if (node.getNodeName().equals(tag))
				byname.add(node);
		}
		return byname;
	}

	public final List<XMLNode> getChildNodes() {
		return children;
	}

	public XMLNode getFirstChild() {
		return (XMLNode) children.get(1);
	}

	public final void setAttributeNode(XMLAttr attr) {
		if (attributes == null)
			attributes = new Vector<XMLAttr>();
		attributes.add(attr);
	}

	public final void setAttribute(String Name, String value) {
		XMLAttr xmlattr = createAttribute(name);
		XMLNode text = createTextNode(value);
		xmlattr.appendChild(text);
		setAttributeNode(xmlattr);
	}

	public List<XMLAttr> getAttributes() {
		return attributes;
	}

	public String getAttribute(String name) {
		XMLAttr attr = null;
		Iterator<XMLAttr> it = attributes.iterator();
		while (it.hasNext()) {
			attr = (XMLAttr) it.next();
			if (attr.getNodeName().equals(name))
				return attr.getNodeValue();
		}
		return null;
	}

	public final XMLNode createProcessingInstruction(String name, String value) {
		XMLNode node = new XMLNode(name, PROCESSING_INSTRUCTION_NODE);
		node.setNodeValue(value);
		return node;
	}

	public final XMLNode createElement(String name) {
		return new XMLNode(name, ELEMENT_NODE);
	}

	public final XMLNode createTextNode(String text) {
		XMLNode node = new XMLNode(TEXT_NODE);
		node.setNodeValue(text);
		return node;
	}

	public final static XMLAttr createAttribute(String name) {
		return new XMLAttr(name);
	}

	private int type = -1;
	private String name = null;
	private String value = null;
	private List<XMLAttr> attributes = null;
	private List<XMLNode> children = null;
}
