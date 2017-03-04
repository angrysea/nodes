package org.adaptinet.node.processoragent;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.messaging.Envelope;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.messaging.Messenger;
import org.adaptinet.node.registry.ProcessorEntry;
import org.adaptinet.node.serverutils.CachedThread;
import org.adaptinet.node.serverutils.Semaphore;

public final class ProcessorAgent implements Runnable {

	private LinkedList<Envelope> messages = new LinkedList<Envelope>();
	private Envelope env = null;
	@SuppressWarnings("unused")
	private static final boolean debug = true;
	private ProcessorState state = null;
	private Exception lastError = null;
	private ProcessorBase processor = null;
	private CachedThread thread = null;
	private ProcessorFactory processorFactory = null;
	private boolean verbose = false;
	private ProcessorEntry entry = null;
	private String name = null;
	private Semaphore semaphore = new Semaphore();

	public ProcessorAgent(boolean verbose) {
		this.verbose = verbose;
		this.state = new ProcessorState(this);
	}

	public ProcessorAgent(ProcessorFactory processorFactory, CachedThread thread,
			boolean verbose) {
		this.thread = thread;
		this.verbose = verbose;
		this.processorFactory = processorFactory;
		this.state = new ProcessorState(this);
	}

	final public boolean wakeup() {
		return thread.wakeup(this);
	}

	public void run() {

		try {
			synchronized (this) {
				while ((env = peekMessage(true)) != null) {
					if (processor.preProcessMessage(env) == true)
						process();
				}
			}
		} catch (AdaptinetException ex) {
			ex.logMessage();
			lastError = ex;
		} catch (Exception e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_ERRORDURINGMETHODEXECUTION);
			processorex.logMessage(e.getMessage());
			lastError = e;
		} finally {
			semaphore.semPost();
		}

