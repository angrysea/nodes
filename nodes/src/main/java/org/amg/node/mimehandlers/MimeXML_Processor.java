package org.amg.node.mimehandlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.amg.node.exception.AMGException;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.MessageParser;
import org.amg.node.messaging.ResponseWriter;
import org.amg.node.processoragent.ProcessorAgent;
import org.amg.node.processoragent.ProcessorFactory;
import org.amg.node.server.IServer;
import org.amg.node.xmltools.parser.InputSource;
import org.amg.node.xmltools.parser.XMLReader;


public class MimeXML_Processor extends Mime {

	static private FixedSizeSet<Integer> messageCache = 
			new FixedSizeSet<Integer>(IServer.getServer().getMessageCacheSize());

	private ProcessorAgent processor = null;

	public MimeXML_Processor() {
	}

	public ByteArrayOutputStream process(IServer server, String xml) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Envelope env = null;

		try {
			if (bVerbose == true)
				System.out.println("=============== Incoming XML Transaction ===============\n"
								+ xml);

			XMLReader parser = new XMLReader();
			parser.setContentHandler(new MessageParser());
			env = (Envelope) parser.parse(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			
			Integer uid = env.getUID();
			if(messageCache.contains(uid)) {
				return out;
			}
			
			messageCache.add(uid);
			String name = env.getProcessor();
			processor = (ProcessorAgent) server.getAvailableProcessor(name);

			if (processor != null) {
				if (name.equals(ProcessorFactory.MAIN) == true)
					processor.preProcess(ProcessorFactory.MAINCLASS);
				else if (name.equals(ProcessorFactory.MAINTENANCE) == true)
					processor.preProcess(ProcessorFactory.MAINTENANCECLASS);
				else if (name.equals(ProcessorFactory.SERVICE) == true)
					processor.preProcess(ProcessorFactory.SERVICECLASS);

				AMGException exMessage = new AMGException(
						AMGException.SEVERITY_SUCCESS,
						AMGException.GEN_MESSAGE);
				exMessage.logMessage("Processor received Name: "
						+ processor.getName());

				if (env.isSync()) {
					System.out.println("in a sync Message");
					new ResponseWriter(out).writeResponse(processor.process(env));
				} else {
					processor.pushMessage(env);
					server.run(processor);
					try {
						out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><processor>"
									+ processor.getName()
									+ "</processor><code>0</code><desc>request accepted</desc><timestamp>"
									+ (new java.util.Date(System
											.currentTimeMillis())
											.toString()) + "</timestamp></status>")
									.getBytes());
					} catch (IOException ioe) {
					}
				}
			} else {
				AMGException exMessage = new AMGException(
						AMGException.SEVERITY_ERROR,
						AMGException.GEN_BASE);
				exMessage.logMessage("Unable to load find available processor: ");
			}

			AMGException exMessage = null;

			/**
			 * We have initiated the transcation with the original node now we
			 * can check to see if this is a broadcast message.
			 */
			int hops = env.getHopCount();
			if (hops != 0) {
				if (hops > 0) {
					hops--;
					env.getHeader().getMessage()
							.setHops(Integer.toString(hops));
				}
				networkAgent.broadcastMessage(env);
			}

			exMessage = new AMGException(
					AMGException.SEVERITY_SUCCESS,
					AMGException.GEN_MESSAGE);
			exMessage.logMessage("Processor successfully executed Name: "
					+ processor.getName());
		} catch (Exception e) {
			AMGException exMessage = new AMGException(
					AMGException.GEN_MESSAGE,
					AMGException.SEVERITY_SUCCESS);
			exMessage.logMessage("Execution failed reason: " + e.getMessage());
			try {
				out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><processor>unknown</processor><code>1</code><desc>"
								+ e.getMessage()
								+ "</desc><timestamp>"
								+ (new java.util.Date(System
										.currentTimeMillis()).toString()) + "</timestamp></status>")
								.getBytes());
			} catch (IOException ioe) {
			}
		} finally {
			processor = null;
		}

		return out;
	}

	public Object getObject() {
		return processor;
	}
}