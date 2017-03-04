package org.adaptinet.node.processoragent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.ProcessorException;
import org.adaptinet.node.loader.ClasspathLoader;
import org.adaptinet.node.messaging.Envelope;
import org.adaptinet.node.messaging.Message;
import org.adaptinet.node.server.IServer;

public class ProcessorMap {

	protected Processor processorObj;

	private Hashtable<String, Method> methods = new Hashtable<String, Method>();

	public ProcessorMap() {
	}

	@SuppressWarnings("unchecked")
	public void createInstance(String strName, ClasspathLoader loader)
			throws AdaptinetException {

		Class<Processor> processorClass = null;
		try {

			if (loader == null)
				processorClass = (Class<Processor>) Class.forName(strName);
			else {
				processorClass = (Class<Processor>) loader.loadClass(strName);
			}
			if (processorClass == null) {
				AdaptinetException processorex = new AdaptinetException(
						AdaptinetException.SEVERITY_FATAL,
						AdaptinetException.GEN_CLASSNOTFOUND);
				processorex.logMessage("Class not found error class name is "
						+ strName);
				throw processorex;
			}

			processorObj = (Processor) processorClass.newInstance();
			if (processorObj == null) {
				AdaptinetException processorex = new AdaptinetException(
						AdaptinetException.SEVERITY_FATAL,
						AdaptinetException.GEN_CLASSNOTFOUND);
				processorex.logMessage("Unable to create instance of class "
						+ strName);
				throw processorex;
			}

			processorObj.setServer(IServer.getServer());
			Method[] methodarray = processorClass.getMethods();
			int length = methodarray.length;
			for (int i = 0; i < length; i++) {
				methods.put(methodarray[i].getName(), methodarray[i]);
			}
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL,
					ProcessorException.ANT_CREATEINSTANCEFAILURE);
			processorex.logMessage("Class not found error class name is "
					+ strName + " reason " + e.getMessage());
			throw processorex;
		}
	}

	public void setCurrentMessage(Message msg) {
		processorObj.setCurrentMessage(msg);
	}

	public boolean preProcessMessage(Envelope env) {
		return processorObj.preProcessMessage(env);
	}

	public void setAgent(ProcessorAgent agent) {
		processorObj.setAgent(agent);
	}

	public Object executeMethod(String name, boolean bLogError, Object... args)
			throws AdaptinetException {

		Object ret = null;

		try {
			if (name != null && name.length() > 0) {
				Method m = methods.get(name);
				if (m == null) {
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_FATAL,
							ProcessorException.ANT_METHODNOTSUPPORTED);
					if (bLogError == true) {
						processorex.logMessage("Illegal method name value is "
								+ name);
					}
					throw processorex;
				}
				ret = m.invoke(processorObj, args);
			}
		} catch (AdaptinetException e) {
			throw e;
		} catch (InvocationTargetException e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " InvocationTargetException thrown: " + e);
			}
			Throwable ex = e.getTargetException();
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " Target exception thrown: " + ex.getMessage());
			}
			ex.printStackTrace();
			throw processorex;
		} catch (Exception e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " exception thrown: " + e);
			}
			throw processorex;
		}
		return ret;
	}

	public Object executeMethod(String name, boolean bLogError, Object request)
			throws AdaptinetException {

		Object ret = null;

		try {
			if (name != null && name.length() > 0) {
				Method m = methods.get(name);
				if (m == null) {
					ProcessorException processorex = new ProcessorException(
							AdaptinetException.SEVERITY_FATAL,
							ProcessorException.ANT_METHODNOTSUPPORTED);
					if (bLogError == true) {
						processorex.logMessage("Illegal method name value is "
								+ name);
					}
					throw processorex;
				}
				ret = m.invoke(processorObj, request);
			}
		} catch (AdaptinetException e) {
			throw e;
		} catch (InvocationTargetException e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " InvocationTargetException thrown: " + e);
			}
			Throwable ex = e.getTargetException();
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " Target exception thrown: " + ex.getMessage());
			}
			ex.printStackTrace();
			throw processorex;
		} catch (Exception e) {
			ProcessorException processorex = new ProcessorException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				processorex.logMessage("Error executing method " + name
						+ " exception thrown: " + e);
			}
			throw processorex;
		}
		return ret;
	}

	public void nodeUpdate() {
		processorObj.nodeUpdate();
	}
}
