package gov.cdc.epiinfo.interpreter.functions;

import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Validate extends EnterRule {

	private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Validate(Rule_Context pContext, Reduction pToken)        
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns value indicating if the input is valid.</returns>
    @Override
    public Object Execute()
    {
        try
        {
        	String p1 = this.ParameterList.get(0).Execute().toString();
        	String p2 = this.ParameterList.get(1).Execute().toString();

        	if (p2.equals("1"))
        	{
        		return func1(p1);
        	}
        	else
        	{
        		return func2(p1);
        	}
        }
        catch(Exception ex)
        {
            return false;
        }
    }
	
	private boolean func1(String input)
	{
		if (input.length() < 5)
		{
			return false;
		}
		char checkletter = input.charAt(4);
		int checkletterc = (int)checkletter;

		char firstletter= input.charAt(0);
		int firstletterc = (int)firstletter;

		char secondletter= input.charAt(1);
		int secondletterc = (int)secondletter;

		char thirdletter= input.charAt(2);
		int thirdletterc = (int)thirdletter;

		char fourthletter= input.charAt(3);
		int fourthletterc = (int)fourthletter;

		double calccheck=(firstletterc - 65) + (secondletterc - 65) + (thirdletterc - 65) + (fourthletterc - 65);

		double a5calc2= Math.floor(calccheck/26)+(calccheck-(26*Math.floor(calccheck/26)));

		double a5calc3;

		if (a5calc2>25) {
			a5calc3= Math.round(a5calc2/26)+(a5calc2-(26*Math.floor(a5calc2/26)));
		}
		else
		{
			a5calc3=a5calc2;
		}

		double a5calc4=65+a5calc3;

        return !(checkletterc != a5calc4);

    }
	
	
	private boolean func2(String input)
	{
		if (input.length() < 8)
		{
			return false;
		}

		int checkdigits= Integer.parseInt(input.substring(6));
		int firstnum = Integer.parseInt(input.substring(0,1));
		int secondnum = Integer.parseInt(input.substring(1,2));
		int thirdnum = Integer.parseInt(input.substring(2,3));
		int fourthnum = Integer.parseInt(input.substring(3,4));
		int fifthnum = Integer.parseInt(input.substring(4,5));
		int sixthnum = Integer.parseInt(input.substring(5,6));

		int checkdigits2 = (firstnum*19)+(secondnum*17)+(thirdnum*13)+(fourthnum*11)+(fifthnum*7)+(sixthnum*5);
		int ckdigitfinal;
		if (checkdigits2<100)
		{
			ckdigitfinal=checkdigits2;
		}
		else
		{
			ckdigitfinal= Integer.parseInt((checkdigits2 + "").substring(1));
		}

        return checkdigits == ckdigitfinal;
    }

	
}
