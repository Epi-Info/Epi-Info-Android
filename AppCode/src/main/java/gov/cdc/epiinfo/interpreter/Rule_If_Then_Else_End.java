package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_If_Then_Else_End extends EnterRule 
{
    EnterRule IfClause;
    EnterRule ThenClause;
    EnterRule ElseClause;

    public Rule_If_Then_Else_End(Rule_Context pContext, Reduction pToken)
    {
    	 super(pContext);
        /*
          
        <If_Statement>                  ::=   IF <Expression> THEN  <Statements>  END-IF 
                                            | IF <Expression> THEN  <Statements>  END
        <If_Else_Statement>              ::=  IF <Expression> THEN  <Statements> <Else_If_Statement>  END-IF 
                                            | IF <Expression> THEN  <Statements>  <Else_If_Statement>  END
                                                IF <Expression> THEN <Statements> ELSE  <Statements>  END-IF 
                                            | IF <Expression> THEN <Statements> ELSE  <Statements>  END
         */

        IfClause = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(1).getData());
        ThenClause = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(3).getData());
        if (pToken.get(4).asString().equalsIgnoreCase("Else"))
        {
            ElseClause = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(5).getData());
        }
        /*
        else
        {
            ElseClause = EnterRule.BuildStatements(pContext, (Reduction)pToken.get(4).getData());
        }*/
    }

    /// <summary>
    /// performs execution of the If...Then...Else command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (IfClause.Execute().toString().toLowerCase() == "true")
        {
            result = ThenClause.Execute();
        }
        else
        {
            if (ElseClause != null)
            {
                result = ElseClause.Execute();
            }
        }
        
        return result;
    }


}

