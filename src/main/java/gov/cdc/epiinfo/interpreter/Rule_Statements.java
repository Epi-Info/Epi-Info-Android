package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_Statements extends EnterRule 
{
     //<Statements> ::= <Statements> <Statement> | <Statement>
    public EnterRule statement = null;
    public EnterRule statements = null;

    public Rule_Statements(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);
       //<Statements> ::= <Statements> <Statement> | <Statement>

        if (pToken.size() > 1)
        {
            //NonterminalToken T;
            //T = (NonterminalToken)pToken.Tokens[0];
            //this.statements = new Rule_Statements(pContext, T);
            this.statements = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());

            //T = ((NonterminalToken)pToken.Tokens[1]);
            //this.statement = new Rule_Statement(pContext, T);
            this.statement = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(1).getData());
        }
        else
        {
            //NonterminalToken T;
            //T = (NonterminalToken)pToken.Tokens[0];
            //this.statement = new Rule_Statement(pContext, T);
            this.statement = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(0).getData());
        }
    }

    /// <summary>
    /// connects the execution of Rule_Statments
    /// </summary>
    /// <returns>object</returns>
    @Override
   public Object Execute()
   {
     Object result = null;

     if (! this.statements.IsNull())
     {
       result = this.statements.Execute();
         
     }

     result = this.statement.Execute();

     return result;
    }
 }
