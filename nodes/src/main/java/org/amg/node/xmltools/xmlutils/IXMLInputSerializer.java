package org.amg.node.xmltools.xmlutils;

import java.io.InputStream;

public interface IXMLInputSerializer {
  /**
   * Marshals the incoming XML to the appropriate object
   * @param is Source of XML
   * @return The object created from the incoming XML
   * @throws Exception
   */
  public Object get(InputStream is) throws Exception;

  /**
   * Marshals the incoming XML to the appropriate object
   * @param is Source of XML
   * @param loader A class loader created specifically to marshal
   * this object.
   * @return The object created from the incoming XML
   * @throws Exception
   */
  public Object get(InputStream is, ClassLoader loader) throws Exception;

  /**
   * Marshals the incoming XML to the appropriate object
   * @param in Source of XML
   * this object.
   * @return The object created from the incoming XML
   * @throws Exception
   */
  public Object get(String in) throws Exception;

  /**
   * Marshals the incoming XML to the appropriate object
   * @param in Source of XML
   * @param loader A class loader created specifically to marshal
   * this object.
   * @return The object created from the incoming XML
   * @throws Exception
   */
  public Object get(String in, ClassLoader loader) throws Exception;

  /**
   * This methods allows you to set the repository package name which
   * will be used to later locate the classes.
   * @param newValue
   */
  public void setPackage(String newValue);

  /**
   * This method will return the last error if one has occured during
   * the XML marshalling
   * @return The exception that had been throw during marshalling
   */
  public Exception getLastError();

  /**
   * This method will indicate if there was an error after the last
   * marshalling process.
   * @return true or false indicating whether an error has occured or not
   */
  public boolean hasError();
}
