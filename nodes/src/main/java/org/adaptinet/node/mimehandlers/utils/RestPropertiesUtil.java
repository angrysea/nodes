package org.adaptinet.node.mimehandlers.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class RestPropertiesUtil {
	private Properties props = new Properties();
	private static final String SYNTHESIS_PROPERTY_FILE = "./instance-config/synthesis.properties";
	private static RestPropertiesUtil propInstance = null; 
	
	private RestPropertiesUtil(){
		try {
			props.load(new FileInputStream(SYNTHESIS_PROPERTY_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static RestPropertiesUtil getInstance(){
		if(propInstance == null){
			propInstance = new RestPropertiesUtil();
		}
		return propInstance;
	}
	
	public String getProperty(String property){
		return props.getProperty(property);
	}
}
