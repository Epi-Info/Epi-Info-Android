package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Substring  extends EnterRule
{
	Object _result = null;
	Object _fullString = null;
	Object _startIndex = 0;
	Object _length = 0;

    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();


    public Rule_Substring(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        //SUBSTRING(fullString,startingIndex,length)
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
        
    }
    /// <summary>
    /// returns a substring index is 1 based ie 1 = first character
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        _result = null;
        _fullString = null;
        _startIndex = 0;
        _length = 0;

        _fullString = this.ParameterList.get(0).Execute();

        if (!EnterRule.isEmpty(_fullString))
        {
            String fullString = _fullString.toString();

            _startIndex = this.ParameterList.get(1).Execute();
            int start = Integer.parseInt(_startIndex.toString());


            if (this.ParameterList.size() > 2)
            {
                _length = this.ParameterList.get(2).Execute();
            }
            else
            {
                _length = fullString.length();
            }

            int length = Integer.parseInt(_length.toString());

            if (start + length > fullString.length())
            {
                length = fullString.length() - start + 1;
            }
            
            if (start <= fullString.length())
            {
                _result = fullString.substring(start - 1, length);
            }
            else
            {
                _result = "";
            }
        }

        return _result;
    }
}
