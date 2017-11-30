package gov.cdc.epiinfo.interpreter;

import goldengine.java.GOLDParser;
import goldengine.java.GPMessageConstants;
import goldengine.java.ParserException;
import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;

import java.io.BufferedInputStream;
import java.io.InputStream;

import android.content.res.AssetManager;




public class CheckCodeProcessor implements GPMessageConstants {

	private GOLDParser parser;
	private FormLayoutManager layoutManager;
	
	public CheckCodeProcessor(AssetManager assetManager)
	{
		try
		{
			parser = new GOLDParser();
			
			

			InputStream raw = assetManager.open("EpiInfo.Enter.Grammar.cgt");
			
			if(raw != null)
			{
			
				BufferedInputStream input = new BufferedInputStream(raw);
				parser.loadCompiledGrammar(input);
			}
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			
			//int x = 5;
			//x++;
		}
	}
	
	public void SetLayoutManager(FormLayoutManager layoutManager)
	{
		this.layoutManager = layoutManager;
	}
	
	public void Execute(String checkCode, String event)
	{
		try
		{
			parser.loadText(checkCode);
			
			boolean done = false;
	        int response = -1;

	        while(!done)
	        {
	            try
	            {
	            	response = parser.parse();
	            }
	            catch(ParserException parse)
	            {
	                System.out.println("**PARSER ERROR**\n" + parse.toString());
	            }

	            switch(response)
	            {
	                case gpMsgTokenRead:
	                    break;

	                case gpMsgReduction:
	                	
	                	ICommand command = new Cmd_Empty();	                	
	                    Reduction myRed = parser.currentReduction();
	                    if (myRed.getParentRule().getText().contains(event))
	                    {
	                    	command = CommandFactory.GetCommand((Reduction)myRed.getToken(1).getData(), layoutManager);
	                    }
	                    command.Execute();
	                    
	                    break;

	                case gpMsgAccept:
	                	System.out.println("All commands parsed successfully.");
	                    done = true;
	                    break;

	                case gpMsgLexicalError:
	                    System.out.println("gpMsgLexicalError");
	                    parser.popInputToken();
	                    break;

	                case gpMsgNotLoadedError:
	                    System.out.println("gpMsgNotLoadedError");
	                    done = true;
	                    break;

	                case gpMsgSyntaxError:
	                    done = true;
	                    Token theTok = parser.currentToken();
	                    System.out.println("Token not expected: " + theTok.getData());
	                    System.out.println("gpMsgSyntaxError");
	                    break;

	                case gpMsgCommentError:
	                    System.out.println("gpMsgCommentError");
	                    done = true;
						break;

	                case gpMsgInternalError:
	                    System.out.println("gpMsgInternalError");
	                    done = true;
	                    break;
	            }
	        }
	        try
	        {
	        	parser.closeFile();
	        }
	        catch(ParserException parse)
	        {
	            System.out.println("**PARSER ERROR**\n" + parse.toString());
	        }
		}
		catch (Exception ex)
		{
			int x = 5;
			x++;
		}
	}
	
	
}
