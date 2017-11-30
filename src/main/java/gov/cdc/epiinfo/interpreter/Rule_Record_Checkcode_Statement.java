package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;

public class Rule_Record_Checkcode_Statement extends EnterRule 
{
    private EnterRule BeginBefore = null;
    private EnterRule BeginAfter = null;

    public Rule_Record_Checkcode_Statement(Rule_Context pContext, Reduction pToken)
        
    {
    	super(pContext);

        //<Record_Checkcode_Statement> ::= Record <Begin_Before_statement> <Begin_After_statement> End
        for (int i = 1; i < pToken.size(); i++)
        {
        	Token T = pToken.get(i);
			if(T.getName().equalsIgnoreCase("Begin_Before_statement"))
			{
			    this.BeginBefore = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
			}else if(T.getName().equalsIgnoreCase("Begin_After_statement"))
			{
				this.BeginAfter = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
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

        this.Context.Record_Checkcode = this;

        //if (this.BeginBefore.Statements != null)
        //{
            if (this.Context.BeforeCheckCode.containsKey("record"))
            {
                this.Context.BeforeCheckCode.remove("record");
            }
            this.Context.BeforeCheckCode.put("record", this.BeginBefore);
        //}


        //if (this.BeginAfter.Statements != null)
        //{
            if (this.Context.AfterCheckCode.containsKey("record"))
            {
                this.Context.AfterCheckCode.remove("record");
            }
            this.Context.AfterCheckCode.put("record", this.BeginAfter);
        //}
        return null;
    }

    /*
    public override string ToString()
    {
        return this.TextField;
    }*/

    @Override
    public boolean IsNull() { return BeginBefore == null && BeginAfter == null; } 

}
