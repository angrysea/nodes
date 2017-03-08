package org.amg.node.xmltools.xmlconverter;

import java.util.Stack;

import org.amg.node.xmltools.parser.Attributes;
import org.amg.node.xmltools.parser.DefaultHandler;
import org.amg.node.xmltools.parser.InputSource;
import org.amg.node.xmltools.parser.XMLReader;
import org.amg.node.xmltoolsex.ParserException;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;


public class XsdSchemaLoader extends DefaultHandler implements LoaderBase {

	public static final String S_NAME = "name";
	public static final String S_TYPE = "type";
	public static final String S_ATTRIBUTEGROUP = "attributeGroup";
	public static final String S_GROUP = "group";
	public static final String S_BASE = "base";
	public static final String S_SIMPLETYPE = "simpleType";
	public static final String S_RESTRICTION = "restriction";
	public static final String S_COMPLEXTYPE = "complexType";
	public static final String S_ELEMENT = "element";
	public static final String S_MINOCCURS = "minOccurs";
	public static final String S_MAXOCCURS = "maxOccurs";
	public static final String S_REF = "ref";
	public static final String S_ATTRIBUTE = "attribute";
	public static final String S_SEQUENCE = "sequence";
	public static final String S_BASETYPE = "baseType";
	public static final String S_CHOICE = "choice";
	private HashMap<String, XMLElementSerializer> attributeGroups = new HashMap<String, XMLElementSerializer>();
	private HashMap<String, XMLElementSerializer> groups = new HashMap<String, XMLElementSerializer>();
	private HashMap<String, XMLElementSerializer> simpleTypes = new HashMap<String, XMLElementSerializer>();
	private HashMap<String, XMLElementSerializer> complexTypes = new HashMap<String, XMLElementSerializer>();
	private HashMap<String, XMLElementSerializer> elements = new HashMap<String, XMLElementSerializer>();
	private HashMap<String, ElementType> elementTypes = new HashMap<String, ElementType>();
	private Collection<ElementType> c;
	private Iterator<ElementType> it;
	private XMLElementSerializer currentElement;
	private XMLElementSerializer currentParent;
	private Stack<XMLElementSerializer> elementStack;
	private Stack<XMLElementSerializer> parentStack;
	private boolean bIgnoreCharacters;

	public XsdSchemaLoader() {
		c = null;
		it = null;
		currentElement = null;
		currentParent = null;
		elementStack = new Stack<XMLElementSerializer>();
		parentStack = new Stack<XMLElementSerializer>();
		bIgnoreCharacters = true;
	}

	public void xmlLoad(InputStream is) throws Exception, Exception {
		try {
			XMLReader parser = new XMLReader();
			parser.setContentHandler(this);
			parser.setErrorHandler(this);
			parser.parse(new InputSource(is));
			processTree();
		} catch (ParserException err) {
			throw err;
		} catch (Exception e) {
			throw e;
		}
	}

	public void xmlLoad(String data) throws Exception, Exception {
		try {
			XMLReader parser = new XMLReader();

			parser.setContentHandler(this);
			parser.setErrorHandler(this);
			parser.parse(new InputSource(new ByteArrayInputStream(data
					.getBytes())));
			processTree();
		} catch (ParserException err) {
			throw err;
		} catch (Exception e) {
			throw e;
		}
	}

	void processTree() {
		currentElement.update(attributeGroups, "attributeGroup");
		currentElement.update(groups, "group");
		currentElement.update(simpleTypes, "simpleType", false);
		Iterator<XMLElementSerializer> iterator = simpleTypes.values()
				.iterator();
		do {
			if (!iterator.hasNext())
				break;
			XMLElementSerializer e = (XMLElementSerializer) iterator.next();
			String name = e.getProperty("name");
			if (name != null) {
				String type = e.getProperty("base");
				AttributeType.insertType(name, type);
			}
		} while (true);
		currentElement.update(complexTypes, "complexType");
		currentElement.update(elements, "element");
		String name;
		ElementType elementType;
		for (iterator = complexTypes.values().iterator(); iterator.hasNext(); elementTypes
				.put(name, elementType)) {
			XMLElementSerializer e = (XMLElementSerializer) iterator.next();
			name = e.getProperty("name");
			elementType = new ElementType();
			elementType.putAll(e);
			enumerateChildren(e, elementType, -1, -1, null);
		}

		iterator = elements.values().iterator();
		do {
			if (!iterator.hasNext())
				break;
			XMLElementSerializer e = (XMLElementSerializer) iterator.next();
			name = e.getProperty("name");
			String type = e.getProperty("type");
			if (type == null) {
				elementType = new ElementType();
				elementType.putAll(e);
				enumerateChildren(e, elementType, -1, -1, null);
				elementTypes.put(name, elementType);
			}
		} while (true);
	}

