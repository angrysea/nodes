package org.amg.node.xmltools.xmlconverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class XmlSchemaLoader {

	public static boolean bObjectAccessors = false;
	private String eol = System.getProperty("line.separator");
	static final public String XSDSCHEMA = "http://www.w3.org/1999/XMLSchema";
	static final public String DEFAULT_PARSER_NAME = "org.amg.sdk.xmltools.parser.XMLReader";
	private XsdSchemaLoader loader = null;

	public XmlSchemaLoader() {
	}

	public void xmlLoad(InputStream is) {
		try {
			InputStreamReader in = new InputStreamReader(is);
			StringBuffer buffer = new StringBuffer();
			char data[] = new char[4096];
			int nch;
			while ((nch = in.read(data, 0, data.length)) != -1)
				buffer.append(data, 0, nch);
			loader.xmlLoad(buffer.toString());
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	public void generateClasses(String rootDirectory, String packageName,
			String schemaFileName, boolean externalizable, boolean cachable) 
					throws Exception {

		try {
			File file = null;

			StringTokenizer st = new StringTokenizer(packageName, ".");
			String packagepath = new String();

			// st.nextToken();
			while (st.hasMoreTokens()) {
				packagepath += st.nextToken() + File.separator;
			}

			file = new File(rootDirectory + File.separator + packagepath);
			file.mkdirs();

			BufferedWriter b = null;
			if (externalizable) {
				b = new BufferedWriter(new FileWriter(
						new File(file.getAbsolutePath() + File.separator
								+ "BaseVO.java")));
				b.write(ElementType.writeBaseVO(packageName));
				b.write(eol);
				b.close();
			}
			
			String classData = null;
			loader = new XsdSchemaLoader();
			xmlLoad(new FileInputStream(schemaFileName));
			loader.start();
			while (true) {
				classData = loader.next(externalizable, cachable);
				if (classData == null) {
					break;
				}

				int start = classData.indexOf("class ");
				start += 6;
				int end = classData.indexOf(" ", start + 1);
				if (end < 0) {
					end = classData.indexOf("\n", start + 1);
				}
				String className = classData.substring(start, end);

				String fileName = file.getAbsolutePath() + File.separator
						+ className + ".java";
				File source = new File(fileName);
				b = new BufferedWriter(new FileWriter(source));

				b.write("// Title:        " + className + eol);
				b.write("// Author:       Generate code" + eol);
				b.write("// Company:      " + eol);
				b.write("// Description:  This class was generated by the XML-Broker Developer Console"
						+ eol);
				b.write("// Schema name:  " + schemaFileName + eol);
				b.write("// Java SDK:     ");

				b.write(eol + eol);

				if (packageName != null) {
					b.write("package " + packageName + ";" + eol + eol);
				}
				b.write(classData);
				b.write(eol);
				b.close();
			}
			loader.end();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error: [" + e.getMessage()
					+ "] occurred while processing the schema.  Aborted.\n");
		}
	}

	public static void main(String[] args) {
		try {
			if (args.length < 3) {
				throw new Exception("Invalid # of arguments");
			}
				
			boolean externalizable = false;
			boolean cachable = false;
			if (args.length == 4) {
				externalizable = Boolean.parseBoolean(args[3]);
			}
			
			if (args.length == 5) {
				cachable = Boolean.parseBoolean(args[4]);
			}
			
			new XmlSchemaLoader().generateClasses(
			/* rootDirectory */args[0],
			/* packageName */args[1],
			/* schemaFileName */args[2],
			externalizable,
			cachable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
