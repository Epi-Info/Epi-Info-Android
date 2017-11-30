package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.RecordEditor;
import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_TxtToDate extends EnterRule 
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_TxtToDate(Rule_Context pContext, Reduction pToken)
        
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
        try
        {
        	Date date = new Date(Date.parse(this.ParameterList.get(0).Execute().toString()));
            
        	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat((RecordEditor)this.Context.CheckCodeInterface);
    		return dateFormat.format(date.getTime());
        }
        catch(Exception ex)
        {
            return null;
        }
    }
}