	void enumerateChildren(XMLElementSerializer e, ElementType elementType,
			int minOccurs, int maxOccurs, XMLElementSerializer inner) {

		Enumeration<XMLElementSerializer> aEnum = e.children();
		if (aEnum != null)
			do {
				if (!aEnum.hasMoreElements())
					break;
				XMLElementSerializer child = aEnum.nextElement();
				String type = child.getProperty("type");
				if (child.getName().equalsIgnoreCase("complexType")) {
					elementType.putAll(child);
					enumerateChildren(child, elementType, -1, -1, inner);
				} else if (child.getName().equalsIgnoreCase("element")) {
					boolean enumChildren = true;
					if (type == null) {
						String ref = child.getProperty("ref");
						if (ref != null) {
							XMLElementSerializer refchild = (XMLElementSerializer) elements
									.get(ref);
							if (refchild != null) {
								child.setProperty("name", refchild
										.getProperty("name"));
								if ((type = refchild.getProperty("type")) == null)
									type = ref;
							}
							enumChildren = false;
						}
					}
					if (enumChildren) {
						enumerateChildren(child, elementType, -1, -1, inner);
						if (type == null)
							type = child.getProperty("type");
					}
					if (type != null) {
						int colon = type.indexOf(':');
						if (colon > -1)
							type = ElementType.convertType(type
									.substring(colon + 1));
						ElementData element = new ElementData(child
								.getProperty("name"), type);
						element.putAll(child);
						if (minOccurs > -1) {
							String property = element.getProperty("minOccurs");
							if (property == null)
								element.setProperty("minOccurs", Integer
										.toString(minOccurs));
						}
						if (maxOccurs > 0) {
							String property = element.getProperty("maxOccurs");
							if (property == null)
								element.setProperty("maxOccurs", Integer
										.toString(maxOccurs));
						}
						elementType.putElement(element);
					}
				} else if (child.getName().equalsIgnoreCase("attribute")) {
					if (type != null) {
						AttributeType attribute = new AttributeType();
						attribute.putAll(child);
						int loc = 0;
						if (type.indexOf(":") > 0)
							attribute.setProperty("type", type.substring(loc));
						else
							attribute.setProperty("type", type);
						elementType.putAttribute(attribute);
					}
				} else if (child.getName().equalsIgnoreCase("simpleType")) {
					String name = child.getProperty("name");
					if (name == null)
						elementType.putAll(child);
					enumerateChildren(child, elementType, minOccurs, maxOccurs,
							e);
				} else if (child.getName().equalsIgnoreCase("attributeGroup")) {
					String ref = child.getProperty("ref");
					if (ref != null) {
						XMLElementSerializer attributeGroup = (XMLElementSerializer) attributeGroups
								.get(ref);
						enumerateChildren(attributeGroup, elementType, -1, -1,
								inner);
					}
				} else if (child.getName().equalsIgnoreCase("group")) {
					String ref = child.getProperty("ref");
					if (ref != null) {
						XMLElementSerializer group = (XMLElementSerializer) groups
								.get(ref);
						enumerateChildren(group, elementType, -1, -1, inner);
					}
				} else if (child.getName().equalsIgnoreCase("sequence")) {
					int min = -1;
					int max = 1;
					String property = child.getProperty("minOccurs");
					if (property != null)
						min = Integer.parseInt(property);
					property = child.getProperty("maxOccurs");
					if (property != null)
						if (property.equals("unbounded"))
							max = 0x7fffffff;
						else
							max = Integer.parseInt(property);
					enumerateChildren(child, elementType, min, max, inner);
				} else if (child.getName().equalsIgnoreCase("restriction")) {
					type = child.getProperty("base");
					if (type != null && inner != null)
						inner.setProperty("type", type);
				} else if (child.getName().equalsIgnoreCase("choice")) {
					int min = minOccurs;
					int max = maxOccurs;
					if (min < 0)
						min = 0;
					if (max < 0)
						max = 1;
					enumerateChildren(child, elementType, min, max, inner);
				} else {
					enumerateChildren(child, elementType, -1, -1, inner);
				}
			} while (true);
	}

