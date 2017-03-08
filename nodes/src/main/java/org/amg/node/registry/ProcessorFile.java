package org.amg.node.registry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.amg.node.exception.AMGException;
import org.amg.node.processoragent.ProcessorAgent;
import org.amg.node.processoragent.ProcessorFactory;
import org.amg.node.server.IServer;
import org.amg.node.server.ServerConfig;
import org.amg.node.xmltools.parser.InputSource;
import org.amg.node.xmltools.parser.XMLReader;

public final class ProcessorFile {

	private boolean bIsDirty = false;
	private boolean bOpen = false;
	private String processorFileName;
	private File file;
	private Thread saveThread;
	private StringBuffer buffer = null;
	private Map<String, ProcessorEntry> processors = null;

	public ProcessorFile() {
	}

	public ProcessorFile(String name) {
		try {
			openFile(name);
		} catch (Exception e) {
		}
	}

	public void openFile(String name) throws Exception {
		FileInputStream fis = null;
		processorFileName = name;
		try {
			file = new File(processorFileName);
			if (!file.exists()) {
				new File(processorFileName.substring(0,
						processorFileName.lastIndexOf(File.separatorChar)))
						.mkdirs();
				file.createNewFile();
			}
			bOpen = true;

			fis = new FileInputStream(file);
			byte bytes[] = new byte[fis.available()];
			fis.read(bytes);

			if (bytes.length > 0) {
				XMLReader parser = new XMLReader();
				ProcessorParser processorsParser = new ProcessorParser();
				parser.setContentHandler(processorsParser);
				parser.parse(new InputSource(new ByteArrayInputStream(bytes)));
				processors = Collections.synchronizedMap(processorsParser
						.getEntries());
			} else {
				processors = Collections
						.synchronizedMap(new HashMap<String, ProcessorEntry>());
			}

			startUpdate();
		} catch (Exception x) {
			x.printStackTrace();
			throw x;
		} finally {
			if (fis != null)
				fis.close();
		}
	}

	public void preload() throws Exception {

		IServer server = IServer.getServer();
		ProcessorEntry entry = null;
		ProcessorAgent processor = null;
		try {
			processor = (ProcessorAgent) server
					.getAvailableProcessor(ProcessorFactory.MAIN);
			entry = new ProcessorEntry();
			entry.setName(ProcessorFactory.MAIN);
			processor.preProcess(ProcessorFactory.MAINCLASS);
			processor.startProcessor(entry);

			processor = (ProcessorAgent) server
					.getAvailableProcessor(ProcessorFactory.MAINTENANCE);
			entry = new ProcessorEntry();
			entry.setName(ProcessorFactory.MAINTENANCE);
			processor.preProcess(ProcessorFactory.MAINTENANCECLASS);
			processor.startProcessor(entry);

			processor = (ProcessorAgent) server
					.getAvailableProcessor(ProcessorFactory.CACHE);
			entry = new ProcessorEntry();
			entry.setName(ProcessorFactory.CACHE);
			processor.preProcess(ProcessorFactory.CACHECLASS);
			processor.startProcessor(entry);

			Boolean b = (Boolean) server
					.getSetting(ServerConfig.SHOWCONSOLE);
			if (b.booleanValue() == true) {
				processor = (ProcessorAgent) server
						.getAvailableProcessor(ProcessorFactory.CONSOLE);
				entry = new ProcessorEntry();
				entry.setName(ProcessorFactory.CONSOLE);
				entry.setType(ProcessorFactory.CONSOLECLASS);
				processor.preProcess(ProcessorFactory.SERVICECLASS);
				processor.startProcessor(entry);
			}
		} catch (Exception e) {
			throw e;
		}

		new Thread(new Runnable() {

			public void run() {
				IServer server = IServer.getServer();
				ProcessorAgent processor = null;
				try {
					for (ProcessorEntry entry : processors.values()) {
						try {
							if (entry.isPreload()) {
								processor = (ProcessorAgent) server
										.getAvailableProcessor(entry.getName());
								processor.preProcess(ProcessorFactory.SERVICECLASS);
								processor.startProcessor(entry);
							}
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}).start();
	}

	public void startUpdate() {
		try {
			saveThread = new Thread() {
				public void run() {
					try {
						while (bOpen) {
							try {
								Thread.sleep(60000);
							} catch (InterruptedException ex) {
							}
							if (bIsDirty == true) {
								save();
							}
						}
					} catch (IOException ioe) {
						AMGException plugx = new AMGException(
								AMGException.SEVERITY_ERROR,
								AMGException.GEN_MESSAGE);
						plugx.logMessage(ioe);
					} catch (Exception e) {
					}
				}
			};
			saveThread.start();
		} catch (Exception e) {
		}
	}

	public void save() throws IOException {
		buffer = new StringBuffer(1024);
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<Processors>");

		for (ProcessorEntry entry : processors.values()) {
			entry.write(buffer);
		}
		bIsDirty = false;

		buffer.append("</Processors>");

		file.renameTo(new File(processorFileName + "~"));
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(buffer.toString().getBytes());
		fos.close();
	}

	public void closeFile() {
		try {
			bOpen = false;
			saveThread.interrupt();
			saveThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ProcessorEntry findEntry(String name) {
		return processors.get(name);
	}

	public void insert(ProcessorEntry entry) {
		insert(entry.getName(), entry);
	}

	public void insert(String tag, ProcessorEntry entry) {
		try {
			processors.put(tag, entry);
			bIsDirty = true;
		} catch (NullPointerException npe) {
		} catch (Exception e) {
		}
	}

	public void remove(ProcessorEntry entry) {
		try {
			processors.remove(entry);
			bIsDirty = true;
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String getName() {
		return processorFileName;
	}

	public Iterator<ProcessorEntry> getValues() {
		return processors.values().iterator();
	}

	public void setDirty(boolean bIsDirty) {
		this.bIsDirty = bIsDirty;
	}

}
