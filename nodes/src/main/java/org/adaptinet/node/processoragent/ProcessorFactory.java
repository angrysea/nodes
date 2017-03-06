package org.adaptinet.node.processoragent;

import java.util.Collections;
import java.util.Map;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Messenger;
import org.adaptinet.node.registry.ProcessorEntry;
import org.adaptinet.node.registry.ProcessorFile;
import org.adaptinet.node.server.IServer;
import org.adaptinet.node.serverutils.CachedThread;
import org.adaptinet.node.serverutils.ThreadCache;

import java.util.HashMap;
import java.util.Iterator;


public class ProcessorFactory implements Runnable {

	static public final String MAIN = "main";
	static public final String MAINCLASS = "org.adaptinet.node.processoragent.MainProcessor";
	static public final String MAINTENANCE = "maintenance";
	static public final String MAINTENANCECLASS = "org.adaptinet.node.processoragent.MaintenanceProcessor";
	static public final String SERVICE = "service";
	static public final String SERVICECLASS = "org.adaptinet.node.processoragent.ServiceProcessor";
	static public final String CACHE = "cache";
	static public final String CACHECLASS = "org.adaptinet.node.cache.CacheProcessor";
	static public final String CONSOLE = "Console";
	static public final String CONSOLECLASS = "org.adaptinet.node.processors.Console";

	private ThreadCache threadcache = null;
	private Map<String, ProcessorState> processors = null;
	private IServer server = null;
	private boolean verbose = false;
	private ProcessorFile processorFile = null;
	@SuppressWarnings("unused")
	private boolean running = true;
	@SuppressWarnings("unused")
	private String classpath = null;

	public ProcessorFactory(String classpath, boolean v) {
		verbose = v;
		this.classpath = classpath;
		processors = Collections.synchronizedMap(new HashMap<String, ProcessorState>());
		try {
			Class.forName("org.adaptinet.sdk.processoragent.Processor");
		} catch (Exception e) {
		}
	}

	protected synchronized void deleteClient(ProcessorState state) {
		processors.remove(state);
	}

	protected synchronized ProcessorState addProcessor(String name) {
		ProcessorState state = null;

		try {
			CachedThread t = threadcache.getThread(true);
			ProcessorEntry entry = null;
			ProcessorAgent processor = new ProcessorAgent(this, t, verbose);
			processor.setName(name);
			state = processor.getState();
			if ((entry = processorFile.findEntry(name)) != null) {
				state.appendClasspath(entry.getClasspath());
				processor.setEntry(entry);
			}
			state.setStatus(ProcessorState.FREE);
			processors.put(name, state);
		} catch (Exception e) {
			state = null;
		}

		return state;
	}

	public void initialize(IServer theServer, int nprocessors) {
		server = theServer;
		processorFile = (ProcessorFile) server.getService("processorfile");
		threadcache = new ThreadCache("processors", nprocessors, server
				.getClientThreadPriority(), 0);
	}

