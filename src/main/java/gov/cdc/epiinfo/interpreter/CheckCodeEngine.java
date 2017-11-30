package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.FormLayoutManager;

import java.io.InputStream;

import android.content.res.AssetManager;

import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.parser.GOLDParser;

public class CheckCodeEngine 
{
	private GOLDParser parser;
	
	private EnterRule Program;
	private Rule_Context Context;
	
	public CheckCodeEngine(AssetManager assetManager)
	{
		try
		{
			parser = new GOLDParser();

			InputStream raw = assetManager.open("EpiInfo.Enter.Grammar.egt");
			parser = new GOLDParser
			(
					raw, // compiled grammar table
		            "com.epiinfo.interpreter",  // rule handler package
		            true
			);  
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			
			//int x = 5;
			//x++;
		}
	}
	
	public void Execute(String pLevel, String pEvent, String pIdentifier)
	{
		EnterRule command = null;
        String QueryString = String.format("level={0}&event={1}&identifier={2}", pLevel.toLowerCase(), pEvent.toLowerCase(), pIdentifier.toLowerCase());

        command = this.Context.GetCommand(QueryString);
        if (command != null)
        {
            command.Execute();
        }
	}
	
	public Rule_Context PreCompile(String pCheckCode)
	{
        // Use the compiled grammar file inside the jar
    	/*
        GOLDParser parser = new GOLDParser(
            getClass().getResourceAsStream("Simple2.cgt"), // compiled grammar table
            "com.creativewidgetworks.goldparser.simple2",  // rule handler package
            true);                                         // trim reductions
        */
    	//GOLDParser parser = null;
    	String tree = null;
    	
    	Program = null;
    	

        
        // Controls whether or not a parse tree is returned or the program executed.
        parser.setGenerateTree(false);
        
        
        try
        {
        	this.Context = new Rule_Context();
        	
            // Parse the source statements to see if it is syntactically correct
        	parser.setTrimReductions(true);
            boolean parsedWithoutError = parser.parseSourceStatements(pCheckCode);

            // Holds the parse tree if setGenerateTree(true) was called
            tree = parser.getParseTree();
            
            // Either execute the code or print any error message
            if (parsedWithoutError) 
            {
                //parser.getCurrentReduction().execute();
            	
                //cRule p = cRule.BuildStatments(new cContext(), new Token(parser.getCurrentReduction().getParent().getHead(), parser.getCurrentReduction()));
            	
            	Program = EnterRule.BuildStatements(this.Context, parser.getCurrentReduction());
            	Program.Execute();
                
                //cVisitorExecute execute = new cVisitorExecute();
                //execute.visit(p);
            } 
            else
            {
                System.out.println(parser.getErrorMessage());
            }
        }
        catch (ParserException e) 
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return this.Context;
	}
	
}
