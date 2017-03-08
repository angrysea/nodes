package org.amg.node.processoragent;

import org.amg.node.exception.AMGException;
import org.amg.node.exception.ProcessorException;
import org.amg.node.loader.ClasspathLoader;
import org.amg.node.messaging.Body;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.node.NodeEntry;
import org.amg.node.registry.ProcessorEntry;
import org.amg.node.registry.ProcessorFile;
import org.amg.node.server.IServer;
import org.amg.node.server.NetworkAgent;

final class MainProcessor extends SystemProcessor {

	private NetworkAgent networkAgent = null;
	
	public void startProcessor(ProcessorEntry entry) throws Exception {
	}

	public void init(ClasspathLoader loader, ProcessorAgent agent) {
		super.init(loader, agent);
		networkAgent = (NetworkAgent) IServer.getServer().getService(
				"networkAgent");
	}

	public void error(String uri, String errorMsg) {
		AMGException exMessage = new AMGException(
				AMGException.SEVERITY_ERROR, AMGException.GEN_BASE);
		exMessage.logMessage("Error processing message : " + uri
				+ "\n Error message " + errorMsg);
	}

	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	public Object process(Envelope env) throws Exception {
		try {
			if (env.isMethod("connect")) {
				connect(env.getBody());
			} else if (env.isMethod("connected")) {
				connected(env.getBody());
			} else if (env.isMethod("isconnected")) {
				isconnected(env.getBody());
			} else if (env.isMethod("replace")) {
				replace(env.getBody());
			} else if (env.isMethod("getkey")) {
				Message message = Message.createReply(env.getHeader()
						.getMessage());
				Object args[] = new Object[1];
				args[0] = new String(IServer.getKey());
				Messenger.postMessage(message, args);
			} else if (env.isMethod("reconnect")) {
				reconnect();
			} else if (env.isMethod("insert")) {
				networkAgent.insert(env);
			} else if (env.isMethod("update")) {
				networkAgent.update(env);
			} else if (env.isMethod("remove")) {
				remove(env.getBody());
			} else if (env.isMethod("load")) {
				load(env.getBody());
			} else if (env.isMethod("nodeUpdate")) {
				nodeUpdate();
			}
			else if (env.isMethod("error")) {
				Object args[] = env.getBody().getcontentArray();
				error((String) args[0], (String) args[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ProcessorException agentex = new ProcessorException(
					AMGException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex.logMessage("Method not supported by amg Command Agent. "
							+ e.getMessage());
			throw e;
		}
		return null;
	}

	private void connect(Body body) throws Exception {

		Object args[] = body.getcontentArray();
		if (args.length != 3)
			throw new Exception("Out of Bounds");
		// Address of the node to connect
		NodeEntry entry = new NodeEntry((String) args[0]);
		// Common name for the node.
		entry.setName((String) args[1]);
		entry.setTime(Long.toString(System.currentTimeMillis()));
		networkAgent.connect(entry, ((Boolean) args[2]).booleanValue());
	}

	private void connected(Body body) throws Exception {

		Object args[] = body.getcontentArray();
		if (args.length != 2)
			throw new Exception("Out of Bounds");
		networkAgent.connected((String) args[0], (String) args[1]);
	}

	private void isconnected(Body body) throws Exception {

		Object args[] = body.getcontentArray();
		if (args.length != 2)
			throw new Exception("Out of Bounds");
		networkAgent.isconnected((String) args[0], (String) args[1]);
	}

	private void replace(Body body) throws Exception {

		Object args[] = body.getcontentArray();
		if (args.length != 2)
			throw new Exception("Out of Bounds");
		networkAgent.replace((String) args[0], (String) args[1]);
	}

	private void reconnect() throws Exception {

		networkAgent.reconnect();
	}

	private void remove(Body body) throws Exception {

		Object args[] = body.getcontentArray();
		if (args.length < 1)
			throw new Exception("Out of Bounds");
		networkAgent.remove((String) args[0]);
	}

	private void load(Body body) throws Exception {
		Object args[] = body.getcontentArray();
		if (args.length < 2)
			throw new Exception("Out of Bounds");
		// networkAgent.update();
		ProcessorAgent processor = (ProcessorAgent) IServer.getServer()
				.getAvailableProcessor((String) args[0]);
		processor.preProcess("org.amg.sdk.processoragent.ServiceProcessor");

		try {
			ProcessorFile processors = (ProcessorFile) IServer.getServer()
					.getService("processorfile");
			processor.startProcessor(processors.findEntry((String) args[1]));
		} catch (Exception e) {
		}
	}

	private void nodeUpdate() throws Exception {

		try {
			IServer.getServer().getService("processorfactory");
			// processor.startProcessor(processors.findEntry((String)args[1]));
		} catch (Exception e) {
		}
	}

	public String process(String xml) throws Exception {
		ProcessorException agentex = new ProcessorException(
				AMGException.SEVERITY_FATAL,
				ProcessorException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	public boolean useEnvelope() {
		return true;
	}
}
