package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_SendSMS extends EnterRule 
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_SendSMS(Rule_Context pContext, Reduction pToken)
        
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the absolute value of two numbers.</returns>
    @Override
    public  Object Execute()
    {

            return null;
        
    }
}
