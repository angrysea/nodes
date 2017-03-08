package org.amg.node.exception;

import org.amg.node.exception.AMGException;

public class FastCacheException extends AMGException {
	private static final long serialVersionUID = 1L;

	public FastCacheException(final int hr) {
		super(hr);
	}

	public FastCacheException(final String description) {
		super(-1);
		setExtraText(description);
	}

	public FastCacheException(final int hr, final String description) {
		super(hr);
		setExtraText(description);
	}

	public FastCacheException(final int sev, final int code) {
		super(sev, FACILITY_COM, code);
	}

	public FastCacheException(final int sev, final int code, final String description) {
		super(sev, FACILITY_COM, code);
		setExtraText(description);
	}
}
