package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_AndExp extends EnterRule 
{
	 EnterRule NotExp = null;
     EnterRule AndExp = null;

     public Rule_AndExp(Rule_Context pContext, Reduction pToken) 
     {
    	 super(pContext);
         // <Not Exp> AND <And Exp> 
         // <Not Exp> 

         this.NotExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
         if (pToken.size() > 1)
         {
             this.AndExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(2).getData());
         }
     }

     /// <summary>
     /// performs an "and" operation on boolean expresssions
     /// </summary>
     /// <returns>object</returns>
     @Override
     public Object Execute()
     {
         Object result = null;

         if (this.AndExp == null)
         {
             result = this.NotExp.Execute();
         }
         else
         {
             // dpb: this needs to be fixed to work with more then just strings
             
             String LHS = this.NotExp.Execute().toString().toLowerCase();
             String RHS = this.AndExp.Execute().toString().toLowerCase();

             result = "true" == LHS && LHS == RHS;
         }

         return result;
     }



}