	public void run() {
		while (true) {
			if (Thread.interrupted() == true) {
				return;
			}
			
			killTimedoutProcessors();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	synchronized public void postMessage(String method, Object args[]) {

		Iterator<ProcessorState> it = processors.values().iterator();
		while (it.hasNext()) {
			ProcessorState state = it.next();
			Messenger.localPostMessage(state.getProcessorAgent(), method, args);
		}
	}

	synchronized protected void killTimedoutProcessors() {
		try {
			Iterator<ProcessorState> it = processors.values().iterator();

			while (it.hasNext()) {
				stopThisProcessor(it.next());
			}
		} catch (Exception e) {
		}
	}

	public void nodeUpdate() {
		Iterator<ProcessorState> it = processors.values().iterator();
		while (it.hasNext()) {
			/**
			 * Will need to look into this for now just suppress the warning
			 */
			@SuppressWarnings("unused")
			ProcessorState state = it.next();
		}
	}

	final public boolean killProcessor(String name, boolean force) {
		return stopProcessor(name);
	}

	final public boolean stopProcessor(String id) {
		boolean bStop = true;
		ProcessorState state = processors.get(id);
		if (state != null) {
			bStop = false;
		}

		while (bStop == false) {
			bStop = stopThisProcessor(state);
			try {
				Thread.sleep(state.getTimeOut());
			} catch (InterruptedException e) {
				bStop = true;
				processors.remove(state);
			}
		}

		return !bStop;
	}

	final private boolean stopThisProcessor(ProcessorState state) {
		boolean bDead = false;

		try {
			switch (state.getStatus()) {
			case ProcessorState.BUSY:
				if (state.getProcessorAgent().isAlive() == false) {
					processors.remove(state);
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_WARNING,
							ProcessorException.ANT_TRANCEIVERTIMEOUT4);
					processorex.logMessage(state.getName());
				} else if (state.isTimedOut()) {
					state.setStatus(ProcessorState.SIGNALED);
					state.startTimer();
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_WARNING,
							ProcessorException.ANT_TRANCEIVERTIMEOUT1);
					processorex.logMessage(state.getName());
				}
				break;

			case ProcessorState.SIGNALED:
				if (state.isTimedOut()) {
					state.setStatus(ProcessorState.INTERRUPTED);
					state.getProcessorAgent().interrupt();
					state.startTimer();
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_WARNING,
							ProcessorException.ANT_TRANCEIVERTIMEOUT2);
					processorex.logMessage(state.getName());
				}
				break;

			case ProcessorState.INTERRUPTED:
				if (state.isTimedOut()) {
					state.kill(true);
					processors.remove(state);
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_WARNING,
							ProcessorException.ANT_TRANCEIVERTIMEOUT3);
					processorex.logMessage(state.getName());
				}
				break;

			case ProcessorState.IDLE:
			case ProcessorState.FREE:
			case ProcessorState.KILL:
			default:
				bDead = true;
				break;
			}
		} catch (Exception e) {
		}
		return bDead;
	}

	public void run(ProcessorAgent processor) {
		processor.wakeup();
	}

	public void cleanupProcessor() {
		Iterator<ProcessorState> it = processors.values().iterator();
		while (it.hasNext()) {
			ProcessorState state = it.next();
			state.getProcessorAgent().cleanupProcessor();
		}
	}

	public void cleanupProcessor(ProcessorAgent processor) {
		processor.cleanupProcessor();
	}

	synchronized protected void notifyIdle(ProcessorAgent processor) {
		ProcessorState state = processor.getState();
		state.setStatus(ProcessorState.IDLE);
	}

	protected void processorFinished(ProcessorAgent processor) {
		ProcessorState state = processor.getState();
		if (state.getStatus() != ProcessorState.FREE) {
			state.reset();
			state.setStatus(ProcessorState.FREE);
			processors.remove(state);
		}
	}

	public ProcessorAgent getAvailableProcessor() {
		return getAvailableProcessor(null);
	}

	public synchronized ProcessorAgent getAvailableProcessor(String name) {
		
		ProcessorAgent processor = null;
		ProcessorState state = null;
		try {
			if (name == null) {
				name = MAIN;
			}
			if (name != null) {
				synchronized (processors) {
					state = processors.get(name);
					if (state != null) {
						processor = state.getProcessorAgent();
					}
				}
			}
		} catch (Exception e) {
			state = null;
		}

		try {
			if (state == null) {
				state = addProcessor(name);
				processor = state.getProcessorAgent();
			}

			if (processor == null) {
				ProcessorException processorex = new ProcessorException(
						AdaptinetException.SEVERITY_FATAL,
						ProcessorException.ANT_PROCESSORERROR);
				processorex
						.logMessage("Exception thrown in processorFactory [getAvailableProcessor]\n\tNo available processors\n\tProcessor Name : "
								+ name);
			} else {
				state.setStatus(ProcessorState.BUSY);
			}
		} catch (Exception e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_PROCESSORERROR);
			processorex
					.logMessage("Exception thrown in processorFactory [getAvailableProcessor]\n\tProcessor Name : "
							+ name
							+ "\n\terror = "
							+ e.toString()
							+ " "
							+ e.getMessage());
		}
		return processor;
	}

	public void shutdown(boolean force) {
		running = false;
		Iterator<ProcessorState> it = processors.values().iterator();
		for (ProcessorState state = it.next(); it.hasNext(); state = it.next()) {
			state.kill(force);
		}

		processors = null;
		server = null;
	}

	public Map<String, ProcessorState> getProcessorAgents() {
		return processors;
	}

	public IServer getServer() {
		return server;
	}
}
