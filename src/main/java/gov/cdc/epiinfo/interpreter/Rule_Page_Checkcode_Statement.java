package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;

public class Rule_Page_Checkcode_Statement extends EnterRule
{
    private EnterRule BeginBefore = null;
    private EnterRule BeginAfter = null;
    private String Identifier = null;

    public Rule_Page_Checkcode_Statement(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);


        //<Page_Checkcode_Statement> ::= Page Identifier <Begin_Before_statement> <Begin_After_statement> End
        this.Identifier = GetIdentifier(pToken.get(1).getData().toString());
        for (int i = 2; i < pToken.size(); i++)
        {
                Token T = pToken.get(i);
                if(T.getName().equalsIgnoreCase("Begin_Before_statement"))
                {
                    this.BeginBefore = EnterRule.BuildStatements(pContext, (Reduction)T.getData());
                }
                else if(T.getName().equalsIgnoreCase("Begin_After_statement"))
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
    public  Object Execute()
    {
        this.Identifier = this.Identifier.toLowerCase();

        if (this.Context.Page_Checkcode.containsKey(this.Identifier))
        {
            this.Context.Page_Checkcode.remove(this.Identifier);
        }
        this.Context.Page_Checkcode.put(this.Identifier, this);

        if (this.Context.PageBeforeCheckCode.containsKey(this.Identifier))
        {
            this.Context.PageBeforeCheckCode.remove(this.Identifier);
        }
        
        this.Context.PageBeforeCheckCode.put(this.Identifier, this.BeginBefore);
        if (this.Context.PageAfterCheckCode.containsKey(this.Identifier))
        {
            this.Context.PageAfterCheckCode.remove(this.Identifier);
        }
        this.Context.PageAfterCheckCode.put(this.Identifier, this.BeginAfter);
        
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
