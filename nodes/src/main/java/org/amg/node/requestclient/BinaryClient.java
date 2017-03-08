package org.amg.node.requestclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BinaryClient extends XMLClient {

    public BinaryClient() {
    	super();
    	super.contentType = "application/octet-stream";
    }
    
    public BinaryClient(String uid, String pw) {
        super(uid, pw);
    }
    
    public BinaryClient(String token) {
        super(token);
    }
    
    public Object doTransmit(String trx, Object o) throws Exception {
        this.trx = trx;
        return doTransmit(o);
    }
    
    public byte [] doBinaryTransmit(byte [] buffer) throws Exception {
    	BinaryTransmitter transmitter = new BinaryTransmitter(buffer);
        transmitter.run();

        if (transmitter.hasError()) {
            throw transmitter.getLastError();
        }
        return transmitter.getResult();
    }

    class BinaryTransmitter implements Runnable {

        private byte [] request = null;
        private Exception lastError = null;
        private byte [] result = null;

        BinaryTransmitter(byte [] request) {
            this.request = request;
        }

        final byte [] getResult() {
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
                System.out.println("XMLSVC-ClientREQ: url="+address);
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
                    BufferedOutputStream out = new BufferedOutputStream(connection
                                    .getOutputStream()); //, "8859_1");
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
            		StringBuffer stringbuffer = new StringBuffer();
                	if (responseCode > 200) {
                		if (responseCode < 601) {
                			responseMessage = line.substring(13);
                			stringbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>");
                			stringbuffer.append(address);
                			stringbuffer.append("</url><code>");
                			stringbuffer.append(Integer.toString(responseCode));
                			stringbuffer.append("</code><desc>");
                			stringbuffer.append( responseMessage);
                			stringbuffer.append("</desc><timestamp>");
                			stringbuffer.append(new java.util.Date(System.currentTimeMillis()).toString());
                			stringbuffer.append("</timestamp></status>");
                			result = stringbuffer.toString().getBytes();
                		} else {
                			result = connection.getHeaderField("XMLSVC-Error-Message").getBytes();
                			throw new XMLClientException(new String(result), responseCode);
                		}
                	} else {
                		BufferedInputStream reader = new BufferedInputStream(connection.getInputStream());
                		int len = -1;
                		byte [] bLen = new byte[10];
                		len = reader.read(bLen, 0, 10);
                		len = Integer.valueOf(new String(bLen).trim()).intValue();
                		if (len>0) {
                			result = new byte[len];
                			int read = 0;
                			while(read<len) {
                				read += reader.read(result, read, len-read);
                			}
                		}
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
        		StringBuffer stringbuffer = new StringBuffer();
                lastError = e;
    			stringbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>");
    			stringbuffer.append(address);
    			stringbuffer.append("</url><code>");
    			stringbuffer.append(Integer.toString(responseCode));
    			stringbuffer.append("</code><desc>");
    			stringbuffer.append( responseMessage);
    			stringbuffer.append("</desc><timestamp>");
    			stringbuffer.append(new java.util.Date(System.currentTimeMillis()).toString());
    			stringbuffer.append("</timestamp></status>");
    			result = stringbuffer.toString().getBytes();
                System.out.println("XMLSVC-ClientERR: url="+address+",REQXML="+request+",err-result="+result+",ex="+e.getMessage());
                
            }         
        }
    }
}
