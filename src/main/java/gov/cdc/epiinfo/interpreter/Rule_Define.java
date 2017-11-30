package gov.cdc.epiinfo.interpreter;


import gov.cdc.epiinfo.interpreter.CSymbol.VariableScope;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Define extends EnterRule 
{
	  String Identifier = null;
      //Strings named to match grammar
      String Variable_Scope = null;
      String VariableTypeIndicator = null;
      String Define_Prompt = null;
      EnterRule Expression = null;


      private VariableScope GetVariableScopeIdByName(String name)
      {
    	  if (name.toUpperCase().contains("PERMANENT"))
    	  {
    		  return VariableScope.Permanent;
    	  }
    	  else
    	  {
    		  return VariableScope.Standard;
    	  }
      }

      public Rule_Define(Rule_Context pContext, Reduction pToken)
      {
    	  super(pContext);
          //DEFINE Identifier <Variable_Scope> <VariableTypeIndicator> <Define_Prompt>
          //DEFINE Identifier '=' <Expression>

          Identifier = pToken.get(1).getData().toString();
          //((Reduction)pToken.get(2).getData()).getParentRule().getText()	
          

	

          if (pToken.get(2).getData().toString() == "=")
          {
              this.Expression = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(3).getData());
              // set some defaults
              Variable_Scope = "STANDARD";
              VariableTypeIndicator  =  "";
              Define_Prompt = "";
          }
          else
          {
              Variable_Scope = pToken.get(2).asString();//STANDARD | GLOBAL | PERMANENT |!NULL

              //VariableTypeIndicator = pToken.get(3).getData().toString();
              VariableTypeIndicator = pToken.get(3).asString();
              //Define_Prompt = pToken.get(4).getData().toString();
              Define_Prompt = pToken.get(4).asString();
          }

      }


       /// <summary>
      /// peforms the Define rule uses the MemoryRegion and this.Context.DataSet to hold variable definitions
      /// </summary>
      /// <returns>object</returns>
      @Override
      public Object Execute()
      {
    	 
          CSymbol result = new CSymbol();
          try
          {
        	  
              result.Name = this.Identifier;
              String dataTypeName = VariableTypeIndicator;
              String variableScope = Variable_Scope.trim().toUpperCase();
              VariableScope vt = VariableScope.Standard;

              if (!isNullOrEmpty(variableScope))
              {
                  result.VariableScope = this.GetVariableScopeIdByName(variableScope);
              }
              else
              {
            	  result.VariableScope = vt;
              }
              result.Type = CSymbol.DataType.convert(dataTypeName);

              this.Context.GetCurrentScope().define(result, "");
			  
              return result;
          }
          catch (Exception ex)
          {
        	  int x=5;
        	  x++;
              //Epi.Diagnostics.Debugger.Break();
              //Epi.Diagnostics.Debugger.LogException(ex);
              //throw ex;
          } /**/
    	  return null;
      }
}
