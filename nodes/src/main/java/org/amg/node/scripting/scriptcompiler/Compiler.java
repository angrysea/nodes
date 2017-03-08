package org.amg.node.scripting.scriptcompiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;

import org.amg.node.scripting.ScriptBlock;
import org.amg.node.scripting.ScriptElement;
import org.amg.node.scripting.ScriptEngine;
import org.amg.node.scripting.ScriptParent;
import org.amg.node.scripting.ScriptStatement;
import org.amg.node.xmltools.xmlutils.IXMLInputSerializer;
import org.amg.node.xmltools.xmlutils.IXMLOutputSerializer;
import org.amg.node.xmltools.xmlutils.XMLSerializerFactory;

public class Compiler {

	private ScriptBlock rootBlock = null;
	private Expression rootExpression = null;
	private boolean bTerminate = false;

	public Compiler() {
	}

	public ScriptBlock load(String scriptFile, String xmlFile) {
		IXMLInputSerializer inserial;
		try {
			inserial = XMLSerializerFactory
					.getInputSerializer();
		inserial.setPackage("org.amg.sdk.scripting");
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(
				xmlFile));
		rootBlock = (ScriptBlock) inserial.get(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootBlock;
	}

	public void compile(ScriptBlock rootBlock, String xmlFile) {
		try {
			this.rootBlock = rootBlock;
			
			FileOutputStream o = new FileOutputStream(xmlFile);
			o.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
			o.write("<methods>\n".getBytes());

			try {
				Enumeration<ScriptElement> enumerator = rootBlock.children();
				if (enumerator.hasMoreElements()) {
					ScriptElement header = enumerator.nextElement();
					compileHeader((ScriptStatement) header);
					if (compile(enumerator)) {
						rootExpression.compile(o);
						IXMLOutputSerializer outserial = XMLSerializerFactory
								.getOutputSerializer();
						String xml = outserial.get(rootBlock);
						BufferedOutputStream os = new BufferedOutputStream(
								new FileOutputStream(xmlFile));
						os.write(xml.getBytes());
						os.close();
					}
				}
			} catch (Exception e) {
				throw e;
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	void compileHeader(ScriptStatement header) {
		try {
			ScriptElement headerElement = null;
			int i = 0;
			Enumeration<ScriptElement> enumerator = header.children();
			while (enumerator.hasMoreElements()) {
				if (i == 0) {
					enumerator.nextElement();
					i++;
				} else {
					headerElement = enumerator.nextElement();
					if (headerElement.getType() == ScriptEngine.VARIABLE) {
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	boolean compile(ScriptParent parent) {
		return compile(parent.children());
	}

	boolean compile(Enumeration<ScriptElement> enumerator) {
		boolean bExecuteNext = true;
		boolean bRet = true;

		while (enumerator.hasMoreElements()) {
			if (bTerminate == true)
				return bRet;

			ScriptElement element = (ScriptElement) enumerator.nextElement();
			int currentType = element.getType();
			switch (currentType) {
			case ScriptEngine.BLOCK:
				if (bExecuteNext == true) {
					ScriptBlock block = (ScriptBlock) element;
					bExecuteNext = compile(block);
				} else
					bExecuteNext = true;
				break;

			case ScriptEngine.STATEMENT:
				if (bExecuteNext == true) {
					ScriptStatement statement = (ScriptStatement) element;
					bExecuteNext = compile(statement);
				} else
					bExecuteNext = true;
				break;
			}
		}
		return bRet;
	}
}