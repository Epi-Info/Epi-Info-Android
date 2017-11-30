package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_NotExp extends EnterRule 
{
    EnterRule CompareExp = null;
    boolean isNotStatement = false;

    public Rule_NotExp(Rule_Context pContext, Reduction pToken)
    {
    	 super(pContext);
        //<Not Exp> ::= NOT <Compare Exp> | <Compare Exp>
        if (pToken.size() == 1)
        {
            CompareExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        }
        else
        {
            CompareExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
            this.isNotStatement = true;
        }

    }

    /// <summary>
    /// performs a NOT or pass-thru operation
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (this.isNotStatement)
        {
            //result = this.CompareExp.Execute();
            
            if (this.CompareExp.Execute().toString().equalsIgnoreCase("true"))
            {
                result = "false";
            }
            else
            {
                result = "true";
            }
        }
        else
        {
            result = this.CompareExp.Execute();
        }

        /*
        if (this.CompareExp.Execute().ToString() == "true")
        {
            result = "false";
        }
        else
        {
            result = "true";
        }*/

        return result;

    }
}
