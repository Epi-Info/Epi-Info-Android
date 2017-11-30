package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_Call extends EnterRule 
{
    String Identifier = null;

    public Rule_Call(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
    	//<Call_Statement> ::= CALL Identifier
        this.Identifier = this.ExtractIdentifier(pToken.get(1));
    }

    /// <summary>
    /// Call Statment rule
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        if (this.Context.Subroutine.containsKey(this.Identifier.toLowerCase()))
        {
            EnterRule Sub = this.Context.Subroutine.get(this.Identifier.toLowerCase());
            return Sub.Execute();
        }

        return null;
    }
}