		if (verbose == true)
			System.out.println("=============== run complete ==================\n");
	}

	final public void join() {

		if (thread != null) {
			while (true) {
				try {
					thread.join();
				} catch (InterruptedException ex) {
					System.out.println(ex);
				}
			}
		}
	}

	public void reset(CachedThread t) {
		thread = t;
		reset();
	}

	public void reset() {
		entry = null;
		processor = null;
	}

	public Object process() throws AdaptinetException {

		Object ret = null;
		try {
			if (processor == null) {
				ProcessorException processorex = new ProcessorException(
						AdaptinetException.SEVERITY_FATAL,
						ProcessorException.ANT_PARSER);
				processorex.logMessage("Exception thrown processor was not preparsed");
				throw processorex;
			}

			ret = processor.process(env);

			if (processorFactory != null) {
				processorFactory.notifyIdle(this);
			}

		} catch (AdaptinetException e) {
			Message message = Message.createReply(env.getHeader().getMessage());
			message.setMethod("error");
			Object args[] = new Object[2];
			args[0] = new String(message.getAddress().getURI());
			args[1] = new String(e.getMessage());
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			if (processorFactory != null) {
				processorFactory.processorFinished(this);
			}
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_PARSER, e + ": " + e.getMessage());
			// processorex.logMessage(e+": "+e.getMessage());
			throw processorex;
		}
		return ret;
	}

	public Object process(Envelope envelope) throws AdaptinetException {

		Object ret = null;
		try {
			if (processor == null) {
				ProcessorException processorex = new ProcessorException(
						AdaptinetException.SEVERITY_FATAL,
						ProcessorException.ANT_PARSER);
				processorex.logMessage("Exception thrown processor was not preparsed");
				throw processorex;
			}
			ret = processor.process(envelope);
		} catch (Exception e) {
			if (processorFactory != null) {
				processorFactory.processorFinished(this);
			}
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_PARSER, e + ": " + e.getMessage());
			// processorex.logMessage(e+": "+e.getMessage());
			throw processorex;
		}
		return ret;
	}

	public Object execute(String methodName, Object arg) throws AdaptinetException {

		Object ret = null;
		try {
			if (processor == null) {
				ProcessorException processorex = new ProcessorException(
						AdaptinetException.SEVERITY_FATAL,
						ProcessorException.ANT_PARSER);
				processorex.logMessage("Exception thrown processor was not preparsed");
				throw processorex;
			}
			ret = processor.execute(methodName, arg);
		} catch (Exception e) {
			if (processorFactory != null) {
				processorFactory.processorFinished(this);
			}
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_PARSER, e + ": " + e.getMessage());
			// processorex.logMessage(e+": "+e.getMessage());
			throw processorex;
		}
		return ret;
	}

	public void preProcess(String className) throws AdaptinetException {

		try {
			if (processor == null) {
				processor = (ProcessorBase) Class.forName(className).newInstance();
				processor.init(state.getLoader(), this);
			}

			if (processor == null) {
				ProcessorException processorex = new ProcessorException(
						AdaptinetException.SEVERITY_FATAL,
						ProcessorException.ANT_CLASSERROR);
				processorex
						.logMessage("[preparse]Error loading Processor unable to load class "
								+ className);
				throw processorex;
			}
			if (state != null) {
				state.setTimeOut(new Integer("-1").intValue());
			}
		} catch (AdaptinetException e) {

			if (processorFactory != null) {
				processorFactory.processorFinished(this);
			}
			throw e;
		} catch (Exception e) {
			if (processorFactory != null) {
				processorFactory.processorFinished(this);
			}

			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_CLASSERROR);
			processorex.logMessage("Error loading Processor unable to load class "
					+ className + " Exception thrown during message: " + e
					+ " " + e.getMessage());
			throw processorex;
		}
	}

	public void nodeUpdate() {
	}

	public void startProcessor(ProcessorEntry entry) throws Exception {
		if(!state.isStarted()) {
			processor.startProcessor(entry);
			state.setStarted(true);
		}
	}

	public void startProcessor() throws Exception {
		if(!state.isStarted()) {
			processor.startProcessor(this.entry);
			state.setStarted(true);
		}
	}

	public boolean isStarted() {
		return state.isStarted();

	}
	
	public final Exception getLastError() {
		Exception e = lastError;
		lastError = null;
		return e;
	}

	public final boolean hasError() {
		return lastError != null;
	}

	public final ProcessorState getState() {
		return state;
	}

	public String getName() {
		return name;
	}

	public void setName(String newValue) {
		name=newValue;
	}

	public String getClasspath() {
		return entry.getClasspath();
	}

	public String getPackageName() {
		return entry.getPackageName();
	}

	public ProcessorEntry getEntry() {
		return entry;
	}

	void setEntry(ProcessorEntry newValue) {
		entry = newValue;
	}

	public void setTransactionTimeout(int i) {
		state.setTimeOut(i);
	}

	public void cleanupProcessor() {
		try {
			processor.cleanupProcessor();
		} catch (Exception e) {
		}
	}

	public void interrupt() {
		thread.notify();
	}

	public boolean kill() {
		return thread.kill();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}

	@SuppressWarnings("unused")
	private StringBuffer loadURL(String strUrl) {

		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			StringBuffer stringbuffer = new StringBuffer();

			// read the response
			int b = -1;
			while (true) {
				b = is.read();
				if (b == -1)
					break;
				stringbuffer.append((char) b);
			}
			is.close();
			return stringbuffer;
		} catch (Exception e) {
			return null;
		}
	}

	public void setEnvelope(Envelope env) {
		this.env = env;
	}

	public void pushMessage(Envelope env) {
		messages.add(env);
	}

	public Envelope getMessage() {
		try {
			while (true) {
				if (messages.size() > 0)
					return messages.removeFirst();
				else
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
			}
		} catch (NoSuchElementException e) {
		}
		return null;
	}

	public Envelope peekMessage() {
		return peekMessage(false);
	}

	public Envelope peekMessage(boolean bRemove) {

		try {
			if (bRemove)
				return messages.removeFirst();
			else
				return messages.getFirst();
		} catch (NoSuchElementException e) {
		}
		return null;
	}
}
