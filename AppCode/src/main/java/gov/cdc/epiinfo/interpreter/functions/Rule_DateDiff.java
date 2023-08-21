package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_DateDiff extends EnterRule
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    //private FunctionUtils.DateInterval currentInterval;

    /// <summary>
    /// Reduction to calculate the difference between two dates.
    /// </summary>
    /// <param name="pToken">The token to use to build the reduction.</param>
    /// <param name="interval">The date interval to use for calculating the difference (seconds, hours, days, months, years)</param>
    public Rule_DateDiff(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        //currentInterval = interval;
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
        /*
        NonterminalToken T = (NonterminalToken)pToken.Tokens[0];
        string type = pToken.Rule.Lhs.ToString();
        switch (type)
        {
            case "<FunctionParameterList>":
                this.ParamList = new Rule_FunctionParameterList(pContext, pToken);
                break;
            case "<FunctionCall>":
                this.functionCall = new Rule_FunctionCall(pContext, T);
                break;
            default:
                break;
        }*/
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the difference between two dates.</returns>
    @Override
    public Object Execute()
    {
        List<String> test = new ArrayList<String>();
        Object result = null;

        if (this.ParameterList.size() == 1)
        {
            result = this.ParameterList.get(0).Execute();
        }
        else
        {

            Date date1, date2;
            
            date1 = (Date) this.ParameterList.get(0).Execute();
            date2 = (Date) this.ParameterList.get(1).Execute();
            if(date1 != null && date2 != null)
            {
            	result = date1.compareTo(date2);
            }
            
        }

        // To prevent a null value in the param list from returning a zero
        //if (result == null)
        //{
        //    result = 0;
        //}

        return result;
    }
}
