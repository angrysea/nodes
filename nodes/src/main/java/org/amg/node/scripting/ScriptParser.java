package org.amg.node.scripting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

public class ScriptParser {

	private StringTokenizer tokenizer = null;
	Iterator<String> iter = null;
	
	public HashMap<String, ScriptBlock> parse(String script) {
		HashMap<String, ScriptBlock> blocks = new HashMap<String, ScriptBlock>();
		ScriptBlock block = new ScriptBlock();
		tokenizer = new StringTokenizer(script, "{");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			block.insertStatements(token);
			createBlock(block);
			blocks.put(token.substring(0, token.indexOf('(')), block);
		}
		return blocks;
	}

    private void createBlock(ScriptBlock parent)
    {
        String token = null;
        int start = 0;
        while(tokenizer.hasMoreTokens())
        {
            ScriptBlock child = new ScriptBlock();
            token = tokenizer.nextToken();
            start = token.indexOf('}');
            if(start<0)
            {
                child.insertStatements(token);
                createBlock(child);
                parent.insertChild(child);
            }
            else
            {
                child.insertStatements(token.substring(0,start));
                parent.insertChild(child);
                parent.insertStatements(token.substring(start+1));
                createBlock(parent);
            }
        }
    }

	public HashMap<String, ScriptBlock> parseScript(String script) {
		HashMap<String, ScriptBlock> blocks = new HashMap<String, ScriptBlock>();
		
		Stack<String> statements = new Stack<String>();
		StringTokenizer tokenizer = new StringTokenizer(script, "{");
		statements.push(tokenizer.nextToken());
		statements.push("{");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int start = token.indexOf('}');
			if (start > 0) {
				StringTokenizer subtokenizer = new StringTokenizer(token, "}");
				String subtoken = subtokenizer.nextToken();
				statements.push(subtoken);
				while (subtokenizer.hasMoreTokens()) {
					subtoken = subtokenizer.nextToken();
					statements.push("}");
					statements.push(subtoken);
				}
			} else {
				statements.push(token);
			}
			if (tokenizer.hasMoreTokens())
				statements.push("{");
			else
				statements.push("}");
		}

		/*
		iter = statements.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}		
		*/
		
		iter = statements.iterator();

		while (iter.hasNext()) {
			ScriptBlock block = new ScriptBlock();
			String token = iter.next();
			block.insertStatements(token);
			token = token.substring(0, token.indexOf('('));
			while (iter.hasNext()) {
				String code = iter.next();
				if (code.equals("{")) {
					break;
				}
			}
			createScriptBlock(block);
			blocks.put(token, block);
		}
		return blocks;
	}

	private void createScriptBlock(ScriptBlock parent) {
		ScriptBlock child = new ScriptBlock();
        parent.insertChild(child);
		while (iter.hasNext()) {
			String code = iter.next();
			if (code.equals("{")) {
				createScriptBlock(child);
			} else if (code.equals("}")) {
				break;
			} else {
				child.insertStatements(code);
			}
		}
	}
}