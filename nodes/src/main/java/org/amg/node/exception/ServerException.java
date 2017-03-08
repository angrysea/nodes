package org.amg.node.exception;

public class ServerException extends AMGException {

	private static final long serialVersionUID = 8420099690826723484L;
	public final static int TCV_BASE = 0;
	public final static int TCV_UNKNOWNHOST = TCV_BASE + 1;
	public final static int TCV_INVALIDPORT = TCV_BASE + 2;
	public final static int TCV_INVALIDMAXCLIENTS = TCV_BASE + 3;
	public final static int TCV_INVALIDLICENSE = TCV_BASE + 4;
	public final static int TCV_STARTUPSUCCEED = TCV_BASE + 5;
	public final static int TCV_INITFAILDED = TCV_BASE + 6;
	public final static int TCV_URLFAILDED = TCV_BASE + 7;
	public final static int TCV_CONFIGLOADFAILED = TCV_BASE + 8;
	public final static int TCV_CONFIGLOADSUCCEEDED = TCV_BASE + 9;
	public final static int TCV_ABRUPTSHUTDOWN = TCV_BASE + 10;
	public final static int TCV_INVALIDMESSAGECACHESIZE = TCV_BASE + 11;

	public ServerException(final int sev, final int code) {
		super(sev, FACILITY_SERVER, code);
	}

	public ServerException(final int sev, final int code, final String xT) {
		super(sev, FACILITY_SERVER, code);
		setExtraText(xT);
	}

	@Override
	public final String getMessageInternal(final int e) {
		String errorMessage = new String("[SERVER]");

		switch (e) {
		case TCV_UNKNOWNHOST:
			errorMessage += "Unknown Host defaulting to localhost.";
			break;
		case TCV_INVALIDPORT:
			errorMessage += "Invalid Port";
			break;
		case TCV_INVALIDMAXCLIENTS:
			errorMessage += "Invalid Max Client Parameter.";
			break;
		case TCV_INVALIDLICENSE:
			errorMessage += "Invalid License.";
			break;
		case TCV_INITFAILDED:
			errorMessage += "Server Initialization failed aborting";
			break;
		case TCV_STARTUPSUCCEED:
			errorMessage += "Startup Succeeded.";
			break;
		case TCV_URLFAILDED:
			errorMessage += "Unable to form URL.";
			break;
		case TCV_CONFIGLOADFAILED:
			errorMessage += "Unable to load configuration file.";
			break;
		case TCV_CONFIGLOADSUCCEEDED:
			errorMessage += "Configuration successfully loaded.";
			break;
		case TCV_ABRUPTSHUTDOWN:
			errorMessage += "Abrupt shutdown of Server detected.";
			break;
		case TCV_INVALIDMESSAGECACHESIZE:
			errorMessage += "Invalid message cache size.";
			break;
		default:
			errorMessage += "Unknown error code from Server";
			break;
		}
		return errorMessage;
	}
}
