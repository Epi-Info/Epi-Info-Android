package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_PowExp extends EnterRule 
{
    EnterRule NegateExp1 = null;
    EnterRule NegateExp2 = null;

    public Rule_PowExp(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        /* 	::= <Negate Exp> '^' <Negate Exp>  | <Negate Exp> */

        this.NegateExp1 = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        if (pToken.size() > 1)
        {
            this.NegateExp2 = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(2).getData());
        }
        
    }


    /// <summary>
    /// raises a number to a power and returns the resulting number
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (NegateExp2 == null)
        {
            result = this.NegateExp1.Execute();
        }
        else
        {

            String LHS = this.NegateExp1.Execute().toString();
            String RHS = this.NegateExp2.Execute().toString();

            result = Math.pow(Double.parseDouble(LHS),Double.parseDouble(RHS));
        }

        return result;
    }
}
