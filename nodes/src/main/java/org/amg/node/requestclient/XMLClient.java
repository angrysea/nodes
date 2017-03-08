package org.amg.node.requestclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.amg.node.xmltools.xmlutils.IXMLInputSerializer;
import org.amg.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.amg.node.xmltools.xmlutils.XMLSerializerFactory;

public class XMLClient {

    static final public String SC_TOKEN = "SCTOKEN=";
    static final public String SETCOOKIE = "Set-Cookie";
    static final public String COOKIE = "Cookie";
    protected String token = null;
    protected String host = "localhost";
    protected int port = 1443;
    protected String method = "POST";
    protected String trx = "/";
    protected String address;
    protected boolean bDebug = false;
    protected String contentType = "application/ws+xml";
    protected int responseCode = 200;
    protected String responseMessage = null;
    
    public XMLClient() {
    }

    public XMLClient(String uid, String pw) {
        throw new UnsupportedOperationException("RS XMLSVC-Client cannot be created with userid/pwd - please use token for creation");
    }
    
    public XMLClient(String token) {
        this.token = token;
    }
    
    public Object doTransmit(String trx, Object o) throws Exception {
        this.trx = trx;
        return doTransmit(o);
    }
    
    public Object doTransmit(Object o) throws Exception {
        IXMLOutputSerializer out = XMLSerializerFactory.getOutputSerializer();        
        String reqMsg=out.get(o, true);
        String result = doTransmit(reqMsg);
        
        if (bDebug) {
          System.out.println("XMLSVC-Client-Verbose: trx=" + trx + ",responseCode=" + responseCode+ "req=["+reqMsg+"]\n, resp=[" + result + "]");
        }
        
        /*
        if (responseCode > 200) {            
            throw new Exception(result);        
        }
        */
        Object retObj = null;
        if(result!=null && result.length()>0) {
            try {
                String className = o.getClass().getName();
                String strpackage = className.substring(0, className.lastIndexOf('.'));
                IXMLInputSerializer in = XMLSerializerFactory.getInputSerializer();
                in.setPackage(strpackage);
                retObj = in.get(result);
                if (bDebug) {
                	System.out.println("XMLSVC-ClientRSP-Deserialize: url="+address+",REQXML="+reqMsg+", RSPHTTPCode="+responseCode+", deserialization: success");
                }
            }
            catch (Exception ex) {
            	System.out.println("XMLSVC-ClientRSP-Deserialize: url="+address+",REQXML="+reqMsg+", RSPHTTPCode="+responseCode+", error during-deserialization: ex="+ex.getMessage()+
                              (bDebug?",result="+result:""));               
                throw ex;
            }
        }
        else {
            if (bDebug) {
            	System.out.println("XMLSVC-ClientRSP-Deserialize: url="+address+",REQXML="+reqMsg+", RSPHTTPCode="+responseCode+", XML result pre-deserialization is "+(result==null?"null":"empty"));
            }
        }
        return retObj;
    }

    public String doTransmit(String xmlIn) throws Exception {
        XMLTransmitter transmitter = new XMLTransmitter(xmlIn);
        transmitter.run();

        if (transmitter.hasError()) {
            throw transmitter.getLastError();
        }
        return transmitter.getResult();
    }

    class XMLTransmitter implements Runnable {

        private String request = null;
        private Exception lastError = null;
        private String result = null;

        XMLTransmitter(String request) {
            this.request = request;
        }

        final String getResult() {
            return result;
        }

        final Exception getLastError() {
            return lastError;
        }

        final boolean hasError() {
            return lastError != null;
        }

        public void run() {

            result = null;
            HttpURLConnection connection = null;

            try {
                if (!trx.startsWith("/")) {
                    trx = "/" + trx;
                }
                address = "http://" + host + ":" + Integer.toString(port)+ trx;

                responseCode = 601;
                System.out.println("XMLSVC-ClientREQ: url="+address+",REQXML="+request);
                URL url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Content-Type", " " + contentType);
                connection.setRequestProperty("Connection", "Keep-Alive");
                if (token != null) {
                    connection.setRequestProperty(COOKIE, SC_TOKEN + token);
                }
                if (method.equals("POST")) {
                    connection.setDoOutput(true);
                    connection.setAllowUserInteraction(true);
                    
                    connection.setRequestMethod("POST");            
                    OutputStreamWriter out = new OutputStreamWriter(
                            new BufferedOutputStream(connection
                                    .getOutputStream()), "8859_1");
                    out.write(request);
                    connection.connect();
                    out.flush();
                    out.close();
                } else {
                    connection.setDoOutput(false);
                    connection.connect();
                }

                try {
                	String line = connection.getHeaderField(0);
                	responseCode = Integer.parseInt(line.substring(9, 12));
                	System.out.println("XMLSVC-ClientRSP: url="+address+",REQXML="+request+", RSPHTTPCode="+responseCode);
                	if (responseCode > 200) {
                		if (responseCode < 601) {
                			responseMessage = line.substring(13);
                			result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>"
                				+ address
                				+ "</url><code>"
                				+ Integer.toString(responseCode)
                				+ "</code><desc>"
                				+ responseMessage
                				+ "</desc><timestamp>"
                				+ (new java.util.Date(System
                						.currentTimeMillis()).toString())
                						+ "</timestamp></status>";
                		} else {
                			result = connection.getHeaderField("XMLSVC-Error-Message");
                			throw new XMLClientException(result, responseCode);
                		}
                	} else {
                		InputStreamReader reader = new InputStreamReader(
                				new BufferedInputStream(connection.getInputStream()));
                		StringBuffer stringbuffer = new StringBuffer();

                		int b = -1;
                		while ((b = reader.read()) != -1) {
                			stringbuffer.append((char) b);
                		}
                		result = stringbuffer.toString();
                		reader.close();
                	}
                }
                finally {
                	try {
                       connection.disconnect();
                	}
                	catch (Exception ex) {
                		System.out.println("XMLSVC-ClientERR: url="+address+",REQXML="+request+",disconnect error="+ex.getMessage());
                	}
                }
            } catch (XMLClientException e) {
                lastError = e;
                System.out.println("XMLSVC-ClientERR: url="+address+",REQXML="+request+",JPM-Error-Message="+result+",xmlclex="+e.getMessage());
            } catch (Exception e) {

                lastError = e;
                result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>"
                    + address
                    + "</url><code>"
                    + Integer.toString(responseCode)
                    + "</code><desc>"
                    + e.getMessage()
                    + "</desc><timestamp>"
                    + (new java.util.Date(System.currentTimeMillis())
                    .toString()) + "</timestamp></status>";

                //if (bDebug) {
                    //e.printStackTrace();
                    //System.out.println(result);
                //}
                System.out.println("XMLSVC-ClientERR: url="+address+",REQXML="+request+",err-result="+result+",ex="+e.getMessage());
                
            }
         
        }
    }

    public void setPort(int port) {
        this.port = port;
    }
    public void setTrx(String trx) {
        this.trx = trx;
    }

    public void setTimeout(int timeout) {
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUrl(String address) {
        this.address = address;
    }

    public void setDebug(boolean b) {
        bDebug = b;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }


}
