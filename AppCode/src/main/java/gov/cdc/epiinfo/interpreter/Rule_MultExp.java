package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_MultExp extends EnterRule 
{
    EnterRule PowExp = null;
    String op = null;
    EnterRule MultExp = null;


    public Rule_MultExp(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        /*::= <Pow Exp> '*' <Mult Exp>
							| <Pow Exp> '/' <Mult Exp>
							| <Pow Exp> MOD <Mult Exp>
							| <Pow Exp> '%' <Mult Exp>
        							| <Pow Exp>*/

    	
        this.PowExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        if(pToken.size() > 1)
        {
            this.op = this.ExtractIdentifier(pToken.get(1));

            this.MultExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(2).getData());
        }
    }

    /// <summary>
    /// performs execution of the (/, MOD and %) operators
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (op == null)
        {
            result = this.PowExp.Execute();
        }
        else
        {
            Object LHSO = this.PowExp.Execute();
            Object RHSO = this.MultExp.Execute();

            if (LHSO instanceof Number)
            {
                LHSO = LHSO;
            }

            if (RHSO instanceof Number)
            {
                RHSO = RHSO;
            }


            switch (Operator_Enum.Convert(op))
            {
                case mul:
                    /*if ((LHSO instanceof TimeSpan) && (RHSO instanceof TimeSpan))
                    {
                        result = ((TimeSpan)LHSO).TotalDays * ((TimeSpan)RHSO).TotalDays;
                    }
                    else if ((LHSO instanceof TimeSpan) && (RHSO instanceof Double))
                    {
                        result = ((TimeSpan)LHSO).TotalDays * (Double)RHSO;
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof TimeSpan))
                    {
                        result = (Double)LHSO * ((TimeSpan)RHSO).TotalDays;
                    }
                    else*/ if ((LHSO instanceof Double) && (RHSO instanceof Double))
                    {
                        result = (Double)LHSO * (Double)RHSO;
                    }
                    break;
                case div:
                    /*if ((LHSO instanceof TimeSpan) && (RHSO instanceof TimeSpan))
                    {
                        result = ((TimeSpan)LHSO).TotalDays / ((TimeSpan)RHSO).TotalDays;
                    }
                    else if ((LHSO instanceof TimeSpan) && (RHSO instanceof Double))
                    {
                        result = ((TimeSpan)LHSO).TotalDays / (Double)RHSO;
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof TimeSpan))
                    {
                        result = (Double)LHSO / ((TimeSpan)RHSO).TotalDays;
                    }
                    else*/ if ((LHSO instanceof Double) && (RHSO instanceof Double))
                    {
                        result = (Double)LHSO / (Double)RHSO;
                    }
                    break;
                case mod:
                    /*if ((LHSO instanceof TimeSpan) && (RHSO instanceof TimeSpan))
                    {
                        result = ((TimeSpan)LHSO).TotalDays % ((TimeSpan)RHSO).TotalDays;
                    }
                    else if ((LHSO instanceof TimeSpan) && (RHSO instanceof Double))
                    {
                        result = ((TimeSpan)LHSO).TotalDays % (Double)RHSO;
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof TimeSpan))
                    {
                        result = (Double)LHSO % ((TimeSpan)RHSO).TotalDays;
                    }
                    else*/ if ((LHSO instanceof Double) && (RHSO instanceof Double))
                    {
                        result = (Double)LHSO % (Double)RHSO;
                    }

                    break;
                  default:
                	  break;
            }
        }

        return result;
    }
}
