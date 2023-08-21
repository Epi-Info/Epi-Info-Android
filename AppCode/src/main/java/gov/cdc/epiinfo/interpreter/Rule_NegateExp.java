package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_NegateExp extends EnterRule 
{
    String op = null;
    EnterRule Value = null;

    public Rule_NegateExp(Rule_Context pContext, Reduction pTokens)
    {
    	super(pContext);
        //<Negate Exp> ::= '-' <Value>
        // or
        //<Negate Exp> ::= <Value>

        if(pTokens.size() > 1)
        {
            this.op = this.ExtractIdentifier(pTokens.get(0));
            this.Value = EnterRule.BuildStatements(pContext, (Reduction) pTokens.get(1).getData());
        }
        else
        {
            this.Value = EnterRule.BuildStatements(pContext, (Reduction) pTokens.get(0).getData());
        }

    }
    /// <summary>
    /// performs the negation or pass-thru operation
    /// </summary>
    /// <returns>object</returns>
    public Object Execute()
    {
        Object result = null;
        if (this.op != null)
        {
            if (this.op == "-")
            {
                result = -1.0 * Double.parseDouble(Value.Execute().toString());
            }
            else
            {
                result = Double.parseDouble(Value.Execute().toString());
            }
        }
        else
        {
            result = Value.Execute();
        }

        return result;
    }
}
