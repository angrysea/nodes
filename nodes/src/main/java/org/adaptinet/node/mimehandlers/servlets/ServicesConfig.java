package org.adaptinet.node.mimehandlers.servlets;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.adaptinet.node.mimehandlers.model.entities.ControllerMethod;
import org.adaptinet.node.mimehandlers.model.entities.Destination;
import org.adaptinet.node.mimehandlers.model.entities.Service;
import org.adaptinet.node.servlet.ServletContext;
import org.adaptinet.node.servlet.http.HttpServletConfig;
import org.adaptinet.node.xmltools.xmlutils.IXMLInputSerializer;
import org.adaptinet.node.xmltools.xmlutils.XMLSerializerFactory;

public class ServicesConfig {

	static private Map<String, Map<String, Class<?>>> controllerMethods = null;
	static private Map<String, String> packages = null;
	public static final String REMOTINGCONFIG = "Remoting.Config";
	static private Map<String, Class<?>> services = null;
	private static ServicesConfig servicesConfig = null;

	public static Class<?> getController(String serviceName) {
		return services.get(serviceName);
	}

	public static String getPackage(String serviceName) {
		return packages.get(serviceName);
	}

	public static Class<?> getRequest(String serviceName, String methodName) {
		final Map<String, Class<?>> methods = controllerMethods.get(serviceName);
		return methods.get(methodName);
	}

	public static void init(HttpServletConfig sc, ServletContext context) {
		if (servicesConfig == null) {
			servicesConfig = new ServicesConfig(sc, context);
		}
	}

	public ServicesConfig(HttpServletConfig sc, ServletContext context) {

		if (services == null) {
			final String remoteConfig = sc.getInitParameter(REMOTINGCONFIG);
			if (remoteConfig != null && remoteConfig.length() > 0) {
				final InputStream is = context.getResourceAsStream(remoteConfig);
				if (is != null) {
					services = new HashMap<String, Class<?>>();
					controllerMethods = new HashMap<String, Map<String, Class<?>>>();
					packages = new HashMap<String, String>();

					IXMLInputSerializer inserial;
					Service in = null;
					try {
						inserial = XMLSerializerFactory.getInputSerializer();
						inserial.setPackage("com.db.canvas.model.entities");
						in = (Service) inserial.get(is);
					} catch (Exception e) {
						error("Unable to parse remoting-config.xml.");
					}

					try {
						for (Destination destination : in.destinationArray()) {
							final String id = destination.getId();
							if (id != null && id.length() > 0) {
								final String className = destination.getProperty().getSource().getClassName();

								if (className != null && className.length() > 0) {
									try {
										services.put(id, Class.forName(className));
									} catch (ClassNotFoundException e) {
										error("Service Class not found check remoting-config.xml. ", e);
									}
									final String packageName = destination.getProperty().getSource().getPackageName();
									if (packageName != null && packageName.length() > 0) {
										packages.put(id, destination.getProperty().getSource().getPackageName());
										final Map<String, Class<?>> methods = new HashMap<>();
										for (ControllerMethod method : destination.getProperty().getSource()
												.controllerMethodArray()) {
											if (method.getRequestName() != null) {
												try {
													methods.put(method.getName(),
															Class.forName(method.getRequestName()));
												} catch (ClassNotFoundException e) {
													error("Class not found check remoting-config.xml. ", e);
												}
											}
										}
										controllerMethods.put(id, methods);
									} else {
										error("remote-config is not valid destination packageName missing.");
									}
								} else {
									error("remote-config is not valid destination classname missing.");
								}
							} else {
								error("remote-config is not valid destination does not include id.");
							}
						}
					} catch (Exception e) {
						error("Remote-config file is invalid. Unable to parse. ", e);
					}
				} else {
					error("Unable to open remoting configuration file: " + remoteConfig);
				}
			} else {
				error("Remoting configuration file not set in web xml ");
			}
		}
	}

	void error(String msg) {
		System.out.println(msg);
	}

	void error(String msg, Exception e) {
		System.out.println(msg + " " + e.getMessage());
	}
}
