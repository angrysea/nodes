package org.amg.node.exception;

public class TypeException extends AMGException {

	private static final long serialVersionUID = -7906310225048606491L;

	public TypeException() {
		super(SEVERITY_ERROR, FACILITY_GENERAL, GEN_TYPEMISMATCH);
	}
}
