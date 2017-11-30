package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_DefineVariables_Statement extends EnterRule 
{
	private EnterRule define_Statements_Group = null;
	private EnterRule define_Statement = null;

    public Rule_DefineVariables_Statement(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);

        //<DefineVariables_Statement> ::= DefineVariables <Define_Statement_Group> End-DefineVariables
        if (pToken.size() > 1)
        {
            //define_Statements_Group = new Rule_Define_Statement_Group(pContext, (NonterminalToken)pToken.Tokens[1]);
        	String test = pToken.get(0).toString();
        	if (pToken.get(0).toString().equals("<Define_Statement_Type>"))
        	{
        		define_Statement = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());
        	}
            define_Statements_Group = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
        }
    }

    
   /// <summary>
    /// performs execute command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {

        //if (define_Statements_Group != null && define_Statements_Group.Define_Statement_Type != null && this.Context.EnterCheckCodeInterface.IsExecutionEnabled)
        //if (define_Statements_Group != null && this.Context.EnterCheckCodeInterface.IsExecutionEnabled)
    	if (define_Statement != null)
    	{
    		Object retVal = define_Statement.Execute();
    	}
    	if (define_Statements_Group != null)
        {
            this.Context.DefineVariablesCheckcode = this;
            return define_Statements_Group.Execute();
        }
        else
        {
            this.Context.DefineVariablesCheckcode = null;
            return null;
        }
    }


    @Override
    public boolean IsNull() { return this.define_Statements_Group == null; } 
}
