package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Exp_Func extends EnterRule 
{
	private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Exp_Func(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    @Override
    public Object Execute()
    {
    	double result = 0.0;
        try
        {
        	double p1 = Double.parseDouble(this.ParameterList.get(0).Execute().toString());
            result = Math.pow(Math.E, p1);
        }
        catch(Exception ex)
        {
            return null;
        }

        return result;
    }
}
