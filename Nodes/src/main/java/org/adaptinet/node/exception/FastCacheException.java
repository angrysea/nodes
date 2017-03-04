package org.adaptinet.node.exception;

import org.adaptinet.node.exception.AdaptinetException;

public class FastCacheException extends AdaptinetException {
	private static final long serialVersionUID = 1L;

	public FastCacheException(int hr) {
		super(hr);
	}

	public FastCacheException(String description) {
		super(-1);
		setExtraText(description);
	}

	public FastCacheException(int hr, String description) {
		super(hr);
		setExtraText(description);
	}

	public FastCacheException(int sev, int code) {
		super(sev, FACILITY_COM, code);
	}

	public FastCacheException(int sev, int code, String description) {
		super(sev, FACILITY_COM, code);
		setExtraText(description);
	}
}
