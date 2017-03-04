package org.adaptinet.node.exception;

public class TypeException extends org.adaptinet.node.exception.AdaptinetException {

	private static final long serialVersionUID = -7906310225048606491L;

	public TypeException() {
		super(SEVERITY_ERROR, FACILITY_GENERAL, GEN_TYPEMISMATCH);
	}
}
