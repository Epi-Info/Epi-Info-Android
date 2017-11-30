package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Dialog extends EnterRule
{
    boolean IsExceptList = false;
    String DialogText = null;
    String TitleText = null;

    public Rule_Dialog(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);

    	this.DialogText = this.ExtractIdentifier(pToken.get(1)).replace("\"", "").toString();
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Context.CheckCodeInterface.Alert(this.DialogText);
        return null;
    }
}
