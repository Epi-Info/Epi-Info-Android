package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_UnHide extends EnterRule 
{
    boolean IsExceptList = false;
    String[] IdentifierList = null;

    public Rule_UnHide(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        if (pToken.size() > 2)
        {
            //<Unhide_Except_Statement> ::= UNHIDE '*' EXCEPT <IdentifierList>
            this.IsExceptList = true;
            this.IdentifierList = this.ExtractIdentifier(pToken.get(3)).toString().split(" ");
        }
        else
        {
            //<Unhide_Some_Statement> ::= UNHIDE <IdentifierList>
            this.IdentifierList = this.ExtractIdentifier(pToken.get(1)).toString().split(" ");
        }
    }
    /// <summary>
    /// performs execution of the UNHIDE command via the EnterCheckCodeInterface.UnHide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Context.CheckCodeInterface.UnHide(this.IdentifierList, this.IsExceptList);

        return null;

    }
}
