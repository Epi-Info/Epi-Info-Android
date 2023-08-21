package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_CheckCodeBlocks extends EnterRule 
{
    EnterRule CheckCodeBlock = null;
    EnterRule CheckCodeBlocks = null;

    public Rule_CheckCodeBlocks(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        //<CheckCodeBlocks> ::= <CheckCodeBlock> <CheckCodeBlocks> | <CheckCodeBlock>
        if (pToken.size() > 1)
        {
            CheckCodeBlock = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
            CheckCodeBlocks = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
        }
        else
        {
            CheckCodeBlock = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        }
    }

            /// <summary>
    ///executes CheckCodeBlock statement
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        CheckCodeBlock.Execute();

        if (CheckCodeBlocks != null)
        {
            CheckCodeBlocks.Execute();
        }

        return null;
    }
}
