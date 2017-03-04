package org.adaptinet.node.scripting;

import java.util.Stack;
import java.util.HashMap;
import java.util.Enumeration;
import java.io.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ScriptEngine {

	static final public int UNKNOWN = 0;
	static final public int EQUAL = 1;
	static final public int INEQUALITY = 2;
	static final public int GREATER = 3;
	static final public int OR = 4;
	static final public int AND = 5;
	static final public int LESS = 6;
	static final public int OPENCURLY = 7;
	static final public int CLOSECURLY = 8;
	static final public int OPENPAREN = 9;
	static final public int CLOSEPAREN = 10;
	static final public int OPENBRACKET = 11;
	static final public int CLOSEBRACKET = 12;
	static final public int SEMICOLON = 13;
	static final public int COLON = 14;
	static final public int DOT = 15;
	static final public int PLUS = 16;
	static final public int MINUS = 17;
	static final public int MULTIPLY = 18;
	static final public int DIVIDE = 19;
	static final public int IF = 201;
	static final public int ELSE = 202;
	static final public int SWITCH = 203;
	static final public int CASE = 204;
	static final public int BREAK = 205;
	static final public int RETURN = 206;
	static final public int CONTINUE = 207;
	static final public int EXIT = 208;
	static final public int DO = 209;
	static final public int WHILE = 210;
	static final public int GOTO = 211;
	static final public int EXECUTE = 212;
	static final public int ELEMENT = 213;
	static final public int ATTRIBUTE = 214;
	static final public int VAR = 215;
	static final public int CONTENT = 216;
	static final public int FOR = 217;
	static final public int DEFAULT = 218;
	static final public int NEW = 219;
	static final public int BLOCK = 301;
	static final public int STATEMENT = 302;
	static final public int KEYWORD = 303;
	static final public int OPERATOR = 304;
	static final public int VARIABLE = 305;
	static final public int LITERAL = 306;
	static final public int STACK = 307;
	static final public int METHOD = 308;

	private int prevtype = 0;
	private int currentType = 0;
	private ScriptBlock rootBlock = null;
	private HashMap<String, ScriptBlock> blocks = new HashMap<String, ScriptBlock>();
	private Stack stack = new Stack();
	private Stack callstack = new Stack();
	private HashMap lookup = new HashMap();
	private boolean buildcallstack = false;
	private boolean bDefineVariable = false;
	private int mathcode = 0;
	private ScriptBlock block = null;
	private ScriptStatement statement = null;
	private int callertype = 0;
	private ScriptValue switchvalue = null;
	private ScriptRunner runner = null;
	private ScriptValue ret = null;
	private int not = 1;
	private int opcode = 0;
	private boolean bTerminate = false;
	private Enumeration rootEnumerator = null;
	private int pluses = 0;
	private int minuses = 0;
	private int stars = 0;
	private int slashes = 0;
	private ScriptParent dostatement = null;

	public ScriptEngine() {
	}

	public Object execute(ScriptRunner runner, String function) throws Exception {
		try {
			ScriptBlock block = blocks.get(function);
			this.runner = runner;
			//Enumeration<ScriptElement> enumerator = rootBlock.children();
			Enumeration<ScriptElement> enumerator = block.children();
			if (enumerator.hasMoreElements()) {
				ScriptElement header = enumerator.nextElement();
				processHeader((ScriptStatement) header);
				if (process(enumerator) == false) {
					ret = null;
				}
			}
		} catch (Exception e) {
			runner.error("Unknown error executing script: " + e.getMessage());
			throw e;
		}
		return ret.getObject();
	}

	void processHeader(ScriptStatement header) {
		try {
			ScriptElement headerElement = null;
			int i = 0;
			Enumeration enumerator = header.children();
			while (enumerator.hasMoreElements()) {
				if (i == 0) {
					enumerator.nextElement();
					i++;
				} else {
					headerElement = (ScriptElement) enumerator.nextElement();
					if (headerElement.getType() == VARIABLE) {
						ScriptVariable v = (ScriptVariable) headerElement;
						v.setObject(runner.getValue(v.getName()));
						lookup.put(v.getName(), v);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	boolean process(ScriptParent parent) {
		return process(parent.children());
	}

	boolean process(Enumeration enumerator) {
		
		boolean bExecuteNext = true;
		boolean bRet = true;
		int parens = 0;
		int or = 0;
		int and = 0;
		int dots = 0;
		
		while (enumerator.hasMoreElements()) {
			if (bTerminate == true)
				return bRet;

			ScriptElement element = (ScriptElement) enumerator.nextElement();
			prevtype = currentType;
			currentType = element.getType();
			try {

				switch (currentType) {
				case BLOCK:
					if (bExecuteNext == true) {
						stack.clear();
						block = (ScriptBlock) element;
						bExecuteNext = process(block);
					} else
						bExecuteNext = true;
					break;

				case STATEMENT:
					if (bExecuteNext == true) {
						statement = (ScriptStatement) element;
						rootEnumerator = enumerator;
						bExecuteNext = process(statement);
					} else
						bExecuteNext = true;
					break;

				case METHOD: {
					ScriptMethod method = new ScriptMethod(
							((ScriptMethod) element).getName());
					String s = getMethodName(method, dots);
					dots = 0;
					if (s != null) {
						method.setName(s);
					}
					stack.push(method);
					break;
				}

				case VARIABLE: {
					ScriptValue variable = new StackVariable(
							(ScriptValue) element);
					if (bDefineVariable) {
						stack.push(variable);
						String varName = getName(dots);
						dots = 0;
						variable.setName(varName);
						lookup.put(varName, variable);
						stack.push(variable);
						bDefineVariable = false;
					} else {
						Stack tstack = null;
						if (buildcallstack)
							tstack = callstack;
						else
							tstack = stack;

						tstack.push(variable);
						String s = getName(dots);
						dots = 0;
						if (s != null) {
							variable.setName(s);
						}
						ScriptValue temp = (ScriptValue) lookup.get(variable
								.getName());
						if (temp == null) {
							variable.setObject(runner.getValue(variable
									.getName()));
						} else {
							variable = temp;
						}

						if (pluses == 2) {
							incdec(variable);
							tstack.push(variable);
							pluses = 0;
						} else if (minuses == 2) {
							incdec(variable);
							tstack.push(variable);
							minuses = 0;
						} else if (tstack.empty() == false) {
							if (opcode == 1) {
								temp = (ScriptValue) tstack.pop();
								tstack.push(variable);
								opcode = 0;
								process(enumerator);
								if (tstack.empty() == false) {
									ScriptValue v = (ScriptValue) tstack.pop();
									if (pluses == 1) {
										pluses = 0;
										appendto(temp, v);
										tstack.push(temp);
									} else if (minuses == 1) {
										minuses = 0;
										temp.setObject(subtractFrom(temp, v));
										tstack.push(temp);
									} else if (stars == 1) {
										stars = 0;
										temp.setObject(multiply(temp, v));
										tstack.push(temp);
									} else if (slashes == 1) {
										stars = 0;
										temp.setObject(divide(temp, v));
										tstack.push(temp);
									} else {
										temp.setObject(v.getObject());
										tstack.push(temp);
									}
								}
							} else if (opcode != 0) {
								temp = (ScriptValue) stack.pop();
								int topcode = opcode;
								int tnot = not;
								opcode = 0;
								not = 1;
								stack.push(temp);
								process(enumerator);
								if (stack.empty() == false) {
									Object o = stack.pop();
									stack.push(temp);
									stack.push(o);
									bRet = compareValues(topcode * tnot);
								} else
									bRet = false;
							} else {
								if (mathcode > 0) {
									if (pluses == 1) {
										temp = (ScriptValue) stack.pop();
										pluses = 0;
										Object results = addto(variable, temp);
										if (results != null) {
											ScriptVariable r = new ScriptVariable(
													"return");
											r.setObject(results);
											stack.push(r);
										}
										callstack.clear();
									} else if (minuses == 1) {
										temp = (ScriptValue) stack.pop();
										pluses = 0;
										Object results = subtractFrom(variable,
												temp);
										if (results != null) {
											ScriptVariable r = new ScriptVariable(
													"return");
											r.setObject(results);
											stack.push(r);
										}
										callstack.clear();
									} else if (stars == 1) {
										temp = (ScriptValue) stack.pop();
										stars = 0;
										Object results = multiply(variable,
												temp);
										if (results != null) {
											ScriptVariable r = new ScriptVariable(
													"return");
											r.setObject(results);
											stack.push(r);
										}
										callstack.clear();
									} else if (slashes == 1) {
										temp = (ScriptValue) stack.pop();
										stars = 0;
										Object results = divide(variable, temp);
										if (results != null) {
											ScriptVariable r = new ScriptVariable(
													"return");
											r.setObject(results);
											stack.push(r);
										}
										callstack.clear();
									}
								} else {
									tstack.push(variable);
								}
							}
						} else {
							tstack.push(variable);
						}
					}
					break;
				}

				case LITERAL: {
					ScriptLiteral literal = (ScriptLiteral) element;
					if (buildcallstack) {
						callstack.push(literal);
					} else if (stack.empty() == false) {
						if (opcode == 1) {
							opcode = 0;
							ScriptValue variable = (ScriptValue) stack.pop();
							if (pluses == 1) {
								pluses = 0;
								appendto(variable, literal);
							} else {
								variable.setObject(literal.getObject());
							}

							if (lookup.get(variable.getName()) == null) {
								String varName = getName(dots);
								dots = 0;
								variable.setName(varName);
								runner.setValue(varName, variable.getObject());
							} else {
								lookup.put(variable.getName(), variable);
							}
							stack.push(variable);
						} else if (opcode != 0) {
							stack.push(element);
							bRet = compareValues(opcode * not);
							not = 1;
							opcode = 0;
						} else {
							stack.push(element);
						}
					} else
						stack.push(element);
					break;
				}

				case KEYWORD: {
					ScriptKeyword keyWord = (ScriptKeyword) element;
					currentType = ScriptKeyword.getKeywordType(keyWord
							.getValue());
					switch (currentType) {
					case FOR:
						callertype = currentType;
						bRet = true;

						// burn the paren and process the initialization
						if (enumerator.hasMoreElements()) {
							enumerator.nextElement();
							process(enumerator);
						}

						ScriptParent incstatement = null;
						ScriptParent teststatement = null;
						ScriptParent execstatement = null;

						if (rootEnumerator.hasMoreElements()) {
							teststatement = (ScriptParent) rootEnumerator
									.nextElement();
							if (rootEnumerator.hasMoreElements()) {
								incstatement = (ScriptParent) rootEnumerator
										.nextElement();
								if (rootEnumerator.hasMoreElements())
									execstatement = (ScriptParent) rootEnumerator
											.nextElement();
							}
						}

						if (incstatement == null || teststatement == null
								|| execstatement == null)
							return false;

						boolean bContinue = true;
						while (bContinue == true) {
							callstack.clear();
							stack.clear();
							if ((bContinue = process(teststatement)))
								if ((bRet = process(execstatement))) {
									callertype = FOR;
									process(incstatement);
								}
						}
						break;

					case WHILE:
						callertype = currentType;
						bRet = true;

						teststatement = statement;
						if (dostatement == null) {
							if (rootEnumerator.hasMoreElements())
								dostatement = (ScriptParent) rootEnumerator
										.nextElement();
						}

						if (teststatement == null || dostatement == null)
							return false;

						bContinue = true;
						Enumeration whileenum = null;
						while (bContinue == true) {
							callstack.clear();
							stack.clear();
							dots = 0;
							whileenum = teststatement.children();
							if (whileenum.hasMoreElements()) {
								whileenum.nextElement();
								if ((bContinue = process(whileenum)))
									if ((bContinue = process(dostatement)))
										callertype = WHILE;
							} else {
								bContinue = false;
							}
						}
						dostatement = null;
						return true;

					case DO:
						callertype = currentType;
						bRet = true;

						if (rootEnumerator.hasMoreElements()) {
							dostatement = (ScriptParent) rootEnumerator
									.nextElement();
						}

						if (dostatement == null)
							return false;

						callstack.clear();
						stack.clear();
						dots = 0;
						bRet = process(dostatement);
						break;

					case IF:
						callertype = currentType;
						bRet = process(enumerator);
						break;

					case ELSE:
						if (bRet == false)
							bRet = true;
						else
							bRet = false;
						break;

					case SWITCH:
						process(enumerator);
						if (stack.empty() == false)
							switchvalue = (ScriptValue) stack.pop();
						while (enumerator.hasMoreElements())
							process(enumerator);
						break;

					case CASE:
						if (process(enumerator) == false) {
							while (enumerator.hasMoreElements())
								enumerator.nextElement();
						}
						break;

					case DEFAULT:
						if (switchvalue != null) {
							while (enumerator.hasMoreElements())
								enumerator.nextElement();
						}
						break;

					case BREAK:
						return bRet;

					case EXIT:
						bTerminate = true;
						return false;

					case RETURN:
						process(enumerator);
						String name = getName(dots);
						dots = 0;
						ret = (ScriptValue) lookup.get(name);
						bTerminate = true;
						return false;

					case CONTINUE:
						return true;

					case GOTO:
						break;

					case EXECUTE:
						process(enumerator);
						break;

					case ELEMENT:
					case ATTRIBUTE:
					case VAR:
						bDefineVariable = true;
						break;
					default:
						break;
					}
					break;
				}

				case OPERATOR: {
					ScriptOperator soperator = (ScriptOperator) element;
					currentType = ScriptOperator.getType(soperator.getValue());
					switch (currentType) {
					case OPENPAREN:
						parens++;
						if (prevtype == METHOD) {
							callertype = EXECUTE;
							buildcallstack = true;
						}
						break;

					case CLOSEPAREN:
						parens--;
						if (callertype == IF) {
							if (parens == 0)
								return bRet;
						} else if (callertype == SWITCH) {
							if (stack.empty() == false) {
								switchvalue = (ScriptValue) stack.pop();
							}
						} else if (callertype == EXECUTE) {
							ScriptMethod caller = null;
							dots = 0;

							if (stack.empty() == false) {
								caller = (ScriptMethod) stack.pop();
								if (caller != null) {
									Stack parameters = new Stack();
									while (callstack.empty() == false) {
										ScriptValue v = (ScriptValue) callstack
												.pop();
										parameters.push(v.getObject());
									}

									try {
										bRet = true;
										Object retValue = runner.execute(caller
												.getName(), parameters);
										if (retValue != null) {
											ScriptVariable r = new ScriptVariable(
													"return");
											r.setObject(retValue);
											stack.push(r);
										}
										callstack.clear();
									} catch (Exception e) {
										StringBuilder sb = new StringBuilder();
										element.dump(sb);
										runner
												.error("Error processing element: "
														+ sb.toString());
										bRet = false;
									}
								}
							}
							buildcallstack = false;
						}
						break;

					case OPENBRACKET:
						process(enumerator);
						Stack tstack = null;
						if (buildcallstack)
							tstack = callstack;
						else
							tstack = stack;

						if (tstack.empty() == false) {
							ScriptValue subscript = (ScriptValue) tstack.pop();
							ScriptValue temp = (ScriptValue) lookup
									.get(subscript.getName());
							if (temp != null)
								subscript = temp;
							String s = getName(dots);
							dots = 0;
							ScriptVariable sv = new ScriptVariable(s);
							sv.setObject(runner.getValue(s, Integer
									.parseInt(subscript.toString())));
							tstack.push(sv);
						}
						break;

					case CLOSEBRACKET:
						return bRet;

					case GREATER:
						opcode += 6;
						break;

					case EQUAL:
						opcode++;
						break;

					case LESS:
						opcode += 3;
						break;

					case INEQUALITY:
						not = -1;
						break;

					case OR:
						or++;
						if (or == 2) {
							if (bRet == false)
								bRet = process(enumerator);
							else
								return bRet;
						}
						break;

					case AND:
						and++;
						if (and == 2) {
							if (bRet == true)
								bRet = process(enumerator);
							else
								return bRet;
						}
						break;

					case COLON:
						bRet = false;
						if (switchvalue != null && stack.empty() == false) {
							ScriptValue v = (ScriptValue) stack.pop();
							if (switchvalue.compareTo(v) == 0) {
								switchvalue = null;
								bRet = true;
							}
						}
						return bRet;

					case DOT:
						dots++;
						break;

					case PLUS:
						pluses++;
						if (pluses == 1) {
							mathcode = 2;
						} else if (pluses == 2) {
							String name = getName(dots);
							dots = 0;
							if (name != null) {
								ScriptValue temp = (ScriptValue) lookup
										.get(name);
								if (temp != null) {
									incdec(temp);
									pluses = 0;
								}
							}
						}
						break;

					case MINUS:
						minuses++;
						if (minuses == 1) {
							mathcode = 2;
						} else if (minuses == 2) {
							String name = getName(dots);
							dots = 0;
							if (name != null) {
								ScriptValue temp = (ScriptValue) lookup
										.get(name);
								if (temp != null) {
									incdec(temp);
									minuses = 0;
								}
							}
						}
						break;

					case MULTIPLY:
						stars = 1;
						mathcode = 2;
						break;

					case DIVIDE:
						slashes = 1;
						mathcode = 2;
						break;

					default:
						break;
					}
				}
				default:
					break;
				}
				mathcode--;
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder();
				element.dump(sb);
				runner.error("Error processing element: " + sb.toString());
			}
		}
		return bRet;
	}

	void incdec(ScriptValue value) throws Exception {
		try {
			String s = (String) value.getObject();
			if (s != null) {
				int v = Integer.parseInt(s);
				if (pluses > 1) {
					v++;
				} else if (minuses > 1) {
					v--;
				}
				value.setObject(Integer.toString(v));
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
	}

	void appendto(ScriptValue svTarget, ScriptValue svSource) throws Exception {
		try {
			Object target = svTarget.getObject();
			Object source = svSource.getObject();

			if (target != null) {
				if (source != null) {
					svTarget
							.setObject(ScriptProcessor.appendto(target, source));
				}
			} else {
				if (source != null) {
					svTarget.setObject(source);
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
	}

	Object addto(ScriptValue svTarget, ScriptValue svSource) throws Exception {
		Object result = null;
		try {
			Object target = svTarget.getObject();
			Object source = svSource.getObject();

			if (target != null) {
				if (source != null) {
					result = ScriptProcessor.addto(target, source);
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return result;
	}

	Object subtractFrom(ScriptValue svTarget, ScriptValue svSource)
			throws Exception {
		Object result = null;
		try {
			Object target = svTarget.getObject();
			Object source = svSource.getObject();

			if (target != null) {
				if (source != null) {
					result = ScriptProcessor.subtractFrom(target, source);
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return result;
	}

	Object multiply(ScriptValue svTarget, ScriptValue svSource)
			throws Exception {
		Object result = null;
		try {
			Object target = svTarget.getObject();
			Object source = svSource.getObject();

			if (target != null) {
				if (source != null) {
					result = ScriptProcessor.multiply(target, source);
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return result;
	}

	Object divide(ScriptValue svTarget, ScriptValue svSource) throws Exception {
		Object result = null;
		try {
			Object target = svTarget.getObject();
			Object source = svSource.getObject();

			if (target != null) {
				if (source != null) {
					result = ScriptProcessor.divide(target, source);
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return result;
	}

	String getName(int dots) throws Exception {
		String callerName = null;
		ScriptValue caller = null;
		Stack tempStack = null;

		if (buildcallstack)
			tempStack = callstack;
		else
			tempStack = stack;

		try {
			for (int i = 0; i <= dots && tempStack.empty() == false; i++) {
				caller = (ScriptValue) tempStack.pop();

				if (caller != null) {
					if (callerName != null) {
						callerName = caller.getName() + "." + callerName;
					} else
						callerName = caller.getName();
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return callerName;
	}

	String getMethodName(ScriptMethod caller, int dots) throws Exception {
		String callerName = caller.getName();
		try {
			for (int i = 0; i < dots && stack.empty() == false; i++) {
				ScriptValue value = (ScriptValue) stack.pop();

				if (value != null) {
					if (callerName != null) {
						callerName = value.getName() + "." + callerName;
					} else
						callerName = value.getName();
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return callerName;
	}

	boolean compareValues(int compare) throws Exception {
		boolean bRet = false;

		ScriptValue right = null;
		ScriptValue left = null;

		try {
			if (stack.empty() == false) {
				right = (ScriptValue) stack.pop();
				if (stack.empty() == false) {
					left = (ScriptValue) stack.pop();
					int i = left.compareTo(right);
					switch (compare) {
					case -6:
						if (i < 1)
							bRet = true;
						break;
					case -3:
						if (i != 0)
							bRet = true;
						break;
					case -2:
						if (i > -1)
							bRet = true;
						break;
					case 2:
						if (i == 0)
							bRet = true;
						break;
					case 3:
						if (i < 0)
							bRet = true;
						break;
					case 4:
						if (i < 1)
							bRet = true;
						break;
					case 6:
						if (i > 0)
							bRet = true;
						break;
					case 7:
						if (i > -1)
							bRet = true;
						break;
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			runner.debug(e.getMessage());
			throw e;
		}
		return bRet;
	}

	public boolean loadScript(String scriptFile) throws Exception {
		boolean breturn = true;
		try {
			String compiledFile = scriptFile.substring(0, scriptFile
					.lastIndexOf('.')+1) + "xml";
			File sf = new File(scriptFile);
			File cf = new File(compiledFile);

			if (!cf.exists() || sf.lastModified() > cf.lastModified()) {
				ScriptParser parser = new ScriptParser();
				BufferedReader in = new BufferedReader(new FileReader(
						scriptFile));

				StringBuffer script = new StringBuffer();
				String line = null;

				while ((line = in.readLine()) != null) {
					script.append(line);
				}
				/*
				blocks = parser.parse(script.toString());
				for(Entry<String, ScriptBlock> entry : blocks.entrySet())
				{
					entry.getValue().dump(builder);
					System.out.println(builder);
					
				}
				*/
				blocks = parser.parseScript(script.toString());
				/*
				builder = new StringBuilder();
				for(Entry<String, ScriptBlock> entry : blocks.entrySet())
				{
					entry.getValue().dump(builder);
					System.out.println(builder);
					
				}
				*/
				in.close();
				//TODO: Add compile and save
			} else {
				//TODO: Add load
			}

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			if(rootBlock!=null)
			{
				rootBlock.dump(sb);
				if(runner!=null)
				{
					runner.debug(sb.toString());
				}
			}
			if(runner!=null)
			{
				runner.debug(e.getMessage());
			}
			throw e;
		}
		return breturn;
	}

}