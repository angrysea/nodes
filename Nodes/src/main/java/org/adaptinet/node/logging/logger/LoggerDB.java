package org.adaptinet.node.logging.logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;

import org.adaptinet.node.exception.AdaptinetException;
import org.adaptinet.node.exception.LoggerException;
import org.adaptinet.node.logging.loggerutils.LogEntry;

public class LoggerDB implements ILogger {
	public LoggerDB(String strUID, String strPWD, String strDSN, String strDB) throws LoggerException {
		dataSetName = strDSN;
		login = strUID;
		password = strPWD;
		databaseName = strDB;

		try {
			init();
		} catch (LoggerException e) {
			throw e;
		}
	}

	private final void init() throws LoggerException {
		String strClassName = new String();
		try {
			if (System.getProperty("java.vendor").equals("Microsoft Corp.")) {
				strClassName = "com.ms.jdbc.odbc.JdbcOdbcDriver";
			} else {
				strClassName = "sun.jdbc.odbc.JdbcOdbcDriver";
			}

			Class.forName(strClassName);
			databaseURL = new String("JDBC:ODBC:") + dataSetName;
			logConnection = DriverManager.getConnection(databaseURL, login, password);

			if (lastMsgHandle == -1) {
				Statement s = logConnection.createStatement();
				ResultSet r = s.executeQuery("SELECT MAX(messageHandle) from LogEntryTable");

				if (r.next()) {
					lastMsgHandle = r.getInt(1);
				}

				r.close();
				s.close();
			}

		} catch (SQLException e) {
			String xT = ":";
			while (e != null) {
				xT += e.getMessage();
				xT += e.getErrorCode();
				e = e.getNextException();
			}
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.LOG_SQLERR, xT);
		} catch (ClassNotFoundException e) {
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.LOG_SQLERR,
					"Could not create class " + strClassName);
		}
	}

	public int logMessage(LogEntry le) throws LoggerException {
		int i = 0;
		PreparedStatement PrStmt = null;
		try {
			PrStmt = logConnection.prepareStatement(strDBinsert);
			PrStmt.setInt(DBCOL_messageHandle, ++lastMsgHandle);
			PrStmt.setInt(DBCOL_severity, le.severity);
			PrStmt.setInt(DBCOL_facility, le.facility);
			PrStmt.setInt(DBCOL_errorCode, le.errorCode);

			if (le.errorMessage != null) {
				PrStmt.setString(DBCOL_errorMessage, le.errorMessage);
			} else {
				PrStmt.setString(DBCOL_errorMessage, " ");
			}

			if (le.extraText != null) {
				PrStmt.setString(DBCOL_extraText, le.extraText);
			} else {
				PrStmt.setString(DBCOL_extraText, " ");
			}

			PrStmt.setString(DBCOL_entryTime, new java.util.Date(le.entryTime).toString());
			int rowNum = PrStmt.executeUpdate();
			if (rowNum < 0) {
				System.out.println("Failed SQL INSERT Command. Row Number = " + rowNum);
			}
		} catch (SQLException e) {
			String xT = ":";
			while (e != null) {
				xT += e.getMessage();
				xT += e.getErrorCode();
				e = e.getNextException();
			}
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.LOG_SQLERR, xT);
		}
		return lastMsgHandle;
	}

	public LogEntry getLogEntryHandle(int messageHandle) throws LoggerException {
		LogEntry tmpEntry = null;

		try {
			ResultSet rs;
			String DBselect = "SELECT * FROM LogEntryTable WHERE messageHandle = " + messageHandle;
			Statement stmt = logConnection.createStatement();
			rs = stmt.executeQuery(DBselect);

			if (rs.next()) {
				tmpEntry = new LogEntry();
				tmpEntry.saved = true;
				tmpEntry.messageHandle = rs.getInt(DBCOL_messageHandle);
				tmpEntry.severity = rs.getInt(DBCOL_severity);
				tmpEntry.facility = rs.getInt(DBCOL_facility);
				tmpEntry.errorCode = rs.getInt(DBCOL_errorCode);
				tmpEntry.errorMessage = rs.getString(DBCOL_errorMessage);
				tmpEntry.extraText = rs.getString(DBCOL_extraText);
				DateFormat df = DateFormat.getDateInstance();
				java.util.Date date = df.parse(rs.getString(DBCOL_entryTime));
				tmpEntry.entryTime = date.getTime();
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			String xT = ":";
			while (e != null) {
				xT += e.getMessage();
				xT += e.getErrorCode();
				e = e.getNextException();
			}
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.LOG_SQLERR, xT);
		} catch (ParseException e) {
			throw new LoggerException(AdaptinetException.SEVERITY_ERROR, LoggerException.LOG_SQLERR, e.toString());
		}

		return tmpEntry;
	}

	public int getLastMessageHandle() {
		return -1;
	}

	private String dataSetName;
	private String login;
	private String password;
	private String databaseName;

	private String databaseURL;
	private Connection logConnection;
	private int lastMsgHandle = -1;

	static final private String strDBinsert = "INSERT INTO LogEntryTable ( messageHandle, severity, facility, errorCode, errorMessage, extraText, entryTime ) VALUES(?,?,?,?,?,?,?) ";
	static final private int DBCOL_messageHandle = 1;
	static final private int DBCOL_severity = 2;
	static final private int DBCOL_facility = 3;
	static final private int DBCOL_errorCode = 4;
	static final private int DBCOL_errorMessage = 5;
	static final private int DBCOL_extraText = 6;
	static final private int DBCOL_entryTime = 7;
}
