package org.adaptinet.node.processoragent;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Envelope;
import org.adaptinet.node.registry.ProcessorEntry;
import org.adaptinet.node.registry.ProcessorFile;
import org.adaptinet.node.server.IServer;

class ServiceProcessor extends ProcessorBase {

	protected ProcessorMap processorMap = null;

	final public Object process(Envelope env) throws Exception {
		
		Object ret = null;
		try {
			if (processorMap == null) {
				startProcessor(((ProcessorFile) IServer.getServer()
						.getService("processorfile")).findEntry(env.getHeader()
						.getMessage().getAddress().getProcessor()));
			}
			processorMap.setCurrentMessage(env.getHeader().getMessage());
			Object args[] = env.getBody().getcontentArray();
			String method = env.getHeader().getMessage().getAddress()
					.getMethod();
			ret = processorMap.executeMethod(method, true, args);
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS, e.getMessage());
			throw agentex;
		}
		return ret;
	}

	public Object execute(String methodName, Object request) throws Exception {
		Object ret = null;
		try {
			if (processorMap == null) {
				throw new Exception("Unknow error no Map");
			}
			ret = processorMap.executeMethod(methodName, true, request);
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS, e.getMessage());
			throw agentex;
		}
		return ret;
	}	

	public final boolean preProcessMessage(Envelope env) {
		boolean bRet = false;
		try {
			if (processorMap == null)
				bRet = true;
			else
				bRet = processorMap.preProcessMessage(env);
		} catch (Exception ex) {
		}
		return bRet;
	}

	final public String process(String xml) throws Exception {
		ProcessorException agentex = new ProcessorException(
				AdaptinetException.SEVERITY_FATAL,
				ProcessorException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	public final void startProcessor(ProcessorEntry entry) throws Exception {
		try {
			if (processorMap == null) {
				createProcessorMap();
			}
			processorMap.createInstance(entry.getType(), loader);
			processorMap.executeMethod("init", true, (Object [])null);
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex
					.logMessage("End document Exception error performing do transaction. "
							+ e.getMessage());
			throw e;
		}
	}

	public final void cleanupProcessor() {
		try {
			if (processorMap != null) {
				processorMap.executeMethod("cleanup", true, (Object [])null);
			}
		} catch (Exception e) {
			ProcessorException agentex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_OBJDOTRANS);
			agentex
					.logMessage("End document Exception error performing do transaction. "
							+ e.getMessage());
		}
	}

	void createProcessorMap() {
		processorMap = new ProcessorMap();
	}
}
