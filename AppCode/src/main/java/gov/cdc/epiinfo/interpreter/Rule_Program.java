package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_Program extends EnterRule 
{
    EnterRule CheckCodeBlocks = null;

    public Rule_Program(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        //<Program> ::= <CheckCodeBlocks> | !Eof
        if (pToken.size() > 0)
        {
            CheckCodeBlocks = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());
        }
    }

            /// <summary>
    /// executes program start
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        if (CheckCodeBlocks != null)
        {
            CheckCodeBlocks.Execute();
        }

        return null;
    }
}
