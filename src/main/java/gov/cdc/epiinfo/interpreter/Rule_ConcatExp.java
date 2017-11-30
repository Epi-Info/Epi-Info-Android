package gov.cdc.epiinfo.interpreter;

import java.text.DecimalFormat;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_ConcatExp extends EnterRule 
{
    EnterRule AddExp = null;
    String op = null;
    EnterRule ConcatExp = null;

    public Rule_ConcatExp(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        /*::= <Add Exp> '&' <Concat Exp>
	   						| <Add Exp>*/

        this.AddExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        if (pToken.size() > 1)
        {
            this.op = this.ExtractIdentifier(pToken.get(1));

            this.ConcatExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(2).getData());
        }

    }
    /// <summary>
    /// performs concatenation of string via the '&' operator
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (op == null)
        {
            result = this.AddExp.Execute();
        }
        else
        {
            Object LHSO = this.AddExp.Execute(); 
            Object RHSO = this.ConcatExp.Execute();

            if (LHSO != null && RHSO != null)
            {
            	if (LHSO.getClass().equals(Double.class))
                {
                	LHSO = new DecimalFormat("0.######").format(LHSO);
                }
                if (RHSO.getClass().equals(Double.class))
                {
                	RHSO = new DecimalFormat("0.######").format(RHSO);
                }
                result = LHSO.toString() + RHSO.toString();
            }
            else if (LHSO != null)
            {
                if (LHSO instanceof String)
                {
                    result = LHSO;
                }
            }
            else if (RHSO instanceof String)
            {
                result = RHSO;
            }
        }

        return result;
    }

}
