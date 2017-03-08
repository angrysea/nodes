package org.amg.node.xmltools.xmlutils;

public interface IXMLOutputSerializer {
  /**
   * This method will generate XML from the incoming object
   * @param o The object to marshal XML out of
   * @return This is the string containing the generated XML
   * @throws Exception
   */
  public String get(Object o) throws Exception;

  /**
   * This method will generate XML from the incoming object
   * @param o The object to marshal XML out of
   * @param bXMLPI This boolean indicates whether proccessing
   * instructions should be included in the XML
   * @return This is the string containing the generated XML
   * @throws Exception
   */
  public String get(Object o, boolean bXMLPI) throws Exception;

  /**
   * This method will generate XML from the incoming object
   * @param o The object to marshal XML out of
   * @param bXMLPI This boolean indicates whether proccessing
   * instructions should be included in the XML
   * @param agentPIAttribs This is a string containing additional
   * proccessing instructions that may be nessary for various clients
   * or server platforms
   * @return This is the string containing the generated XML
   * @throws Exception
   */
  public String get(Object o, boolean bXMLPI, String agentPIAttribs) throws
      Exception;

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
