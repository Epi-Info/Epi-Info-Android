package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_Expression extends EnterRule 
{

    EnterRule And_Exp = null;
    String op = null;
    EnterRule Expression = null;
    public String CommandText = null;
    
    public Rule_Expression(Rule_Context pContext, Reduction pTokens)
    {
    	 super(pContext);
        /*::= <And Exp> OR <Expression>
	  						| <And Exp> XOR <Expression>
           							| <And Exp> */

        //this.CommandText = this.ExtractTokens(pTokens.Tokens);

        And_Exp = EnterRule.BuildStatements(pContext, (Reduction)pTokens.get(0).getData());
        if(pTokens.size() > 1)
        {
                op = this.ExtractIdentifier(pTokens.get(1));
                Expression = EnterRule.BuildStatements(pContext, (Reduction)pTokens.get(2).getData());
        }
    }

    /// <summary>
    /// performs execution of an 'OR' or 'XOR' expression
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;
        boolean AndResult = false;

        if(op == null)
        {
            result = this.And_Exp.Execute();
        }
        else
        {
            if (this.And_Exp.Execute().toString().equalsIgnoreCase("true"))
            {
                result = AndResult = true;
            }

            if (op != null)
            {

                boolean ExpressionResult = false;


                if (this.Expression.Execute().toString().equalsIgnoreCase("true"))
                {
                    ExpressionResult = true;
                }

                
                if(op.equalsIgnoreCase("OR"))
                {
                        AndResult = AndResult || ExpressionResult;
                }
                else if(op.equalsIgnoreCase("XOR"))
                        AndResult = AndResult != ExpressionResult;
            	}
                
            	result = AndResult;
                //result = BoolVal(AndResult);
            }
        return result;
    }
	
}
