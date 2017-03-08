package org.amg.node.xmltools.xmlconverter;

import java.io.InputStream;
import java.util.Collection;

import org.amg.node.exception.AMGException;

interface LoaderBase {

	public abstract void xmlLoad(InputStream inputstream) throws Exception,
			AMGException;

	public abstract void xmlLoad(String s) throws Exception, AMGException;

	public abstract ElementType getElementType(String s);

	public abstract Collection<ElementType> getElementTypes();

	public abstract int start();

	public abstract void end();

	public abstract String next(boolean externalizable, boolean cachable);
}
