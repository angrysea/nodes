package org.adaptinet.node.scripting.scriptcompiler;

import java.io.FileOutputStream;

public class Variable implements Element
{
    public Variable(String name)
    {
        this.name=name;
    }

    final public void setName(String name)
    {
        this.name=name;
    }

    public void compile(FileOutputStream o)
    {
        try
        {
            String line = new String("<Variable name=\"");
            line += name;
            line += "\"/>\n";
            o.write(line.getBytes());
        }
        catch(Exception e)
        {
        }
    }

    public void dump()
    {
        System.out.println("\t\tvariable "+name);
    }

    String name=null;
}