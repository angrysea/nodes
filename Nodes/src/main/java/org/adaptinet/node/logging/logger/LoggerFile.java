package org.adaptinet.node.logging.logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class LoggerFile implements ILogger {
	public LoggerFile(String strFileName) throws LoggerException {
		logFileName = strFileName;
		File fLog = new File(strFileName);
		if (!fLog.exists()) {
			try {
				FileOutputStream fo = new FileOutputStream(strFileName, false);
				fo.write("<?xml version=\"1.0\"?><XMLBrokerLog>".getBytes());
			} catch (IOException e) {
				throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.FACILITY_LOGGER,
						e.getMessage());
			}
		}
	}

	public int getLastMessageHandle() {
		int iHandle = 0;
		try {
			int n;
			String ln = new String();
			String lastLn = new String();
			FileInputStream fi = new FileInputStream(logFileName);
			DataInputStream buff = new DataInputStream(fi);

			while (true) {
				ln = buff.readUTF();
				if (ln != null) {
					lastLn = ln;
				} else if (lastLn != null) {
					n = lastLn.indexOf("<Handle>");
					if (n > 0) {
						lastLn = lastLn.substring(n + 8);
						n = lastLn.indexOf("</Handle>");
						if (n > 0) {
							lastLn = lastLn.substring(0, n);
							if (lastLn.length() > 0) {
								Integer i = new Integer(lastLn);
								iHandle = i.intValue();
							}
						}
					}
					break;
				}
			}
			buff.close();
			fi.close();
		} catch (Exception e) {
			// System.out.println(e.getMessage());
		}

		return iHandle;
	}

	private String buildLogMsg(LogEntry le) {

		String strBuff = new String("<Record>");
		strBuff += "<Handle>" + le.messageHandle + "</Handle>";
		strBuff += "<DateTime>" + new java.util.Date(System.currentTimeMillis()).toString() + "</DateTime>";
		strBuff += "<Severity>" + le.severity + "</Severity>";
		strBuff += "<Facility>" + le.facility + "</Facility>";
		strBuff += "<ErrorCode>" + le.errorCode + "</ErrorCode>";

		if (le.errorMessage != null) {
			strBuff += "<ErrorMsg>" + le.errorMessage + "</ErrorMsg>";
		} else {
			strBuff += "<ErrorMsg> </ErrorMsg>";
		}

		if (le.extraText != null) {
			strBuff += "<ExtraText>" + le.extraText + "</ExtraText>";
		} else {
			strBuff += "<ExtraText> </ExtraText>";
		}

		strBuff += "</Record>\r\n";

		return strBuff;
	}

	private RandomAccessFile openLogFile(File f) throws LoggerException {
		// FileWriter fw = new FileWriter(f);
		// BufferedWriter bw = new BufferedWriter(fw);
		// BufferedOutputStream bos = new BufferedOutputStream(

		RandomAccessFile raf = null;

		try {
			if (!f.exists()) {
				try {
					f.createNewFile();
					raf = new RandomAccessFile(f, "rw");
				} catch (Exception x) {
					throw new LoggerException(AdaptinetException.SEVERITY_ERROR, AdaptinetException.FACILITY_LOGGER,
							x.getMessage());
				}
			}

			raf = new RandomAccessFile(logFileName, "rw");
		} catch (FileNotFoundException e) {
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, AdaptinetException.FACILITY_LOGGER,
					e.getMessage());
		}

		return raf;
	}

	public int logMessage(LogEntry le) throws LoggerException {
		/*
		 * FileOutputStream fo = new FileOutputStream(f.getName(),true);
		 * DataOutputStream dos = new DataOutputStream(fo);
		 * fo.write(strBuff.getBytes()); fo.close();
		 */

		RandomAccessFile raf = null;
		File f = new File(logFileName);

		try {
			raf = openLogFile(f);

			String strBuff = buildLogMsg(le);
			if (f.length() == 0)
				strBuff = strXMLHeader + strROOT + strBuff;

			strBuff = strBuff + strEND_ROOT;

			if (f.length() > strEND_ROOT.length())
				raf.seek(f.length() - strEND_ROOT.length());

			// raf.writeUTF(strBuff);
			raf.write(strBuff.getBytes());
			raf.close();
		} catch (IOException e) {
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, AdaptinetException.FACILITY_LOGGER,
					e.getMessage());
		}

		return le.messageHandle;
	}

	public LogEntry getLogEntryHandle(int messageHandle) {
		return new LogEntry();
	}

	private String logFileName;
	private final String strXMLHeader = "<?xml version=\"1.0\" standalone=\"yes\"?>\r\n";

	private final String messageHandleStart = "<Last_Handle>";
	private final String messageHandleEnd = "</Last_Handle>";
	private final String strTopRecord = "<Data>";
	private final String strLASTRecord = "</Data>";

	private final String strROOT = "<LogData>\r\n";
	private final String strEND_ROOT = "</LogData>\r\n";

	private final String strLogName = "LOG.XML";
	private final String strHandle = "LOG.XML";
}
