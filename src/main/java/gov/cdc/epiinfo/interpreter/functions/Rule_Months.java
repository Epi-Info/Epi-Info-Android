package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Months  extends EnterRule 
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Months(Rule_Context pContext, Reduction pToken)
        
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the absolute value of two numbers.</returns>
    @Override
    public Object Execute()
    {
        double result = 0.0;
        try
        {

        	String param1 = this.ParameterList.get(0).Execute().toString();
        	String param2 = this.ParameterList.get(1).Execute().toString();
        	Date d1;
        	Date d2;
        	
        	if (param1.toLowerCase().equals("systemdate") || param1.toLowerCase().equals("systemtime"))
        	{
        		d1 = Calendar.getInstance().getTime();
        	}
        	else
        	{
        		d1 = (Date)this.ParameterList.get(0).Execute();
        	}
        	
        	if (param2.toLowerCase().equals("systemdate") || param2.toLowerCase().equals("systemtime"))
        	{
        		d2 = Calendar.getInstance().getTime();
        	}
        	else
        	{
        		d2 = (Date)this.ParameterList.get(1).Execute();
        	}
        	
        	return (int)Math.floor((d2.getTime() - d1.getTime()) / 2.63e+9);
        }
        catch(Exception ex)
        {
            return null;
        }
    }
}
