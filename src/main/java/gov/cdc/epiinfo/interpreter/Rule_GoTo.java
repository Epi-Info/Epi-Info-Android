package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_GoTo extends EnterRule
{
    boolean IsExceptList = false;
    String Identifier = null;

    public Rule_GoTo(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);

        this.Identifier = this.ExtractIdentifier(pToken.get(1)).toString();
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Context.CheckCodeInterface.GoTo(this.Identifier);
        return null;
    }
}
