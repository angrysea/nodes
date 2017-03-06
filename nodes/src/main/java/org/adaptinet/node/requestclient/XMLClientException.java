package org.adaptinet.node.requestclient;

public class XMLClientException extends Exception {

	private static final long serialVersionUID = 4534031631768770851L;

	public XMLClientException(String msg) {
        super(msg);
    }

    public XMLClientException(String msg, int error) {
        super(msg);
		this.error=error;
    }

	public int getError() {
		return error;
	}

	private int error=0;
}
