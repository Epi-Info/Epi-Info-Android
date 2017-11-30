package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.parser.ProcessRule;

public class Rule_View_Checkcode_Statement extends EnterRule
{
    private EnterRule BeginBefore = null;
    private EnterRule BeginAfter = null;

    //private String TextField = null;

    public Rule_View_Checkcode_Statement(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);

        //this.TextField = pToken.this.ExtractTokensWithFormat(pToken..Tokens);
        // <View_Checkcode_Statement> ::= View <Begin_Before_statement> <Begin_After_statement> End
        for (int i = 1; i < pToken.size(); i++)
        {
        	Token T = pToken.get(i);
	        if(T.getName().equalsIgnoreCase("<Begin_Before_statement>"))
	        {
	        	this.BeginBefore = EnterRule.BuildStatements(pContext, pToken.get(i).asReduction());
	        }
	        else if(T.getName().equalsIgnoreCase("<Begin_After_statement>"))
			{
	            this.BeginAfter = EnterRule.BuildStatements(pContext, pToken.get(i).asReduction());
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
        this.Context.View_Checkcode = this;

        //if (this.BeginBefore.Statements != null)
        //{

            if (this.Context.BeforeCheckCode.containsKey("view"))
            {
                this.Context.BeforeCheckCode.remove("view");
            }
            this.Context.BeforeCheckCode.put("view", this.BeginBefore);
        //}

        //if (this.BeginAfter.Statements != null)
        //{
            if (this.Context.AfterCheckCode.containsKey("view"))
            {
                this.Context.AfterCheckCode.remove("view");
            }
            this.Context.AfterCheckCode.put("view", this.BeginAfter);
        //}
        return null;
    }

    /*
    @Override
    public  String toString()
    {
        return this.TextField;
    }*/

    @Override
    public boolean IsNull() { return BeginBefore == null && BeginAfter == null; } 
}
