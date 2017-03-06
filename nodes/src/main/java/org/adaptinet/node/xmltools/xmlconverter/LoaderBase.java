package org.adaptinet.node.xmltools.xmlconverter;

import java.io.InputStream;
import java.util.Collection;

import org.adaptinet.node.exception.AdaptinetException;

interface LoaderBase {

	public abstract void xmlLoad(InputStream inputstream) throws Exception,
			AdaptinetException;

	public abstract void xmlLoad(String s) throws Exception, AdaptinetException;

	public abstract ElementType getElementType(String s);

	public abstract Collection<ElementType> getElementTypes();

	public abstract int start();

	public abstract void end();

	public abstract String next(boolean externalizable, boolean cachable);
}
