package org.amg.node.scripting.scriptcompiler;

import java.io.FileOutputStream;

public class Literal implements Element
{
    public void setName(String name)
    {
    }

    public Literal(String value)
    {
        this.value=value;
    }

    public void compile(FileOutputStream o)
    {
        try
        {
            String line = new String("<Literal value=");
            boolean noparens = value.startsWith("\"");
            if(noparens==false)
                line+="\"";
            line += value;
            if(noparens==false)
                line+="\"";
            line += "/>\n";
            o.write(line.getBytes());
        }
        catch(Exception e)
        {
        }
    }

    public void dump()
    {
        System.out.println("\t\tLiteral "+value);
    }

    private String value;

    public static boolean isLiteral(String word)
    {
        boolean results = false;
        if((word.charAt(0)=='"' && word.charAt(word.length()-1)=='"')||isNumeric(word))
        {
            results=true;
        }

	    return results;
    }

    public static boolean isNumeric(String word)
    {
        int len = word.length();
        char buff[] = word.toCharArray();
        for(int i=0;i<len;i++)
        {
			char ch = buff[i];
            //System.out.println(ch);
            if(ch<48||ch>57)
                return false;
        }
        return true;
    }

}