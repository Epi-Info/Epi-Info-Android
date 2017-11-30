package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;


public class Rule_Field_Checkcode_Statement extends EnterRule 
{
    private EnterRule BeginBefore = null;
    private EnterRule BeginAfter = null;
    private EnterRule BeginClick = null;

    private String Identifier = null;

    public Rule_Field_Checkcode_Statement(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        //<Field_Checkcode_Statement> ::=  Field Identifier <Begin_Before_statement> <Begin_After_statement> <Begin_Click_statement>  End

        //this.Identifier = this.GetCommandElement(pToken.Tokens, 1).Trim(new char[] { '[',']'});
    	this.Identifier = GetIdentifier(pToken.get(1).getData().toString());
        for (int i = 2; i < pToken.size(); i++)
        {
        	Token T = pToken.get(i);
        	Rule_Enum result = Rule_Enum.Convert(T.getName());
        	if(result != null)
        	{
	        	switch (result)
	            {
	        		case Begin_Before_statement:
		            	this.BeginBefore = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
		            	break;
	        		case Begin_After_statement:
		            	this.BeginAfter = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
		            	break;
	        		case Begin_Click_statement:
	        			this.BeginClick = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
	        			break;
	        		default:
	        			break;
	            }
        	}
        	else
        	{
        		System.out.print("nuff said");
        	
        	}
        }
    }


    /// <summary>
    /// performs execute command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Identifier = this.Identifier.toLowerCase();

        if (this.Context.Field_Checkcode.containsKey(this.Identifier))
        {
            this.Context.Field_Checkcode.remove(this.Identifier);
        }
        this.Context.Field_Checkcode.put(this.Identifier, this);

        if (this.Context.FieldBeforeCheckCode.containsKey(this.Identifier))
        {
            this.Context.FieldBeforeCheckCode.remove(this.Identifier);
        }
        this.Context.FieldBeforeCheckCode.put(this.Identifier, this.BeginBefore);

        if (this.Context.FieldAfterCheckCode.containsKey(this.Identifier))
        {
            this.Context.FieldAfterCheckCode.remove(this.Identifier);
        }
        this.Context.FieldAfterCheckCode.put(this.Identifier, this.BeginAfter);

        if (this.Context.FieldClickCheckCode.containsKey(this.Identifier))
        {
            this.Context.FieldClickCheckCode.remove(this.Identifier);
        }
        this.Context.FieldClickCheckCode.put(this.Identifier, this.BeginClick);
        return null;
    }


@Override
    public  boolean IsNull() 
    {
        return BeginBefore == null && BeginAfter == null && BeginClick == null; 
    } 
}
