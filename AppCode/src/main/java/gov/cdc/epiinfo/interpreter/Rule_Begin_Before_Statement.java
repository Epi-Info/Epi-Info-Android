package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_Begin_Before_Statement extends EnterRule 
{
    public EnterRule Statements = null;

    public Rule_Begin_Before_Statement(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        //<Begin_Before_statement> ::= Begin-Before <Statements> End | Begin-Before End |!Null
        if (pToken.size() > 2)
        {
            //NonterminalToken T = (NonterminalToken)pToken.Tokens[1];
            //this.Statements = new Rule_Statements(pContext, T);
            this.Statements = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(1).getData());
        }
    }

    
   /// <summary>
    /// performs execute command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (this.Statements != null)
        {
            try
            {
                result = this.Statements.Execute();
            }
            catch (Exception ex)
            {
            	ex.printStackTrace();
            	/*
                if (this.Context.EnterCheckCodeInterface.IsSuppressErrorsEnabled)
                {
                    Logger.Log(string.Format("{0} - EnterInterpreter Execute : source [{1}]\n message:\n{2}", DateTime.Now, ex.Source, ex.Message));
                }
                else
                {*/
                    //throw ex;
                //}
            }
        }
        return result;
    }


    /// <summary>
    /// To String method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public String toString()
    {
        return super.toString();
    }

    @Override
    public boolean IsNull(){ return this.Statements == null; } 
}
