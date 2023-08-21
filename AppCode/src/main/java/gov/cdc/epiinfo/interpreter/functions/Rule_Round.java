package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;



public class Rule_Round extends EnterRule
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Round(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the rounded value of a number.</returns>
    @Override
    public  Object Execute()
    {
    	Object result = null;
    	try
    	{
	    	Object p1 = this.ParameterList.get(0).Execute().toString(); // the number to round
	    	Object p2 = 0; 
	
	        int param2 = 0;
	
	        if (this.ParameterList.size() == 2) // if provided, use this value to specify the number of decimal places to round to; otherwise round to a whole number
	        {
	            p2 = this.ParameterList.get(1).Execute().toString();
	            param2 = Integer.parseInt(p2.toString());
	        }
	
	        double param1;
	
	        param1 = Double.parseDouble(p1.toString());
	        DecimalFormat df = null;
	        
	        if(param2 == 0)
	        {
	        	df = new DecimalFormat("#");
	        }
	        else
	        {
	        	String precision = "";
	        	for(int i = 0; i < param2; i++) precision+="0";
	        	df = new DecimalFormat("#." + precision);
	        }
	        
	        result = Double.parseDouble(df.format(param1));
    	}
    	catch(Exception ex)
    	{
    		result = null;
    	}

        return result;
    }
}
