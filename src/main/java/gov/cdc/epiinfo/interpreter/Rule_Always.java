package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;



public class Rule_Always extends EnterRule 
{
    EnterRule statements = null;

    public Rule_Always(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        // ALWAYS <Statements> END
        this.statements = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
    }

    /// <summary>
    /// executes the enclosed expressions
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object results = null;

        if (this.statements != null)
        {
            results = statements.Execute();
        }

        return results;
    }
}
