
package org.adaptinet.node.xmltools.xmlconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.adaptinet.node.xmltools.xmlutils.NameMangler;

public class ElementType extends XmlBase {

	private ArrayList<AttributeType> attributes = new ArrayList<AttributeType>();
	private Vector<ElementData> elements = new Vector<ElementData>();
	private StringBuffer ret = null;

	public ElementType() {
	}

	public final void putAttribute(AttributeType attrib) {
		try {
			if (attrib != null)
				attributes.add(attrib);
		} catch (Exception exception) {
		}
	}

	public final void putElement(ElementData element) {
		try {
			elements.add(element);
		} catch (Exception exception) {
		}
	}

	public final Iterator<ElementData> getElements() {
		return elements.iterator();
	}

	public final ElementData getElementByType(String type) {
		ElementData data = null;
		for (Iterator<ElementData> it = elements.iterator(); it.hasNext();) {
			data = it.next();
			if (data != null && data.getElementType().equals(type))
				return data;
		}
		return null;
	}

	public final String generateCode(boolean externalizable) {
		return generateCode(externalizable, false);
	}

	public final String generateCode(boolean externalizable, boolean cacheable) {

		String content = null;
		String mixed = null;
		String model = null;
		String base = null;
		boolean bText = false;
		boolean bContent = false;
		boolean bConstructor = true;

		try {
			String name = super.properties.getProperty("name");
			if (name != null) {
				name = NameMangler.encode(name);
				content = getProperty("content");
				mixed = getProperty("mixed");
				base = getProperty("base");
				base = NameMangler.encode(base);
				model = getProperty("model");
				if (model != null && model.equals("open")) {
					bText = true;
					bContent = true;
				} else if (content != null) {
					if (content.equals("empty")) {
						bContent = false;
						bText = false;
					} else if (content.equals("eltOnly")) {
						bContent = false;
						bText = false;
					} else if (content.equals("textOnly")) {
						bContent = false;
						bText = true;
					} else if (content.equals("mixed")) {
						bContent = false;
						bText = true;
					}
				} else if (mixed != null && mixed.equals("true")) {
					bContent = false;
					bText = true;
				} else {
					bText = false;
				}
				int size = elements.size();
				if (size > 0)
					bConstructor = false;

				boolean bAddIts = false;
				boolean bAddDate = false;
				boolean bHasArrays = false;
				boolean bHasNonJava = false;
				boolean bHasNonJavaArray = false;

				for (ElementData e : elements) {
					int maxOccurs = 0;
					if (e != null) {
						String property = e.getProperty("minOccurs");
						if (property != null && property.equals("0"))
							e.setOptional(true);
						property = e.getProperty("maxOccurs");
						if (property != null) {
							if (property.equals("?"))
								e.setOptional(true);
							if (property.equals("*")
									|| property.equals("unbounded")) {
								e.setMaxOne(false);
								e.setMaxOccurs(maxOccurs);
								bAddIts = true;
								bHasArrays = true;
							} else {
								maxOccurs = Integer.parseInt(property);
								if (maxOccurs == 1) {
									e.setMaxOne(true);
								} else {
									e.setMaxOne(false);
									e.setMaxOccurs(maxOccurs);
									bHasArrays = true;
								}
							}
						} else {
							e.setMaxOne(false);
							e.setMaxOccurs(maxOccurs);
							bHasArrays = true;
						}
						property = e.getProperty("order");
						if (property != null && property.equals("one"))
							e.setMaxOne(true);
						if (e.getElementTypeTag().equalsIgnoreCase("Date")) {
							bAddDate = true;
						} else if (nonNativeTypes.get(e.getElementTypeTag()) == null) {
							bHasNonJava = true;
							if (maxOccurs != 1) {
								bHasNonJavaArray = true;
							}
						}
					}
				}

				for (AttributeType a : attributes) {
					if (a == null)
						continue;
					String nativeType = a.convertType();
					if (nativeType != null
							&& nativeType.equalsIgnoreCase("Date")) {
						bAddDate = true;
					}
				}

				ret = new StringBuffer();

				if (bAddDate) {
					ret.append("import java.util.Date;\n");
				}

				if (bAddIts) {
					ret.append("import java.util.Iterator;\n");
					ret.append("import java.util.NoSuchElementException;\n");
				}

				if (externalizable) {
					ret.append("import java.io.ObjectInput;\n");
					ret.append("import java.io.ObjectOutput;\n");
					ret.append("import java.io.IOException;\n");
				}

				if (cacheable) {
					ret.append("import java.io.IOException;\n\n");
					ret.append("import org.adaptinet.sdk.exception.FastCacheException;\n");
					ret.append("import com.db.sdk.fastcache.CacheServer;\n");
					ret.append("import com.db.sdk.fastcache.DataArray;\n");
					ret.append("import com.db.sdk.fastcache.DataItem;\n");
					ret.append("import com.db.sdk.fastcache.FastCacheDVOBase;\n");
				}

				ret.append("\npublic class ");
				ret.append(name);
				if (base != null) {
					ret.append(" extends ");
					ret.append(base);
				} else if (externalizable) {
					ret.append(" extends BaseVO ");
				}

				if (cacheable) {
					ret.append(" implements FastCacheDVOBase ");
				}

				ret.append(" {\n");

				if (bText) {
					String type = convertType(getProperty("type"));
					if (type != null) {
						type = NameMangler.encode(type);
						if (bConstructor) {
							ret.append("\tpublic ");
							ret.append(name);
							ret.append("() {\n");
							ret.append("\t}\n");
							ret.append("\tpublic ");
							ret.append(name);
							ret.append("(");
							ret.append(type);
							ret.append(" _contentData) {\n");
							ret.append("\t\t this._contentData=_contentData;\n\t}\n");
						}
						ret.append("\tpublic ");
						ret.append(type);
						ret.append(" getContentData() {\n\t\treturn _contentData;\n\t}\n");
						ret.append("\tpublic void setContentData(");
						ret.append(type);
						ret.append(" newValue) {\n\t\t _contentData = newValue;\n\t}\n");
					} else {
						if (bConstructor) {
							ret.append("\tpublic ");
							ret.append(name);
							ret.append("() {\n");
							ret.append("\t}\n");
							ret.append("\tpublic ");
							ret.append(name);
							ret.append("(String _contentData) {\n");
							ret.append("\t\t this._contentData=_contentData;\n\t}\n");
						}
						ret.append("\tpublic String getContentData() {\n\t\treturn _contentData;\n\t}\n");
						ret.append("\tpublic void setContentData(String newValue) {\n\t\t_contentData = newValue;\n\t}\n");
					}
				}

				for (ElementData e : elements) {
					if (e != null) {
						String elementName = e.getElementType();
						elementName = NameMangler.encode(elementName);
						String elementType = e.getElementTypeTag();
						elementType = NameMangler.encode(elementType);
						if (elementName != null) {
							if (!e.getMaxOne()) {
								int maxOccurs = e.getMaxOccurs();
								ret.append("\tpublic long get");
								ret.append(elementName);
								ret.append("Count() { \n\t\treturn _");
								ret.append(elementName);
								ret.append("Count;\n\t}\n\tpublic ");
								ret.append(elementType);
								ret.append("[] get");
								ret.append(elementName);
								ret.append("Array() { \n\t\treturn _");
								ret.append(elementName);
								ret.append(";\n\t}\n\tpublic void set");
								ret.append(elementName);
								ret.append("Array(");
								ret.append(elementType);
								ret.append(" newValue[]) { \n\t\t_");
								ret.append(elementName);
								ret.append("=newValue;\n\t}\n");
								ret.append("\t@SuppressWarnings(\"rawtypes\")\n");
								ret.append("\tpublic Iterator get");
								ret.append(elementName);
								ret.append("Iterator() { \n\t\treturn new Iterator() {\n\t\t\tint cursor=0;\n\t\t\tint last=-1;\n");
								ret.append("\t\t\tpublic boolean hasNext() {\n\t\t\t\tif(_");
								ret.append(elementName);
								ret.append("==null) return false;\n\t\t\t\tif(last<0) {");
								ret.append("\n\t\t\t\t\tlast = _");
								ret.append(elementName);
								ret.append(".length;\n\t\t\t\t\twhile(--last>-1&&_");
								ret.append(elementName);
								ret.append("[last]==null);\n\t\t\t\t\tlast++;\n\t\t\t\t}");
								ret.append("\n\t\t\t\treturn cursor!=last;\n");
								ret.append("\t\t\t}\n\t\t\tpublic Object next() {\n\t\t\t\ttry {\n\t\t\t\t\treturn _");
								ret.append(elementName);
								ret.append("[cursor++];\n\t\t\t\t} catch(IndexOutOfBoundsException e) {");
								ret.append("\n\t\t\t\t\tthrow new NoSuchElementException();\n\t\t\t\t}\n\t\t\t}\n");
								ret.append("\t\t\tpublic void remove() {\n\t\t\t\ttry {\n\t\t\t\t\t_");
								ret.append(elementName);
								ret.append("[cursor++]=null;\n\t\t\t\t} catch(IndexOutOfBoundsException e) {");
								ret.append("\n\t\t\t\t\tthrow new NoSuchElementException();\n\t\t\t\t}\n\t\t\t}\n\t\t};\n\t}\n");
								String nativeType = convertToNativeType(elementType);
								if (nativeType == null
										|| XmlSchemaLoader.bObjectAccessors) {
									ret.append("\tpublic ");
									ret.append(elementType);
									ret.append(" get");
									ret.append(elementName);
									ret.append("(int idx) { \n\t\treturn (");
									ret.append(elementType);
									ret.append(")_");
									ret.append(elementName);
									ret.append("[idx];\n\t}\n\tpublic void set");
									ret.append(elementName);
									ret.append("(");
									ret.append(elementType);
									ret.append(" newValue) { \n\t\tif(_");
									ret.append(elementName);
									ret.append("!=null) {\n\t\t\tint __OPEN_A=-1;\n\t\t\tfor(int __I_A=0;__I_A<_");
									ret.append(elementName);
									ret.append(".length;__I_A++) {\n\t\t\t\tif(_");
									ret.append(elementName);
									ret.append("[__I_A]==null) {\n\t\t\t\t\t__OPEN_A=__I_A;\n\t\t\t\t\tbreak;\n\t\t\t\t}\n");
									ret.append("\t\t\t}\n\t\t\tif(__OPEN_A<0) {\n");
									if (maxOccurs > 0) {
										ret.append("\t\t\t\tthrow new IndexOutOfBoundsException();\n\t\t\t}\n");
									} else {
										ret.append("\t\t\t\t__OPEN_A=_");
										ret.append(elementName);
										ret.append(".length;\n\t\t\t\t");
										ret.append(elementType);
										ret.append(" array[] = new ");
										ret.append(elementType);
										ret.append("[__OPEN_A+10];\n\t\t\t\tSystem.arraycopy(_");
										ret.append(elementName);
										ret.append(",0,array,0,_");
										ret.append(elementName);
										ret.append(".length);\n\t\t\t\t_");
										ret.append(elementName);
										ret.append(" = array;\n\t\t\t}\n");
									}
									ret.append("\t\t\t_");
									ret.append(elementName);
									ret.append("[__OPEN_A] = newValue;\n\t\t}\n\t\telse {\n\t\t\t_");
									ret.append(elementName);
									ret.append(" = new ");
									ret.append(elementType);
									ret.append("[");
									if (maxOccurs > 0)
										ret.append(Integer.toString(maxOccurs));
									else
										ret.append("10");
									ret.append("];\n\t\t\t_");
									ret.append(elementName);
									ret.append("[0] = newValue;\n\t\t}\n");
									ret.append("\t\t_");
									ret.append(elementName);
									ret.append("Count++;\n\t}\n");

								} else {
									ret.append("\tpublic ");
									ret.append(nativeType);
									ret.append(" get");
									ret.append(elementName);
									ret.append("(int idx) { \n\t\treturn _");
									ret.append(elementName);
									ret.append("[idx].");
									ret.append(nativeType);
									ret.append("Value();\n\t}\n\tpublic void set");
									ret.append(elementName);
									ret.append("(");
									ret.append(nativeType);
									ret.append(" newValue) { \n\t\tif(_");
									ret.append(elementName);
									ret.append("!=null) {\n\t\t\tint __OPEN_A=-1;\n\t\t\tfor(int __I_A=0;__I_A<_");
									ret.append(elementName);
									ret.append(".length;__I_A++) {\n\t\t\t\tif(_");
									ret.append(elementName);
									ret.append("[__I_A]==null) {\n\t\t\t\t\t__OPEN_A=__I_A;\n\t\t\t\t\tbreak;\n\t\t\t\t}\n");
									ret.append("\t\t\t}\n\t\t\tif(__OPEN_A<0) {\n");
									if (maxOccurs > 0) {
										ret.append("\t\t\t\tthrow new IndexOutOfBoundsException();\n\t\t\t}\n");
									} else {
										ret.append("\t\t\t\t__OPEN_A=_");
										ret.append(elementName);
										ret.append(".length;\n\t\t\t\t");
										ret.append(elementType);
										ret.append(" array[] = new ");
										ret.append(elementType);
										ret.append("[__OPEN_A+10];\n\t\t\t\tSystem.arraycopy(_");
										ret.append(elementName);
										ret.append(",0,array,0,_");
										ret.append(elementName);
										ret.append(".length);\n\t\t\t\t_");
										ret.append(elementName);
										ret.append(" = array;\n\t\t\t}\n");
									}
									ret.append("\t\t\t_");
									ret.append(elementName);
									ret.append("[__OPEN_A] = new ");
									ret.append(elementType);
									ret.append("(newValue);\n\t\t}\n\t\telse {\n\t\t\t_");
									ret.append(elementName);
									ret.append(" = new ");
									ret.append(elementType);
									ret.append("[");
									if (maxOccurs > 0)
										ret.append(Integer.toString(maxOccurs));
									else
										ret.append("10");
									ret.append("];\n\t\t\t_");
									ret.append(elementName);
									ret.append("[0] = ");
									ret.append(elementType);
									ret.append(".valueOf(newValue);\n\t\t}\n\t}\n");
								}
							} else {
								String nativeType = convertToNativeType(elementType);
								if (nativeType == null
										|| XmlSchemaLoader.bObjectAccessors) {
									ret.append("\tpublic ");
									ret.append(elementType);
									ret.append(" get");
									ret.append(elementName);
									ret.append("() { \n\t\treturn ");
									ret.append("_");
									ret.append(elementName);
									ret.append(";\n\t}\n");
									ret.append("\tpublic void set");
									ret.append(elementName);
									ret.append("(");
									ret.append(elementType);
									ret.append(" newValue) { \n\t\t");
									ret.append("_");
									ret.append(elementName);
									ret.append(" = newValue;\n\t}\n");
								} else {
									ret.append("\tpublic ");
									ret.append(nativeType);
									ret.append(" get");
									ret.append(elementName);
									ret.append("() { \n\t\treturn _");
									ret.append(elementName);
									ret.append(".");
									ret.append(nativeType);
									ret.append("Value();\n\t}\n\tpublic void set");
									ret.append(elementName);
									ret.append("(");
									ret.append(nativeType);
									ret.append(" newValue) { \n\t\t_");
									ret.append(elementName);
									ret.append(" = ");
									ret.append(elementType);
									ret.append(".valueOf(newValue);\n\t}\n");
								}
							}
						}
					}
				}

				for (AttributeType a : attributes) {
					if (a != null) {
						a.generateCode(ret);
					}
				}

				if (bContent) {
					bConstructor = false;
					ret.append("\tpublic int getContentElementsSize() {\n\t\treturn _contentElements.size();\n\t}\n");
					ret.append("\tpublic Enumeration getContentElements() {\n\t\treturn _contentElements.elements();\n\t}\n");
					ret.append("\tpublic Object getContentElements(int i) {\n\t\treturn _contentElements.get(i);\n\t}\n");
					ret.append("\tpublic void setContentElements(Object newValue) {\n\t\t_contentElements.add(newValue);\n\t}\n");
				}

				if (externalizable) {

					ret.append("\tpublic void writeExternal(ObjectOutput out) throws IOException {\n");
					generateWrite(ret);
					for (AttributeType a : attributes) {
						if (a != null) {
							a.generateWrite(ret);
						}
					}
					ret.append("\t}\n");

					ret.append("\tpublic void readExternal(ObjectInput in) throws IOException,\n");
					ret.append("\t\t\tClassNotFoundException {\n");
					generateRead(ret);
					for (AttributeType a : attributes) {
						if (a != null) {
							a.generateRead(ret);
						}
					}
					ret.append("\t}\n");
				}

				if (cacheable) {

					ret.append("\tpublic void putToCache(String key, CacheServer server) throws FastCacheException {\n");
					ret.append("\t\tDataArray da = writeCache();\n");
					//ret.append("\t\tDataItem item = new DataItem();\n");
					ret.append("\t\titem.putDataArray(da);\n");
					ret.append("\t\tserver.putValue(key, item);\n");
					ret.append("\t\titem.DataItemClear();\n");
					ret.append("\t}\n");

					ret.append("\tpublic void getFromCache(String key, CacheServer server) throws FastCacheException {\n");
					ret.append("\t\tDataItem item = server.getValue(key);\n");
					ret.append("\t\tDataArray da = new DataArray();\n\t\titem.getDataArray(da);\n");
					ret.append("\t\treadCache(da);\n");
					ret.append("\t\titem.DataItemClear();\n");
					ret.append("\t}\n");

					ret.append("\tpublic DataArray writeCache() throws FastCacheException {\n");
					ret.append("\t\tint sa_idx = 0;\n");
					ret.append("\t\tDataArray da = new DataArray(DataItem.DATAITEMDATAITEM, ");
					ret.append(elements.size() + attributes.size());
					ret.append(");\n");
					//ret.append("\t\tDataItem item = new DataItem();\n");
					generateCacheWrite(ret);
					for (AttributeType a : attributes) {
						if (a != null) {
							a.generateCacheWrite(ret);
						}
					}
					ret.append("\t\titem.DataItemClear();\n");
					ret.append("\t\treturn da;\n");
					ret.append("\t}\n");

					ret.append("\tpublic void readCache(DataArray in) throws FastCacheException {\n");
					ret.append("\t\tint sa_idx = 0;\n");
					//ret.append("\t\tDataItem item = new DataItem();\n");
					if (bHasNonJava || bHasArrays) {
						ret.append("\t\tDataArray daItem = new DataArray();\n");
					}
					if (bHasNonJavaArray) {
						ret.append("\t\tDataArray daElement = new DataArray();\n");
					}

					generateCacheRead(ret);
					for (AttributeType a : attributes) {
						if (a != null) {
							a.generateCacheRead(ret);
						}
					}
					
					ret.append("\t}\n");
					ret.append("\tpublic void writeObject(byte [] data) throws FastCacheException {\n");
					//ret.append("\t\tDataItem item = new DataItem();\n");
					ret.append("\t\titem.writeObject(data);\n");
					ret.append("\t\tDataArray in = new DataArray();\n");
					ret.append("\t\titem.getDataArray(in);\n");
					ret.append("\t\treadCache(in);\n\t}\n");

					ret.append("\tpublic byte [] readObject() throws FastCacheException, IOException {\n");
					ret.append("\t\tDataArray requestDA = writeCache();\n");
					//ret.append("\t\tDataItem item = new DataItem();\n");
					ret.append("\t\titem.putDataArray(requestDA);\n");
					ret.append("\t\treturn item.readObject();\n\t}\n");

				}

				for (ElementData e : elements) {
					if (e != null) {
						String elementName = e.getElementType();
						elementName = NameMangler.encode(elementName);
						String elementType = e.getElementTypeTag();
						if (elementName != null) {
							ret.append("\tprivate ");
							if (e.getMaxOne()) {
								ret.append(elementType);
								ret.append(" _");
								ret.append(elementName);
								getInitInfo(elementType, e);
							} else {
								ret.append(elementType);
								ret.append(" _");
								ret.append(elementName);
								ret.append("[] = ");
								if (!e.getOptional()) {
									int maxOccurs = e.getMaxOccurs();
									if (maxOccurs == 0) {
										ret.append("new ");
										ret.append(elementType);
										ret.append("[10];\n");
									} else {
										ret.append("new ");
										ret.append(elementType);
										ret.append("[");
										ret.append(Integer.toString(maxOccurs));
										ret.append(

										"];\n");
									}
								} else {
									ret.append("null;\n");
								}
								ret.append("\tprivate long ");
								ret.append("_");
								ret.append(elementName);
								ret.append("Count = 0;\n");
							}
						}
					}
				}

				for (AttributeType a : attributes) {
					if (a != null)
						a.generateProperty(ret);
				}

				if (bContent) {
					ret.append("\tprivate ");
					ret.append("Vector _contentElements = new Vector();\n");
				}
				if (bText) {
					String type = getProperty("type");
					if (type != null) {
						String cType = convertType(type);
						ret.append("\tprivate ");
						cType = NameMangler.encode(cType);
						ret.append(cType);
						ret.append(" _contentData;\n");
					} else {
						ret.append("\tprivate ");
						ret.append("String _contentData;\n");
					}
				}
				if (cacheable) {
					ret.append("\tprivate DataItem item = new DataItem();\n");
				}
				ret.append("}\n");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return ret.toString();
	}

	public final void generateWrite(StringBuffer ret) {
		try {
			for (ElementData e : elements) {
				if (e != null) {
					String elementName = NameMangler.encode(e.getElementType());
					if (elementName != null) {
						String elementType = e.getElementTypeTag();
						String method = writeMethods.get(elementType);
						if (method == null)
							method = "out.writeObject(";
						ret.append("\t\t");
						if (e.getMaxOne()) {
							ret.append(method);
							ret.append("_");
							ret.append(elementName);
							ret.append(");\n");
						} else {
							ret.append("out.writeInt(");
							ret.append("_");
							ret.append(elementName);
							ret.append("Count);\n\t\tif(_");
							ret.append(elementName);
							ret.append("!=null) {\n");
							ret.append("\t\t\tfor(int __I_A=0;__I_A<_");
							ret.append(elementName);
							ret.append("Count;__I_A++) {\n");
							ret.append("\t\t\t\tif(_");
							ret.append(elementName);
							ret.append("[__I_A]==null) {\n\t\t\t\t\tbreak;\n\t\t\t\t}\n");
							ret.append("\t\t\t\tout.writeObject(_");
							ret.append(elementName);
							ret.append("[__I_A]);\n\t\t\t}\n\t\t}\n");
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateRead(StringBuffer ret) {
		try {
			for (ElementData e : elements) {
				if (e != null) {
					String elementName = NameMangler.encode(e.getElementType());
					if (elementName != null) {
						String elementType = e.getElementTypeTag();
						String method = readMethods.get(elementType);
						if (method == null)
							method = "in.readObject(";
						ret.append("\t\t");
						if (e.getMaxOne()) {
							ret.append("_");
							ret.append(elementName);
							ret.append(" = (");
							ret.append(elementType);
							ret.append(")");
							ret.append(method);
							ret.append(");\n");
						} else {
							ret.append("_");
							ret.append(elementName);
							ret.append("Count = in.readInt();\n");
							ret.append("\t\tif(_");
							ret.append(elementName);
							ret.append("Count>0) {\n");
							ret.append("\t\t\t_");
							ret.append(elementName);
							ret.append(" = new ");
							ret.append(elementType);
							ret.append(" [(int)_");
							ret.append(elementName);
							ret.append("Count];\n");
							ret.append("\t\t\tfor(int __I_A=0;__I_A<_");
							ret.append(elementName);
							ret.append("Count;__I_A++) {\n\t\t\t\t_");
							ret.append(elementName);
							ret.append("[__I_A] = (");
							ret.append(elementType);
							ret.append(")in.readObject();\n\t\t\t}\n\t\t}\n");
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateCacheWrite(StringBuffer ret) {
		try {
			for (ElementData e : elements) {
				if (e != null) {
					String elementName = NameMangler.encode(e.getElementType());
					if (elementName != null) {
						String elementType = e.getElementTypeTag();
						String type = cacheTypes.get(elementType);

						boolean isJava = true;
						if (type == null) {
							type = "DataItem";
							isJava = false;
						}
						if (e.getMaxOne()) {
							if (isJava) {
								ret.append("\t\titem.put");
								ret.append(type);
								ret.append("(_");
								ret.append(elementName);
								ret.append(");\n");
							} else {
								ret.append("\t\titem.putDataArray(_");
								ret.append(elementName);
								ret.append(".writeCache());\n");
							}
						} else {
							ret.append("\t\tif(_");
							ret.append(elementName);
							ret.append("!=null) {\n");
							ret.append("\t\t\tDataArray daChild = new DataArray(DataItem.DATAITEMARRAY, _");
							ret.append(elementName);
							ret.append("Count);\n");
							ret.append("\t\t\tfor(int __I_A=0;__I_A<_");
							ret.append(elementName);
							ret.append("Count;__I_A++) {\n\t\t\t\t");
							if (isJava) {
								ret.append("daChild.set");
								ret.append(type);
								ret.append("(__I_A, _");
								ret.append(elementName);
								ret.append("[__I_A]);\n\t\t\t}\n");
								ret.append("\t\t\titem.putDataArray(daChild);\n");
							} else {
								ret.append("daChild.setDataArray(__I_A, _");
								ret.append(elementName);
								ret.append("[__I_A].writeCache());\n\t\t\t}\n");
								ret.append("\t\t\titem.putDataArray(daChild);\n");
							}
							ret.append("\t\t}\n\t\telse {\n\t\t\titem.putEmpty();\n\t\t}\n");
						}
						ret.append("\t\tda.setDataItem(sa_idx++, item);\n");
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public final void generateCacheRead(StringBuffer ret) {
		try {
			for (ElementData e : elements) {
				if (e != null) {
					String elementName = NameMangler.encode(e.getElementType());
					if (elementName != null) {
						ret.append("\t\tin.getDataItem(sa_idx++, item);\n");
						String elementType = e.getElementTypeTag();
						String type = cacheTypes.get(elementType);
						boolean isJava = true;
						if (type == null) {
							type = "DataItem";
							isJava = false;
						}

						if (e.getMaxOne()) {
							if (isJava) {
								ret.append("\t\t_");
								ret.append(elementName);
								ret.append(" = item.get");
								ret.append(type);
								ret.append("();\n");
							} else {
								ret.append("\t\titem.getDataArray(daItem);\n\t\t_");
								ret.append(elementName);
								ret.append(".readCache(daItem);\n");
							}
						} else {
							ret.append("\t\titem.getDataArray(daItem);\n");
							ret.append("\t\t_");
							ret.append(elementName);
							ret.append("Count = daItem.getElements();\n");
							ret.append("\t\tif(_");
							ret.append(elementName);
							ret.append("Count>0) {\n");
							ret.append("\t\t\t_");
							ret.append(elementName);
							ret.append(" = new ");
							ret.append(elementType);
							ret.append(" [(int)_");
							ret.append(elementName);
							ret.append("Count];\n");
							ret.append("\t\t\tfor(int __I_A=0;__I_A<_");
							ret.append(elementName);
							ret.append("Count;__I_A++) {\n\t\t\t\t_");
							ret.append(elementName);
							ret.append("[__I_A] = new ");
							ret.append(elementType);
							ret.append("(");
							if (isJava) {
								ret.append("daItem.get");
								ret.append(type);
								ret.append("(__I_A));\n");
							} else {
								ret.append(");\n\t\t\t\tdaItem.getDataArray(__I_A, daElement);\n\t\t\t\t_");
								ret.append(elementName);
								ret.append("[__I_A].readCache(daElement);\n");
							}
							ret.append("\t\t\t}\n\t\t}\n");
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	boolean isJavaType(String className) {
		boolean bRet = false;
		if (className.startsWith("java.")) {
			bRet = true;
		} else {
			if (className.compareTo("String") == 0)
				bRet = true;
			else if (className.compareTo("boolean") == 0)
				bRet = true;
			else if (className.compareTo("byte") == 0)
				bRet = true;
			else if (className.compareTo("char") == 0)
				bRet = true;
			else if (className.compareTo("double") == 0)
				bRet = true;
			else if (className.compareTo("float") == 0)
				bRet = true;
			else if (className.compareTo("int") == 0)
				bRet = true;
			else if (className.compareTo("long") == 0)
				bRet = true;
			if (className.compareTo("short") == 0)
				bRet = true;
		}
		return bRet;
	}

	void getInitInfo(String className, ElementData e) {
		try {
			boolean bOptional = e.getOptional();
			String defaultValue = e.getProperty("default");

			if (defaultValue != null) {
				if (className.equals("String") || className.equals("char")) {
					defaultValue = '"' + e.getProperty("default") + '"';
				} else if (className.equals("Boolean")) {
					defaultValue = !defaultValue.equals("1")
							&& !defaultValue.equalsIgnoreCase("true") ? "false"
							: "true";
				}
			}
			ret.append(" = ");
			if (className.compareTo("boolean") == 0) {
				if (defaultValue == null)
					defaultValue = "false";
				ret.append(defaultValue);
			} else if (className.compareTo("byte") == 0) {
				if (defaultValue == null)
					defaultValue = "''";
				ret.append(defaultValue);
			} else if (className.compareTo("char") == 0) {
				if (defaultValue == null)
					defaultValue = "''";
				ret.append(defaultValue);
			} else if (className.compareTo("double") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(defaultValue);
			} else if (className.compareTo("float") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(defaultValue);
			} else if (className.compareTo("int") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(defaultValue);
			} else if (className.compareTo("long") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(defaultValue);
			} else if (className.compareTo("short") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(defaultValue);
			} else if (bOptional && defaultValue == null) {
				ret.append(" null");
			} else if (className.compareTo("Boolean") == 0) {
				if (defaultValue == null)
					defaultValue = "false";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Byte") == 0) {
				if (defaultValue == null)
					defaultValue = "''";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Character") == 0) {
				if (defaultValue == null)
					defaultValue = "''";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Double") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Float") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Integer") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Long") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else if (className.compareTo("Short") == 0) {
				if (defaultValue == null)
					defaultValue = "0";
				ret.append(" ");
				ret.append(className);
				ret.append(".valueOf(");
				ret.append(defaultValue);
				ret.append(")");
			} else {
				if (defaultValue == null)
					defaultValue = "";
				ret.append(" new ");
				ret.append(className);
				ret.append("(");
				ret.append(defaultValue);
				ret.append(")");
			}
			ret.append(";\n");
		} catch (Throwable throwable) {
		}
	}

	public final String getName() {
		return super.properties.getProperty("name");
	}

	public static final String convertType(String inType) {
		String outType = null;
		try {
			if (inType != null)
				outType = (String) dataTypes.get(inType);
			if (outType == null)
				outType = "String";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outType;
	}

	public static final String convertToNativeType(String inType) {
		String outType = null;
		try {
			if (inType != null)
				outType = (String) javaTypes.get(inType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outType;
	}

	public static final void insertType(String simpleType, String xmlType) {
		try {
			dataTypes.put(xmlType, convertType(xmlType));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final String writeBaseVO(String packageName) {
		StringBuffer sret = new StringBuffer();
		sret.append("// Title:        BaseVO\n");
		sret.append("// Author:       Generate code" + "\n");
		sret.append("// Company:      " + "\n");
		sret.append("// Description:  This class was generated by the XML-Broker Developer Console\n");
		sret.append("// Java SDK:     \n\n");
		sret.append("package ");
		sret.append(packageName);
		sret.append(";\n\n");
		sret.append("import java.io.Externalizable;\n");
		sret.append("import java.io.IOException;\n");
		sret.append("import java.io.ObjectInput;\n");
		sret.append("import java.io.ObjectOutput;\n");
		sret.append("import java.util.Date;\n");
		sret.append("import java.lang.reflect.Method;\n\n");
		sret.append("abstract public class BaseVO implements Externalizable{\n");
		sret.append("\tpublic static final String COMMA = \", \";\n\n");
		sret.append("\tpublic String toString(int indent) {\n");
		sret.append("\t\tStringBuffer lStr = new StringBuffer();\n\n");
		sret.append("\t\tindent(lStr, indent);\n");
		sret.append("\t\tlStr.append(getClass().getSimpleName()+\"[\");\n\n");
		sret.append("\t\ttry {\n");
		sret.append("\t\t\t@SuppressWarnings(\"rawtypes\")");
		sret.append("\t\t\tClass lDataCls = getClass();\n");
		sret.append("\t\t\tMethod[] lMethods = lDataCls.getDeclaredMethods();\n");
		sret.append("\t\t\tfor (int ii = 0; ii < lMethods.length; ii++) {\n");
		sret.append("\t\t\t\tMethod lMethod = lMethods[ii];\n");
		sret.append("\t\t\t\tString lMethodName = lMethod.getName();\n\n");
		sret.append("\t\t\t\tif (((lMethodName.startsWith(\"get\") == false)\n");
		sret.append("\t\t\t\t\t&& (lMethodName.startsWith(\"is\") == false))\n");
		sret.append("\t\t\t\t\t|| (lMethod.getParameterTypes().length != 0)\n");
		sret.append("\t\t\t\t\t|| (lMethodName.equals(\"get\")) || (lMethodName.equals(\"is\"))) {\n");
		sret.append("\t\t\t\t\tcontinue;\n\t\t\t\t}\n\n");
		sret.append("\t\t\t\tObject lRet = lMethod.invoke(this, (Object[])null);\n\n");
		sret.append("\t\t\t\tString lAttrName = null;\n\n");
		sret.append("\t\t\t\tif (lMethodName.startsWith(\"get\")) {\n");
		sret.append("\t\t\t\t\tlAttrName = lMethodName.substring(3, 4).toLowerCase();\n\n");
		sret.append("\t\t\t\t\tif (lMethodName.length() >= 4) {\n");
		sret.append("\t\t\t\t\t\tlAttrName += lMethodName.substring(4);\n");
		sret.append("\t\t\t\t\t}\n\t\t\t\t}\n");
		sret.append("\t\t\t\telse {\n");
		sret.append("\t\t\t\t\tlAttrName = lMethodName.substring(2, 3).toLowerCase();\n");
		sret.append("\t\t\t\t\tif (lMethodName.length() >= 3) {\n");
		sret.append("\t\t\t\t\t\tlAttrName += lMethodName.substring(3);\n");
		sret.append("\t\t\t\t\t}\n\t\t\t\t}\n\n");
		sret.append("\t\t\t\tif (lRet instanceof BaseVO) {\n");
		sret.append("\t\t\t\t\tBaseVO lValObj = (BaseVO) lRet;\n");
		sret.append("\t\t\t\t\tindent(lStr, indent + 1);\n");
		sret.append("\t\t\t\t\tlStr.append(lAttrName + \" = \" + COMMA);\n");
		sret.append("\t\t\t\t\tlStr.append(lValObj.toString(indent + 2));\n");
		sret.append("\t\t\t\t\tlStr.append(COMMA);\n");
		sret.append("\t\t\t\t}\n");
		sret.append("\t\t\t\telse {\n");
		sret.append("\t\t\t\t\tindent(lStr, indent + 1);\n");
		sret.append("\t\t\t\t\tlStr.append(\"\\r\\n\" + lAttrName + \" = \" + lRet + COMMA);\n");
		sret.append("\t\t\t\t}\n");
		sret.append("\t\t\t}\n");
		sret.append("\t\t}\n");
		sret.append("\t\tcatch (Exception excp) {\n");
		sret.append("\t\t\tlStr.append(\"Error in converting to string ....\" + COMMA);\n");
		sret.append("\t\t}\n\n");
		sret.append("\t\tindent(lStr, indent);\n");
		sret.append("\t\tlStr.append(\"]\");\n\n");
		sret.append("\t\treturn lStr.toString();\n");
		sret.append("\t}\n\n");
		sret.append("\tpublic String toString() {\n");
		sret.append("\t\treturn toString(0);\n");
		sret.append("\t}\n\n");
		sret.append("\tpublic static void indent(StringBuffer str) {\n");
		sret.append("\t\tindent(str, 1);\n");
		sret.append("\t}\n\n");
		sret.append("\tpublic static void indent(StringBuffer str, int level) {\n");
		sret.append("\t\tfor (int ii = 0; ii < level; ii++) {\n");
		sret.append("\t\t\tstr.append(\"\");\n");
		sret.append("\t\t}\n\t}\n");

		sret.append("\tpublic static void writeString(ObjectOutput out, char [] value) throws IOException {\n");
		sret.append("\t\tint len = 0;\n");
		sret.append("\t\tif(value!=null)\n");
		sret.append("\t\t\tlen = value.length;\n");
		sret.append("\t\tout.writeInt(len);\n");
		sret.append("\t\tfor(int i = 0; i<len; i++ ) {\n");
		sret.append("\t\t\tout.writeChar(value[i]);\n");
		sret.append("\t\t}\n\t}\n");

		sret.append("\tpublic static char [] readString(ObjectInput in) throws IOException {\n");
		sret.append("\t\tchar [] value = null;\n");
		sret.append("\t\tint len = in.readInt();\n");
		sret.append("\t\tif(len > 0 ) {\n");
		sret.append("\t\t\tvalue = new char[len];\n");
		sret.append("\t\t\tfor(int i = 0; i<len; i++ ) {\n");
		sret.append("\t\t\t\tvalue[i] = in.readChar();\n");
		sret.append("\t\t\t}\n\t\t}\n\t\treturn value;\n\t}\n");

		sret.append("\tpublic static void writeDate(ObjectOutput out, Date value) throws IOException {\n");
		sret.append("\t\tout.writeLong(value.getTime());\n");
		sret.append("\t}\n");

		sret.append("\tpublic static Date readDate(ObjectInput in) throws IOException {\n");
		sret.append("\t\treturn new Date(in.readLong());\n\t}\n}\n");

		return sret.toString();
	}

	protected static HashMap<String, String> dataTypes;
	protected static HashMap<String, String> javaTypes;
	protected static HashMap<String, String> cacheTypes;
	protected static HashMap<String, String> dataItemTypes;
	protected static HashMap<String, String> writeMethods;
	protected static HashMap<String, String> readMethods;
	protected static HashMap<String, String> nonNativeTypes;

	static {
		dataTypes = new HashMap<String, String>();
		dataTypes.put("boolean", "Boolean");
		dataTypes.put("string", "String");
		dataTypes.put("enumeration", "String");
		dataTypes.put("string.ansi", "String");
		dataTypes.put("fixed.14.4", "Float");
		dataTypes.put("float", "Float");
		dataTypes.put("i4", "Integer");
		dataTypes.put("long", "Long");
		dataTypes.put("unsignedLong", "Long");
		dataTypes.put("short", "Short");
		dataTypes.put("unsignedShort", "Short");
		dataTypes.put("double", "Double");
		dataTypes.put("date", "Date");
		dataTypes.put("dateTime", "Date");
		dataTypes.put("dateTime.iso8601", "Date");
		dataTypes.put("dateTime.iso8601.tz", "Date");
		dataTypes.put("date.iso8601", "Date");
		dataTypes.put("time.iso8601", "Date");
		dataTypes.put("time.iso8601.tz", "Date");
		dataTypes.put("number", "String");
		dataTypes.put("int", "Integer");
		dataTypes.put("integer", "Integer");
		dataTypes.put("i1", "Byte");
		dataTypes.put("i2", "Short");
		dataTypes.put("i4", "Integer");
		dataTypes.put("i8", "Long");
		dataTypes.put("ui1", "Byte");
		dataTypes.put("ui2", "Short");
		dataTypes.put("ui4", "Integer");
		dataTypes.put("positiveInteger", "Integer");
		dataTypes.put("nonPositiveInteger", "Integer");
		dataTypes.put("negativeInteger", "Integer");
		dataTypes.put("nonNegativeInteger", "Integer");
		dataTypes.put("ui8", "Long");
		dataTypes.put("r4", "Float");
		dataTypes.put("r8", "Double");
		dataTypes.put("decimal", "Double");
		dataTypes.put("float.IEEE.754.32", "Float");
		dataTypes.put("float.IEEE.754.64", "Double");
		dataTypes.put("char", "Character");
		dataTypes.put("bin.hex", "String");
		dataTypes.put("uri", "String");
		dataTypes.put("uuid", "String");
		dataTypes.put("uriReference", "String");

		javaTypes = new HashMap<String, String>();
		javaTypes.put("Float", "float");
		javaTypes.put("Integer", "int");
		javaTypes.put("Long", "long");
		javaTypes.put("Short", "short");
		javaTypes.put("Double", "double");
		javaTypes.put("Boolean", "boolean");
		javaTypes.put("Byte", "byte");
		javaTypes.put("Character", "char");

		nonNativeTypes = new HashMap<String, String>();
		nonNativeTypes.put("Float", "float");
		nonNativeTypes.put("Integer", "int");
		nonNativeTypes.put("Long", "long");
		nonNativeTypes.put("Short", "short");
		nonNativeTypes.put("Double", "double");
		nonNativeTypes.put("Boolean", "boolean");
		nonNativeTypes.put("Byte", "byte");
		nonNativeTypes.put("Character", "char");
		nonNativeTypes.put("String", "String");
		nonNativeTypes.put("Date", "Date");

		cacheTypes = new HashMap<String, String>();
		cacheTypes.put("Boolean", "Boolean");
		cacheTypes.put("String", "String");
		cacheTypes.put("Float", "Float");
		cacheTypes.put("Integer", "Int");
		cacheTypes.put("Long", "Long");
		cacheTypes.put("Short", "Short");
		cacheTypes.put("Double", "Double");
		cacheTypes.put("Date", "Date");
		cacheTypes.put("Byte", "Byte");
		cacheTypes.put("Character", "Byte");

		dataItemTypes = new HashMap<String, String>();
		dataItemTypes.put("Boolean", "DATAITEMBOOLEAN");
		dataItemTypes.put("String", "DATAITEMSTRING");
		dataItemTypes.put("Float", "DATAITEMFLOAT");
		dataItemTypes.put("Integer", "DATAITEMINT");
		dataItemTypes.put("Long", "DATAITEMLONG");
		dataItemTypes.put("Short", "DATAITEMSHORT");
		dataItemTypes.put("Double", "DATAITEMDOUBLE");
		dataItemTypes.put("Date", "DATAITEMDATE");
		dataItemTypes.put("Byte", "DATAITEMBYTE");
		dataItemTypes.put("Character", "DATAITEMBYTE");

		writeMethods = new HashMap<String, String>();
		writeMethods.put("Boolean", "out.writeBoolean(");
		writeMethods.put("String", "out.writeUTF(");
		writeMethods.put("Float", "out.writeFloat(");
		writeMethods.put("Integer", "out.writeInt(");
		writeMethods.put("Long", "out.writeLong(");
		writeMethods.put("Short", "out.writeShort(");
		writeMethods.put("Double", "out.writeDouble(");
		writeMethods.put("Date", "writeDate(out, ");
		writeMethods.put("Byte", "out.writeChar(");
		writeMethods.put("Character", "out.writeChar(");

		readMethods = new HashMap<String, String>();
		readMethods.put("Boolean", "in.readBoolean(");
		readMethods.put("String", "in.readUTF(");
		readMethods.put("Float", "in.readFloat(");
		readMethods.put("Integer", "in.readInt(");
		readMethods.put("Long", "in.readLong(");
		readMethods.put("Short", "in.readShort(");
		readMethods.put("Double", "in.readDouble(");
		readMethods.put("Date", "readDate(in");
		readMethods.put("Byte", "in.readChar(");
		readMethods.put("Character", "in.readChar(");
	}
}