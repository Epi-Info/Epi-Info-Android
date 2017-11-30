package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Cos extends EnterRule
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Cos(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }
    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the COS of a number.</returns>
    @Override
    public Object Execute()
    {
        double result = 0.0;
        try
        {
        	result = Double.parseDouble(this.ParameterList.get(0).Execute().toString());
            return Math.cos(result);
        }
        catch(Exception ex)
        {
            return null;
        }
        
    }
}
