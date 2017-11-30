package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;



public class Rule_Subroutine_Statement extends EnterRule 
{
    private EnterRule Statements = null;
    private String Identifier = null;

    public Rule_Subroutine_Statement(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);

        //<Subroutine_Statement> ::= Sub Identifier <Statements> End | Sub Identifier End
        this.Identifier = this.ExtractIdentifier(pToken.get(1));
        if (pToken.size() > 3)
        {
            //NonterminalToken T = (NonterminalToken)pToken.Tokens[2];
            //this.Statements = new Rule_Statements(pContext, T);
            this.Statements = EnterRule.BuildStatements(pContext, pToken.get(2).asReduction());
        }
    }

    
   /// <summary>
    /// performs execute command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Identifier = this.Identifier.toLowerCase();

        if (this.Context.Subroutine.containsKey(this.Identifier))
        {
            this.Context.Subroutine.remove(this.Identifier);
        }
        this.Context.Subroutine.put(this.Identifier, this.Statements);

        return null;
    }

}
