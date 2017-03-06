package org.adaptinet.node.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adaptinet.node.server.IServer;
import org.adaptinet.node.serverutils.ThreadCache;
import org.adaptinet.node.servlet.http.HttpSession;

public class ServletFactory implements Runnable {
	public static final int AVG_DEAD = 4;
	public static final int AVG_HIGH = 3;
	public static final int AVG_LIGHT = 1;
	public static final int AVG_NORMAL = 2;
	public static final int IDLETO = 10000;
	private List<ServletState> brokerList = null;
	private int brokers = 0;
	private String classpath = null;
	private List<ServletState> freeList = null;
	private int idealFree = 0;
	private List<ServletState> idleList = null;
	private int loadavg = AVG_LIGHT;
	private boolean running = true;
	private IServer server = null;
	private ServletContext servletContext;
	private Map<String, HttpSession> sessions = new HashMap<>();
	private ThreadCache threadcache = null;
	private boolean verbose = false;

	public ServletFactory(String classpath, boolean v) {
		verbose = v;
		this.classpath = classpath;
		idleList = Collections.synchronizedList(new ArrayList<>());
		freeList = Collections.synchronizedList(new ArrayList<>());
		brokerList = Collections.synchronizedList(new ArrayList<>());
		servletContext = new ServletContext();
	}
	protected synchronized void deleteClient(ServletState state) {
		brokerList.remove(state);
	}
	public IServer getServer() {
		return server;
	}
	/*
	 * protected synchronized ServletState addBroker() { CachedThread t =
	 * threadcache.getThread(true); ServletBroker broker = new
	 * ServletBroker(this, classpath, t, verbose); ServletState state =
	 * broker.getState(); brokerList.add(state);
	 * 
	 * state.setStatus(ServletState.FREE); freeList.add(state);
	 * 
	 * return state; }
	 * 
	 * public void initialize(IServer theServer, int brokers) { this.brokers =
	 * brokers; server = theServer; idealFree = brokers > 1 ? (brokers >> 1) :
	 * 1; servletContext.setAttribute("com.adaptinet.xsltpath",
	 * server.getXSLPath());
	 * 
	 * threadcache = new ThreadCache("servlets", brokers,
	 * server.getClientThreadPriority(), 0);
	 * 
	 * for (int i = 0; i < brokers; i++) { if (addBroker() == null) { throw new
	 * RuntimeException( this.getClass().getName() + "[constructor]" +
	 * ": unable to create servlets."); } } }
	 * 
	 * public void run() { while (true) { int oldavg = loadavg; int free =
	 * freeList.size();
	 * 
	 * if (free >= idealFree) { loadavg = AVG_LIGHT; } else if (free < 5) { if
	 * ((loadavg = AVG_NORMAL) < oldavg) {
	 * server.getThread().setPriority(Thread.MAX_PRIORITY); } } else if (free >
	 * 0) { if ((loadavg = AVG_HIGH) > oldavg) {
	 * server.getThread().setPriority(server.getClientThreadPriority() - 2); } }
	 * else { loadavg = AVG_DEAD; }
	 * 
	 * if (Thread.currentThread().interrupted() == true) { return; }
	 * 
	 * // System.out.println("Call killTimedoutBrokers");
	 * 
	 * killTimedoutBrokers();
	 * 
	 * // System.out.println("return killTimedoutBrokers"); try {
	 * Thread.sleep(1000); } catch (InterruptedException e) { return; } } }
	 * 
	 * synchronized protected void killTimedoutBrokers() { try { Iterator it =
	 * brokerList.iterator();
	 * 
	 * while (it.hasNext()) { stopThisBroker((ServletState) it.next()); } }
	 * catch (Exception e) { } }
	 * 
	 * final public boolean killBroker(int id, boolean force) { ServletState
	 * state = (ServletState) brokerList.get(id); if (state != null) { boolean
	 * killed = state.kill(force); idleList.remove(state);
	 * state.setStatus(ServletState.FREE); freeList.add(state); if (killed) {
	 * state.reset(threadcache.getThread(true)); } return true; } return false;
	 * }
	 * 
	 * final public boolean stopBroker(int id) { boolean bStop = true;
	 * ServletState state = (ServletState) brokerList.get(id); if (state !=
	 * null) { bStop = false; }
	 * 
	 * while (bStop == false) { bStop = stopThisBroker(state); try {
	 * Thread.sleep(state.getTimeOut()); } catch (InterruptedException e) {
	 * bStop = true; } }
	 * 
	 * return !bStop; }
	 * 
	 * final private boolean stopThisBroker(ServletState state) { boolean bDead
	 * = false;
	 * 
	 * try { switch (state.getStatus()) { case ServletState.BUSY: if
	 * (state.getServletBroker().isAlive() == false) {
	 * notifyIdle(state.getServletBroker()); ServletException servletex = new
	 * ServletException(AdaptinetException.SEVERITY_WARNING,
	 * ServletException.XBK_BROKERTIMEOUT4);
	 * servletex.logMessage(state.getName()); } else if (state.isTimedOut()) {
	 * state.setStatus(ServletState.SIGNALED); state.startTimer(); // new
	 * Thread(new Rollback(state)).start(); ServletException servletex = new
	 * ServletException(AdaptinetException.SEVERITY_WARNING,
	 * ServletException.XBK_BROKERTIMEOUT1);
	 * servletex.logMessage(state.getName()); } break;
	 * 
	 * case ServletState.SIGNALED: if (state.isTimedOut()) {
	 * state.setStatus(ServletState.INTERRUPTED);
	 * state.getServletBroker().interrupt(); state.startTimer();
	 * ServletException servletex = new
	 * ServletException(AdaptinetException.SEVERITY_WARNING,
	 * ServletException.XBK_BROKERTIMEOUT2);
	 * servletex.logMessage(state.getName()); } break;
	 * 
	 * case ServletState.INTERRUPTED: if (state.isTimedOut()) {
	 * state.kill(true); notifyIdle(state.getServletBroker()); ServletException
	 * servletex = new ServletException(AdaptinetException.SEVERITY_WARNING,
	 * ServletException.XBK_BROKERTIMEOUT3);
	 * servletex.logMessage(state.getName()); } break;
	 * 
	 * case ServletState.IDLE: case ServletState.FREE: case ServletState.KILL:
	 * default: bDead = true; break; } } catch (Exception e) { } return bDead; }
	 * 
	 * protected void brokerFinished(ServletBroker broker) { ServletState state
	 * = broker.getState();
	 * 
	 * if (verbose) { System.out.println("Servlet " + state.getName() +
	 * " in slot " + new Integer(state.getId()).toString() + " is finished."); }
	 * 
	 * if (running == true) { if (state.getStatus() == ServletState.IDLE) {
	 * idleList.remove(state); } else if (state.getStatus() !=
	 * ServletState.FREE) { state.reset(); state.setStatus(ServletState.FREE);
	 * freeList.add(state); } } }
	 * 
	 * synchronized protected void notifyIdle(ServletBroker broker) {
	 * ServletState state = broker.getState();
	 * 
	 * if (verbose) { System.out.println( "Servlet " + state.getName() +
	 * " in slot " + new Integer(state.getId()).toString() + " is idle."); }
	 * 
	 * state.setStatus(ServletState.IDLE); idleList.add(state); }
	 * 
	 * synchronized protected int freeSomeBrokers() { int count = idealFree -
	 * freeList.size(); if (count > 0) { int size = idleList.size(); count =
	 * (count > size ? size : count); size--;
	 * 
	 * for (int i = 0; i < count; i++) { ServletState s = (ServletState)
	 * idleList.remove(size - i); s.setStatus(ServletState.FREE); s.reset();
	 * freeList.add(s); } } return count; }
	 * 
	 * public void run(ServletBroker broker) { broker.wakeup(); }
	 * 
	 * public ServletBroker getAvailableServlet() { return
	 * getAvailableServlet(null); }
	 * 
	 * public synchronized ServletBroker getAvailableServlet(String name) {
	 * ServletBroker broker = null; ServletState state = null; try { if (name !=
	 * null) { Iterator it = idleList.iterator(); while (it.hasNext()) {
	 * ServletState s = (ServletState) it.next(); String brokerName =
	 * s.getName(); if (brokerName != null && name.equals(brokerName) == true) {
	 * state = s; idleList.remove(state); broker = state.getServletBroker(); if
	 * (verbose) { System.out.println("Reusing Servlet: " + brokerName +
	 * " in slot: " + new Integer(state.getId()).toString()); } break; } } } }
	 * catch (Exception e) { state = null; }
	 * 
	 * try { if (state == null) { int i = freeList.size(); if (i == 0) { i =
	 * freeSomeBrokers(); } if (i > 0) { i--; state = (ServletState)
	 * freeList.remove(i); broker = state.getServletBroker(); if (verbose)
	 * System.out.println( "Using slot: " + new
	 * Integer(state.getId()).toString() + " for servlet " + name); } }
	 * 
	 * if (broker == null) { AdaptinetException servletex = new
	 * AdaptinetException(AdaptinetException.SEVERITY_FATAL,
	 * ServletException.TCV_INVALIDNOTIMPLEMENT); servletex.logMessage(
	 * "Exception thrown in ServletFactory [getAvailableBroker]\n\tNo available servlets\n\tServlet Name : "
	 * + name); } else { state.setStatus(ServletState.BUSY); } } catch
	 * (Exception e) { AdaptinetException servletex = new
	 * AdaptinetException(AdaptinetException.SEVERITY_FATAL,
	 * ServletException.TCV_INVALIDPI); servletex.
	 * logMessage("Exception thrown in ServletFactory [getAvailableBroker]\n\tServlet Name : "
	 * + name + "\n\terror = " + e.toString() + " " + e.getMessage()); } return
	 * broker; }
	 * 
	 * public void shutdown(boolean force) { running = false; Iterator it =
	 * brokerList.iterator(); for (ServletState state = (ServletState)
	 * it.next(); it.hasNext(); state = (ServletState) it.next()) {
	 * state.kill(force); }
	 * 
	 * brokerList = null; freeList = null; idleList = null; server = null; }
	 */
	public List<ServletState> getServletBrokers() {
		return brokerList;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}
	public synchronized HttpSession getSession(String sessionId) {
		return (HttpSession) sessions.get(sessionId);
	}
	public synchronized void putSession(String sessionId, HttpSession session) {
		sessions.put(sessionId, session);
	}
	public synchronized void removeSession(String sessionId) {
		sessions.remove(sessionId);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
