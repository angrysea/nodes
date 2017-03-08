package org.amg.node.scripting.scriptcompiler;

import java.io.FileOutputStream;

interface Element
{
    void dump();
    void compile(FileOutputStream o);
}