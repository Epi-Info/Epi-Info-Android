package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Execute extends EnterRule
{
    boolean IsExceptList = false;
    String ExecutionItem = null;

    public Rule_Execute(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);

    	this.ExecutionItem = this.ExtractIdentifier(pToken.get(2)).replace("\"", "").toString();
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
    	if (this.ExecutionItem.contains(".pdf"))
    	{
    		this.Context.CheckCodeInterface.DisplayPDF(this.ExecutionItem);
    	}
        this.Context.CheckCodeInterface.ExecuteUrl(this.ExecutionItem.replace("::", "://"));
        return null;
    }
}