	public void startElement(String uri, String tag, String raw,
			Attributes attrs) throws ParserException {
		bIgnoreCharacters = false;
		if (currentElement != null) {
			currentParent = currentElement;
			parentStack.push(currentElement);
		}
		currentElement = new XMLElementSerializer(tag, currentParent);
		int len = attrs.getLength();
		for (int i = 0; i < len; i++)
			currentElement
					.setProperty(attrs.getLocalName(i), attrs.getValue(i));

		elementStack.push(currentElement);
	}

	public void endElement(String uri, String tag, String raw)
			throws ParserException {
		bIgnoreCharacters = true;
		if (!parentStack.isEmpty()) {
			parentStack.pop();
			if (!parentStack.isEmpty())
				currentParent = (XMLElementSerializer) parentStack.peek();
		}
		if (!elementStack.isEmpty()) {
			elementStack.pop();
			if (!elementStack.isEmpty())
				currentElement = (XMLElementSerializer) elementStack.peek();
		}
	}

	public void characters(char buf[], int offset, int len)
			throws ParserException {
		if (!bIgnoreCharacters && len > 0)
			currentElement.setData(buf, offset, len);
	}

	public void ignorableWhitespace(char ac[], int i, int j)
			throws ParserException {
	}

	public void comment(String s) {
	}

	public void endCDATA() {
	}

	public void endParsedEntity(String s, boolean flag) {
	}

	public void startCDATA() {
	}

	public void startPrefixMapping(String s, String s1) throws ParserException {
	}

	public void endPrefixMapping(String s) throws ParserException {
	}

	public void startParsedEntity(String s) {
	}

	public void error(ParserException e) throws ParserException {
		System.out.println("   ".concat(String.valueOf(String.valueOf(e
				.getMessage()))));
		throw e;
	}

	public void warning(ParserException e) throws ParserException {
		System.out.println("   ".concat(String.valueOf(String.valueOf(e
				.getMessage()))));
		throw e;
	}

	@SuppressWarnings("unused")
	private String getValueFromString(String str, String name) {
		String value = null;
		int start = str.indexOf(name);
		if (start != -1
				&& ((start = str.indexOf("'", start)) != -1 || (start = str
						.indexOf('"', start)) != -1)) {
			start++;
			int end;
			if ((end = str.indexOf("'", start)) != -1
					|| (end = str.indexOf('"', start)) != -1)
				value = str.substring(start, end);
		}
		return value;
	}

	public ElementType getElementType(String type) {
		return (ElementType) elementTypes.get(type);
	}

	public Collection<ElementType> getElementTypes() {
		return elementTypes.values();
	}

	public XMLElementSerializer getSimpleType(String type) {
		return (XMLElementSerializer) simpleTypes.get(type);
	}

	public int start() {
		c = elementTypes.values();
		it = c.iterator();
		return elementTypes.size();
	}

	public void end() {
		c = null;
		it = null;
	}

	public String next(boolean externalizable, boolean cachable) {
		String classData = null;
		while (it.hasNext()) {
			ElementType e = (ElementType) it.next();
			if (e != null) {
				classData = e.generateCode(externalizable, cachable);
				if (classData != null)
					return classData;
			}
		}
		return classData;
	}

	public void dump(OutputStream stream, boolean externalizable) throws IOException {
		Collection<ElementType> c = elementTypes.values();
		Iterator<ElementType> it = c.iterator();
		do {
			if (!it.hasNext())
				break;
			ElementType e = (ElementType) it.next();
			if (e != null) {
				String classData = e.generateCode(externalizable);
				if (classData != null)
					stream.write(classData.getBytes());
			}
		} while (true);
	}
}
