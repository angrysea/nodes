package org.adaptinet.node.mimehandlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.messaging.Envelope;
import org.adaptinet.node.messaging.MessageParser;
import org.adaptinet.node.messaging.ResponseWriter;
import org.adaptinet.node.processoragent.ProcessorAgent;
import org.adaptinet.node.processoragent.ProcessorFactory;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.xmltools.parser.InputSource;
import org.adaptinet.node.xmltools.parser.XMLReader;


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

				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_SUCCESS,
						AdaptinetException.GEN_MESSAGE);
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
				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_ERROR,
						AdaptinetException.GEN_BASE);
				exMessage.logMessage("Unable to load find available processor: ");
			}

			AdaptinetException exMessage = null;

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

			exMessage = new AdaptinetException(
					AdaptinetException.SEVERITY_SUCCESS,
					AdaptinetException.GEN_MESSAGE);
			exMessage.logMessage("Processor successfully executed Name: "
					+ processor.getName());
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
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