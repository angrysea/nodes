package org.amg.node.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.amg.node.node.NodeRoot;

public class NodeFile {

	private static final String DEFAULT = "DefaultNodes.xml";
	private boolean bOpen = false;
	private File file;
	private boolean bDefault = false;
	private boolean bDirty = false;

	public boolean isOpen() {
		return bOpen;
	}

	public void setOpen(boolean bOpen) {
		this.bOpen = bOpen;
	}

	public boolean isDefault() {
		return bDefault;
	}

	public void setDefault(boolean bDefault) {
		this.bDefault = bDefault;
	}

	public boolean isDirty() {
		return bDirty;
	}

	public void setDirty(boolean bDirty) {
		this.bDirty = bDirty;
	}

	public void openFile(final String name) throws Exception {

	}

	public NodeRoot open(String name) throws Exception {

		if (name == null || name.length() == 0) {
			this.bDefault = true;
			name = DEFAULT;
		}

		NodeRoot tmpRoot = new NodeRoot();
		FileInputStream fis = null;
		try {
			if (name.equalsIgnoreCase(DEFAULT)) {
				File defaultfile = new File(name);
				bDefault = true;
				if (!file.exists()) {
					throw new IOException(
							"error Default node file does not exist.");
				}
				fis = new FileInputStream(defaultfile);
			} else {
				file = new File(name);

				if (!file.exists()) {
					new File(name.substring(0,
							name.lastIndexOf(File.separatorChar))).mkdirs();
					file.createNewFile();
				}
				fis = new FileInputStream(file);
				bOpen = true;
			}
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);

			tmpRoot.parseNodes(bytes);
			// Check to see if there are any nodes if not go to the default
			// node file that should have been included in the installation
			if (tmpRoot.count() < 1 && !bDefault) {
				return open(DEFAULT);
			}
			return tmpRoot;
		} catch (IOException x) {
			x.printStackTrace();
			throw x;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public void load(String name, NodeRoot root) throws Exception {

		if (name == null || name.length() == 0) {
			this.bDefault = true;
			name = DEFAULT;
		} else {
			FileInputStream fis = null;
			try {
				if (name.equalsIgnoreCase(DEFAULT)) {
					File defaultfile = new File(name);
					bDefault = true;
					if (!file.exists()) {
						throw new IOException(
								"error Default node file does not exist.");
					}
					try {
						fis = new FileInputStream(defaultfile);
					} catch (IOException x) {
						return;
					}
				} else {
					file = new File(name);

					if (!file.exists()) {
						new File(name.substring(0,
								name.lastIndexOf(File.separatorChar))).mkdirs();
						file.createNewFile();
					}
					fis = new FileInputStream(file);
					bOpen = true;
				}
				byte[] bytes = new byte[fis.available()];
				fis.read(bytes);

				root.parseNodes(bytes);
				// Check to see if there are any nodes if not go to the default
				// node file that should have been included in the installation
				try {
					if (root.count() < 1 && !bDefault) {
						load(DEFAULT, root);
					}
				} catch (IOException x) {
					// Do nothing
				}
			} catch (IOException x) {
				x.printStackTrace();
				throw x;
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		}
	}

	public void close() {
		bOpen = false;
	}

	public void save(String data) throws IOException {

		FileOutputStream fos = null;
		try {
			file.renameTo(new File(file.getName() + "~"));
			fos = new FileOutputStream(file);
			fos.write(data.getBytes());
			bDirty = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fos.close();
		}
	}
}
