package org.adaptinet.node.xmltools.parser;

public class XMLDocument extends XMLNode {

	public XMLDocument() {
		setNodeType(XMLNode.DOCUMENT_NODE);
	}

	public final void appendChild(XMLNode child) {
		if (child.getNodeType() == XMLNode.ELEMENT_NODE)
			docElement = child;
		else
			super.appendChild(child);
	}

	public XMLNode getDocumentElement() {
		return docElement;
	}

	private XMLNode docElement = null;
}
