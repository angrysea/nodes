package org.adaptinet.node.logging.logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.adaptinet.node.exception.BaseException;
import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class LoggerFile implements ILogger {
	private String logFileName;

	// private final String messageHandleEnd = "</Last_Handle>";
	// private final String messageHandleStart = "<Last_Handle>";
	private final String strEND_ROOT = "</LogData>\r\n";
	// private final String strHandle = "LOG.XML";
	// private final String strLASTRecord = "</Data>";
	// private final String strLogName = "LOG.XML";
	private final String strROOT = "<LogData>\r\n";
	// private final String strTopRecord = "<Data>";
	private final String strXMLHeader = "<?xml version=\"1.0\" standalone=\"yes\"?>\r\n";

	public LoggerFile(String strFileName) throws LoggerException {
		logFileName = strFileName;
		File fLog = new File(strFileName);
		if (!fLog.exists()) {
			try {
				FileOutputStream fo = new FileOutputStream(strFileName, false);
				fo.write("<?xml version=\"1.0\"?><XMLBrokerLog>".getBytes());
				fo.close();
			} catch (IOException e) {
				throw new LoggerException(BaseException.SEVERITY_ERROR, BaseException.FACILITY_LOGGER, e.getMessage());
			}
		}
	}

	private String buildLogMsg(LogEntry le) {

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<Record>");
		strBuilder.append("<Handle>");
		strBuilder.append(le.messageHandle);
		strBuilder.append("</Handle>");
		strBuilder.append("<DateTime>");
		strBuilder.append(new java.util.Date(System.currentTimeMillis()).toString());
		strBuilder.append("</DateTime>");
		strBuilder.append("<Severity>");
		strBuilder.append(le.severity);
		strBuilder.append("</Severity>");
		strBuilder.append("<Facility>");
		strBuilder.append(le.facility);
		strBuilder.append("</Facility>");
		strBuilder.append("<ErrorCode>");
		strBuilder.append(le.errorCode);
		strBuilder.append("</ErrorCode>");

		if (le.errorMessage != null) {
			strBuilder.append("<ErrorMsg>");
			strBuilder.append(le.errorMessage);
			strBuilder.append("</ErrorMsg>");
		} else {
			strBuilder.append("<ErrorMsg> </ErrorMsg>");
		}

		if (le.extraText != null) {
			strBuilder.append("<ExtraText>");
			strBuilder.append(le.extraText);
			strBuilder.append("</ExtraText>");
		} else {
			strBuilder.append("<ExtraText> </ExtraText>");
		}

		strBuilder.append("</Record>\r\n");

		return strBuilder.toString();
	}

	@Override
	public int getLastMessageHandle() {
		int iHandle = 0;
		try {
			int n;
			String ln = new String();
			String lastLn = new String();
			final FileInputStream fi = new FileInputStream(logFileName);
			final DataInputStream buff = new DataInputStream(fi);

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
								final Integer i = new Integer(lastLn);
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

	@Override
	public LogEntry getLogEntryHandle(int messageHandle) {
		return new LogEntry();
	}

	@Override
	public int logMessage(LogEntry le) throws LoggerException {
		File f = new File(logFileName);
		try {
			final RandomAccessFile raf = openLogFile(f);

			StringBuilder strBuff = new StringBuilder(buildLogMsg(le));
			if (f.length() == 0) {
				strBuff.append(strXMLHeader);
				strBuff.append(strROOT);
				strBuff.append(strBuff);
			}
			strBuff.append(strEND_ROOT);
			if (f.length() > strEND_ROOT.length()) {
				raf.seek(f.length() - strEND_ROOT.length());
			}
			raf.write(strBuff.toString().getBytes());
			raf.close();
		} catch (IOException e) {
			throw new LoggerException(BaseException.SEVERITY_ERROR, BaseException.FACILITY_LOGGER, e.getMessage());
		}
		return le.messageHandle;
	}

	private RandomAccessFile openLogFile(File f) throws LoggerException {
		RandomAccessFile raf = null;

		try {
			if (!f.exists()) {
				try {
					f.createNewFile();
					raf = new RandomAccessFile(f, "rw");
				} catch (Exception x) {
					throw new LoggerException(BaseException.SEVERITY_ERROR, BaseException.FACILITY_LOGGER,
							x.getMessage());
				}
			}

			raf = new RandomAccessFile(logFileName, "rw");
		} catch (FileNotFoundException e) {
			throw new LoggerException(BaseException.SEVERITY_ERROR, BaseException.FACILITY_LOGGER, e.getMessage());
		}

		return raf;
	}
}
