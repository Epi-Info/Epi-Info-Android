package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_ExprList extends EnterRule 
{
    EnterRule Expression = null;
    EnterRule ExprList = null;

    public Rule_ExprList(Rule_Context pContext, Reduction pToken)
    {
    	 super(pContext);
        /*::= <Expression> ',' <Expr List> | <Expression> */

        this.Expression = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());

        if (pToken.size() > 1)
        {
            this.ExprList = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(2).getData());
        }

    }


    /// <summary>
    /// performs execution of a list of expressions
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        result = this.Expression.Execute();

        if (this.ExprList != null)
        {
            result = this.ExprList.Execute();
        }


        return result;
    }


}
