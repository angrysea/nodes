package org.amg.node.processoragent;

import java.util.Iterator;

import org.amg.node.exception.ProcessorException;
import org.amg.node.messaging.Envelope;
import org.amg.node.messaging.Message;
import org.amg.node.messaging.Messenger;
import org.amg.node.node.Node;
import org.amg.node.server.IServer;
import org.amg.node.server.NetworkAgent;

/**
 * The Processor class is an abstract class from which all client processors
 * must derive.
 */
public abstract class Processor extends ProcessorRoot {

	/**
	 * Gives a processor a chance to do one time initialization. Method is
	 * called when the processor is first loaded.
	 */
	public abstract void init();

	/**
	 * Gives a processor a chance to cleanup any outstanding resources this
	 * processor may be holding. Method is called just before it is unloaded.
	 */
	public abstract void cleanup();

	/**
	 * This method will unload this processor.
	 */
	public void unload() {
		unloadProcessor();
	}

	/**
	 * This method will shutdown the Server.
	 */
	public void shutdown() {
		shutdownServer();
	}

	/**
	 * This method allows the another entity to post an error message to the
	 * processor. The source of the message could be a local failure or from a
	 * remote request.
	 * 
	 * @param uri
	 *            This is the uri of the message when the fault was caused.
	 * @param errorMsg
	 *            This is the message indicating the particular fault.
	 */
	public void error(String uri, String errorMsg) {
		// Default do nothing up to the processor writer to override
		// System.out.println("error called to " +uri+" error message "
		// +errorMsg);
	}

	/**
	 * This method is for notification of processors that the status of the
	 * nodes has changed
	 */
	public void nodeUpdate() {
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 * @return Return value
	 */
	public final Object sendMessage(Message message, Object... args) throws ProcessorException {

		try {
			return Messenger.sendMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Immediately sends the specified message with the given arguments to the
	 * local server
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void localPostMessage(Message message, Object... args) throws ProcessorException {

		try {
			Messenger.localPostMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_LOCALPOSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(Message message) throws ProcessorException {

		try {
			Messenger.postMessage(message);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(String toUri) throws ProcessorException {

		Message message = new Message(toUri, this.server);
		try {
			Messenger.postMessage(message);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param toUri
	 * @param args
	 * @throws ProcessorException
	 */
	public final void postMessage(String toUri, Object... args) throws ProcessorException {

		Message message = new Message(toUri, this.server);
		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param toUri
	 * @param replyServer
	 * @param args
	 * @throws ProcessorException
	 */
	public final void postMessage(String toUri, IServer replyServer, Object... args) throws ProcessorException {

		Message message = new Message(toUri, replyServer);

		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(Message message, Object... args) throws ProcessorException {

		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_POSTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each node of this
	 * node
	 * 
	 * @param toUri
	 * @param args
	 * @throws ProcessorException
	 */
	public final void broadcastMessage(String toUri, Object... args) throws ProcessorException {

		Message message = new Message(toUri, this.server);

		try {
			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_BROADCASTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each node of this
	 * node
	 * 
	 * @param toUri
	 * @param replyServer
	 * @param args
	 * @throws ProcessorException
	 */
	public final void broadcastMessage(String toUri, IServer replyServer, Object... args) throws ProcessorException {

		Message message = new Message(toUri, replyServer);

		try {

			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_BROADCASTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each node of this
	 * node
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void broadcastMessage(Message message, Object... args) throws ProcessorException {

		try {
			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new ProcessorException(ProcessorException.SEVERITY_ERROR, ProcessorException.ANT_BROADCASTFAILURE,
					message.getAddress().getURI());
		}
	}

	/**
	 * Gives a processor an opportunity to do any message preprocessing that may
	 * be required.
	 * 
	 * @param env
	 *            Envelope of message
	 * @return true if preprocessing was successful and the message is ready to
	 *         be delivered. false if preprocessing failed and message delivery
	 *         should not be attempted.
	 */
	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	/**
	 * Checks this processor's message queue for available messages.
	 * 
	 * @param bRemove
	 *            Indicates whether the message should be removed from the
	 *            queue.
	 * @return Envelope of available message or null if no message is available
	 */
	public final Envelope peekMessage(boolean bRemove) {
		return agent.peekMessage(bRemove);
	}

	/**
	 * This retrieves the list of nodes being maintained by the node topology.
	 * 
	 * @param bAll
	 *            Indicates whether all the nodes should be returned, or only
	 *            the nodes that are directly connected
	 * @return Iterator of the nodes requested null if there are no nodes
	 *         connected
	 */
	public final Iterator<Node> getNodes(boolean all) {

		try {
			return ((NetworkAgent) server.getService("networkagent")).getValues(all);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object execute(Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}
}
