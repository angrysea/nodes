package org.amg.node.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor extends JarFile {

	public JarExtractor(final File file) throws IOException {
		super(file);
	}

	public JarExtractor(final File file, final boolean verify) throws IOException {
		super(file, verify);
	}

	public JarExtractor(final String name) throws IOException {
		super(name);
	}

	public JarExtractor(final String name, final boolean verify) throws IOException {
		super(name, verify);
	}

	/**
	 * Retrieves the named entry as a byte array.
	 * 
	 * @return Returns a byte array containing the contents of the JarEntry or
	 *         null if the item is not found.
	 */
	public byte[] getJarEntryBytes(final String name) {
		byte[] bytes = null;
		final JarEntry jarEntry = getJarEntry(name);
		if (jarEntry != null) {
			try {
				final InputStream in = getInputStream(jarEntry);
				int len;
				final byte[] data = new byte[1024];
				final ByteArrayOutputStream buff = new ByteArrayOutputStream();
				while ((len = in.read(data)) != -1) {
					buff.write(data, 0, len);
				}

				// System.out.println("\nContents of JarEntry " +
				// jarEntry.getName() );
				// System.out.println("**** Begin Byte Array
				// ******************************");
				// buff.writeTo( System.out );
				// System.out.println("**** End Byte Array
				// ********************************");

				bytes = buff.toByteArray();

				in.close();
				buff.close();
			} catch (final IOException e) {
			}
		}
		return bytes;
	}
}