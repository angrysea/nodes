package org.amg.node.xmltools.parser;

import java.util.*;

import org.amg.node.xmltools.xmlutils.*;
import org.amg.node.xmltoolsex.*;

import java.io.*;


//@SuppressWarnings("unchecked")
public class XMLDOMParser extends org.amg.node.xmltools.parser.DefaultHandler {

	private XMLDocument document = null;
	private XMLNode currentElement = null;
	private XMLNode currentParent = null;
	private Stack<XMLNode> elementStack = new Stack<XMLNode>();
	private Stack<XMLNode> parentStack = new Stack<XMLNode>();
	private boolean bIgnoreCharacters = true;
	protected Exception lastError = null;
	static final public String DEFAULT_PARSER_NAME = "org.amg.sdk.xmltools.parser.XMLReader";

	public XMLDOMParser() {
	}

	public void parse(String in) throws Exception {
		parse(new ByteArrayInputStream(in.getBytes()));
	}

	public void parse(InputStream is) throws Exception {

		parse(new InputSource(is));
	}

	public void parse(InputSource is) throws Exception {

		try {
			XMLReader parser = (XMLReader) Class.forName(DEFAULT_PARSER_NAME)
					.newInstance();
			parser.setContentHandler(this);
			parser.setErrorHandler(this);
			parser.parse(is);
		} catch (Exception e) {
			lastError = e;
			e.printStackTrace();
			throw e;
		}
	}

	public XMLDocument getDocument() {
		return document;
	}

	public void processingInstruction(String target, String pi)
			throws ParserException {
	}

	public void startDocument() throws ParserException {
		document = new XMLDocument();
	}

	public void endDocument() throws ParserException {
		document.appendChild(currentElement);
	}

	//@SuppressWarnings("static-access")
	public void startElement(String uri, String tag, String raw,
			Attributes attrs) throws ParserException {

		bIgnoreCharacters = false;
		try {
			if (currentElement != null) {
				currentParent = currentElement;
				parentStack.push(currentElement);
			}
			currentElement = document.createElement(NameMangler.encode(tag));
			elementStack.push(currentElement);

			int len = attrs.getLength();
			String attrName;
			for (int i = 0; i < len; i++) {
				attrName = attrs.getLocalName(i);
				attrName = NameMangler.encode(attrName);
				XMLAttr xmlattr = XMLNode.createAttribute(attrName);
				XMLNode text = document.createTextNode(attrs.getValue(i));
				xmlattr.appendChild(text);
				currentElement.setAttributeNode(xmlattr);
			}
			if (currentParent != null)
				currentParent.appendChild(currentElement);
		} catch (Exception e) {
			lastError = e;
			throw new ParserException("Error in Sax event startElement. Tag = "
					+ tag);
		}
	}

	public void endElement(String uri, String tag, String raw)
			throws ParserException {
		bIgnoreCharacters = true;

		try {
			if (parentStack.isEmpty() != true) {
				parentStack.pop();
				if (parentStack.isEmpty() != true) {
					currentParent = (XMLNode) parentStack.peek();
				}
			}

			if (elementStack.isEmpty() != true) {
				elementStack.pop();
				if (elementStack.isEmpty() != true) {
					currentElement = (XMLNode) elementStack.peek();
				}
			}
		} catch (Exception e) {
			lastError = e;
			throw new ParserException("Error in Sax event endElement. Tag = "
					+ tag);
		}
	}

	public void characters(char buf[], int offset, int len)
			throws ParserException {
		try {
			if (bIgnoreCharacters == false && len > 0) {
				currentElement.setNodeValue(new String(buf, offset, len));
			}
		} catch (Exception e) {
			lastError = e;
			throw new ParserException("Error in Sax event characters.");
		}
	}

	public void ignorableWhitespace(char buf[], int offset, int len)
			throws ParserException {
	}

	public void comment(String text) {
	}

	public void endCDATA() {
	}

	public void endParsedEntity(String name, boolean included) {
	}

	public void startCDATA() {
	}

	public void startParsedEntity(String name) {
	}

	public void error(ParserException e) throws ParserException {
		System.out.println("   " + e.getMessage());
		lastError = e;
		throw e;
	}

	public void warning(ParserException e) throws ParserException {
		System.out.println("   " + e.getMessage());
		lastError = e;
		throw e;
	}

	protected XMLElementSerializer createElement(String name, String schemaURI,
			XMLElementSerializer parent) {
		return new XMLElementSerializer(name, schemaURI, parent);
	}

	protected XMLElementSerializer createElement(String name, String schemaURI,
			XMLElementSerializer parent, boolean bIsElement) {
		return new XMLElementSerializer(name, schemaURI, parent, bIsElement);
	}

	public final Exception getLastError() {
		return lastError;
	}

	public final boolean hasError() {
		return lastError != null;
	}
}
