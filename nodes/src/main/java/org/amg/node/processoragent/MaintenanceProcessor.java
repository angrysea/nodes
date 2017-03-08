package org.amg.node.processoragent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.amg.node.exception.AMGException;
import org.amg.node.exception.ProcessorException;
import org.amg.node.messaging.Address;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.registry.ProcessorEntry;

public final class MaintenanceProcessor extends SystemProcessor {

	static private Map<String, MaintenanceWorker> workers = Collections
			.synchronizedMap(new HashMap<String, MaintenanceWorker>(10));

	public void startProcessor(ProcessorEntry entry) throws Exception {
	}

	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	public Object process(Envelope env) throws Exception {

		try {
			if (env.isMethod("ping")) {
				ping(env.getHeader().getMessage());
			} else if (env.isMethod("pong")) {
				pong(env.getHeader().getMessage());
			}
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(
					AMGException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex.logMessage("Method not supported by amg Maintenance Agent. "
					+ e.getMessage());
			throw e;
		}
		return null;
	}

	public void ping(Message msg) {

		try {
			Message message = Message.createReply(msg);
			message.setMethod("pong");
			Messenger.postMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pong(Message msg) {

		try {
			MaintenanceWorker worker = workers.get(msg.getReplyTo().getURL());
			worker.setResponded(true);
			worker.setEndTime(System.currentTimeMillis());

			// System.out.println("Response time for node " +
			// worker.getAddress().getURL() + " is " +
			// Long.toString(worker.getPingTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String process(String xml) throws Exception {

		ProcessorException agentex = new ProcessorException(
				AMGException.SEVERITY_FATAL,
				ProcessorException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	static public void doPing(Address address) {

		MaintenanceWorker worker = new MaintenanceWorker(address);
		workers.put(address.getURL(), worker);
	}

	static public Collection<MaintenanceWorker> workers() {
		return workers.values();
	}

	static public void clear() {
		workers.clear();
	}

}
