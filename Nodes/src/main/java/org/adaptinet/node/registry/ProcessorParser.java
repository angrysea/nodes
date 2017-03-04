package org.adaptinet.node.registry;

import java.util.HashMap;

import org.adaptinet.node.xmltools.parser.Attributes;
import org.adaptinet.node.xmltools.parser.DefaultHandler;


final public class ProcessorParser extends DefaultHandler {
	
	private static final int NONE = 0;
	private static final int PROCESSORENTRY = 1;
	private static final int CLASSPATH = 2;
	private static final int PACKAGENAME = 3;
	private static final int NAME = 4;
	private static final int TYPE = 5;
	private static final int DESCRIPTION = 6;
	private static final int PRELOAD = 7;
	private ProcessorEntry processorEntry = null;
	private HashMap<String, ProcessorEntry> entries = new HashMap<String, ProcessorEntry>();
	private int state = 0;

	public ProcessorParser() {
	}

	public void startElement(String uri, String tag, String qtag,
			Attributes attrs) {
		if (tag.equals("Processor")) {
			state = PROCESSORENTRY;
			processorEntry = new ProcessorEntry();
		} else if (tag.equals("Classpath")) {
			state = CLASSPATH;
		} else if (tag.equals("PackageName")) {
			state = PACKAGENAME;
		} else if (tag.equals("Name")) {
			state = NAME;
		} else if (tag.equals("Type")) {
			state = TYPE;
		} else if (tag.equals("Preload")) {
			state = PRELOAD;
		} else if (tag.equals("Description")) {
			state = DESCRIPTION;
		}
	}

	public void characters(char buffer[], int start, int length) {
		switch (state) {
		case CLASSPATH:
			processorEntry.setClasspath(new String(buffer, start, length));
			break;

		case PACKAGENAME:
			processorEntry.setPackageName(new String(buffer, start, length));
			break;
			
		case NAME:
			processorEntry.setName(new String(buffer, start, length));
			break;

		case DESCRIPTION:
			processorEntry.setDescription(new String(buffer, start, length));
			break;

		case TYPE:
			processorEntry.setType(new String(buffer, start, length));
			break;

		case PRELOAD:
			processorEntry.setPreload(new String(buffer, start, length));
			break;

		default:
			break;
		}
		state = NONE;
	}

	public void endElement(String uri, String name, String qname) {
		if (name.equals("Processor")) {
			entries.put(processorEntry.getName(), processorEntry);
		}

	}

	HashMap<String, ProcessorEntry> getEntries() {
		return entries;
	}

}