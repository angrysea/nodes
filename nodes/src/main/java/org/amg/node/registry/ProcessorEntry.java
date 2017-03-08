package org.amg.node.registry;

public final class ProcessorEntry {

	private String name;
	private String description = null;
	private String type = null; // class or program id
	private String classpath = null;
	private String packageName = null;
	private boolean preLoad = false;

	public ProcessorEntry() {
		reset();
	}

	public ProcessorEntry(ProcessorEntry re) {
		reset();
		name = re.name;
		description = re.description;
		type = re.type;
		classpath = re.classpath;
		packageName = re.packageName;
		preLoad = re.preLoad;
	}

	private void reset() {
		name = null;
		description = null;
		type = null;
		classpath = null;
		packageName = null;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String toString() {
		return name;
	}

	public void setPreload(String p) {
		if (p.equals("1"))
			preLoad = true;
		else
			preLoad = false;
	}

	public String getPreload() {
		if (preLoad == false)
			return "0";
		else
			return "1";
	}

	public boolean isPreload() {
		return preLoad;
	}

	public void write(StringBuffer buffer) {
		buffer.append("<Processor>");
		buffer.append("<Name>");
		buffer.append(name);
		buffer.append("</Name>");
		buffer.append("<Description>");
		buffer.append(description);
		buffer.append("</Description>");
		buffer.append("<Type>");
		buffer.append(type);
		buffer.append("</Type>");
		buffer.append("<Classpath>");
		buffer.append(classpath);
		buffer.append("</Classpath>");
		buffer.append("<PackageName>");
		buffer.append(packageName);
		buffer.append("</PackageName>");
		buffer.append("<Preload>");
		buffer.append(getPreload());
		buffer.append("</Preload>");
		buffer.append("</Processor>");
	}
}
