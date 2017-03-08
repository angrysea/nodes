package org.amg.node.scripting.scriptcompiler;

import java.io.FileOutputStream;

public class Operator implements Element
{
    public Operator(char value)
    {
        this.value=value;
    }

    public void compile(FileOutputStream o)
    {
        try
        {
            String line = new String("<Operator type=\"");
            if(value==GREATER)
                line+="&gt;";
            else if (value==LESS)
                line+="&lt;";
            else
                line += value;
            line += "\"/>\n";
            o.write(line.getBytes());
        }
        catch(Exception e)
        {
        }
    }

    public void dump()
    {
        System.out.println("\t\tOperator "+value);
    }

    private char value;

    public static int getType(char lit)
    {
	    return operators.indexOf(lit)+1;
    }

    public static boolean isOperator(char lit)
    {
	    return operators.indexOf(lit)>-1;
    }

    static String operators = "=!>|&<{}()[];:.+-";

    public static final char EQUAL = '=';
    public static final char INEQUALITY = '!';
    public static final char GREATER = '>';
    public static final char OR = '|';
    public static final char AND = '&';
    public static final char LESS = '<';
    public static final char OPENCURLY = '{';
    public static final char CLOSECURLY = '}';
    public static final char OPENPAREN = '(';
    public static final char CLOSEPAREN = ')';
    public static final char OPENBRACKET = '[';
    public static final char CLOSEBRACKET = ']';
    public static final char SEMICOLON = ';';
    public static final char COLON = ':';
    public static final char DOT = '.';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
}