package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;



public class Rule_Statement extends EnterRule 
{
	private EnterRule reduction = null;

    public Rule_Statement(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
    	this.reduction = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());
    }
    
	@Override
	public Object Execute() 
	{
		Object result = null;
        if (this.reduction != null)
        {
            result = this.reduction.Execute();
        }
        return result;
	}

}
