package org.amg.node.loader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathLoader extends ClassLoader {

	public static boolean bAutoReload = true;
	protected static final boolean DEBUG = true;
	private Hashtable<String, ClassInfo> classInfoMap = new Hashtable<String, ClassInfo>();
	private String classpath = "";

	public ClasspathLoader(final String classpath) {
		this.classpath = (classpath != null) ? classpath : "";
	}

	public ClasspathLoader(final String classpath, final ClassLoader parent) {
		super(parent);
		this.classpath = (classpath != null) ? classpath : "";
	}

	public synchronized String appendLocalClasspath(final String cp) {
		if (cp == null) {
			return "";
		}
		final StringBuilder buff = new StringBuilder();
		final String tokens[] = cp.split(File.pathSeparator);
		for (String path : tokens) {
			if (classpath.indexOf(path) == -1) {
				if (buff.length() > 0) {
					buff.append(File.pathSeparatorChar);
				}
				buff.append(path);
			}
		}

		final String newEntries = buff.toString();
		if (classpath.length() > 0) {
			classpath += File.pathSeparatorChar + newEntries;
		} else {
			classpath = newEntries;
		}
		return newEntries;
	}

	protected ClassInfo createClassInfo(final String className) {
		ClassInfo entry = null;
		File file = findClassFile(className);
		if (file != null) {
			entry = new ClassInfo(className, file);
		}
		return entry;
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		Class<?> c = findLocalClass(name);
		if (c == null) {
			throw new ClassNotFoundException(name);
		}
		return c;
	}

	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			c = findLocalClass(name);
			if (c == null) {
				c = super.loadClass(name, false);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	protected File findClassFile(final String className) {
		File file = null;
		try {
			final String zipEntryName = className.replace('.', '/') + ".class"; // Zip
			final String classFileName = className.replace('.', File.separatorChar) + ".class";
			final String tokens[] = classpath.split(File.pathSeparator);
			for(String filename : tokens) {
				final String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip") || filenameLower.endsWith(".jar")) {
					try {
						final JarFile jarFile = new JarFile(filename);
						final JarEntry j = jarFile.getJarEntry(zipEntryName);
						if (j != null) {
							file = new File(filename);
							break;
						}
						jarFile.close();
					} catch (IOException e) {
					}
				} else {
					if (!filename.endsWith(File.separator)) {
						filename += File.separatorChar;
					}

					File f = new File(filename + classFileName);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (final Exception e) {
		}
		return file;
	}

	protected ClassInfo findClassInfo(final String className) {
		return classInfoMap.get(className);
	}

	private Class<?> findLocalClass(final String name) {
		Class<?> cl = null;
		final ClassInfo entry = createClassInfo(name);
		if (entry != null) {
			cl = loadFromInfo(entry);
			if (cl != null) {
				classInfoMap.put(entry.getClassName(), entry);
			}
		}
		return cl;
	}

	public String getLocalClasspath() {
		return classpath;
	}

	private Class<?> loadFromInfo(final ClassInfo entry) {
		Class<?> cl = null;
		final byte[] bytes = entry.getClassBytes();
		if (bytes != null) {
			try {
				cl = this.defineClass(entry.getClassName(), bytes, 0, bytes.length);
			} catch (Throwable th) {
				trace("** Failed to defineClass:" + entry.getClassName() + "  Reason:" + th.getMessage());
			}
		}
		return cl;
	}

	protected boolean removeClassInfo(final String className) {
		return false;
	}

	private void trace(final String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}
}