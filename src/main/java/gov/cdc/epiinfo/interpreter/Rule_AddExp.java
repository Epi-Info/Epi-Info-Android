package gov.cdc.epiinfo.interpreter;

import java.util.Calendar;
import java.util.Date;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_AddExp extends EnterRule 
{
    EnterRule MultExp = null;
    String operation = null;
    EnterRule AddExp = null;

    public Rule_AddExp(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        /*::= <Mult Exp> '+' <Add Exp>
        							| <Mult Exp> '-' <Add Exp>
        							| <Mult Exp>*/

        this.MultExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());

        if (pToken.size() > 1)
        {
            operation = this.ExtractIdentifier(pToken.get(1));
            this.AddExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(2).getData());
        }
    }
    /// <summary>
    /// performs an addition / subtraction or pass thru rule
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (operation == null)
        {
            result = this.MultExp.Execute();
        }
        else
        { 
            Object LHSO = this.MultExp.Execute();
            Object RHSO = this.AddExp.Execute();


            //if (this.NumericTypeList.contains(LHSO.getClass().getName().toUpperCase()) && this.NumericTypeList.contains(RHSO.getClass().getName().toUpperCase()))
            //if (this.isNumeric(LHSO) && this.isNumeric(RHSO))
            if(LHSO instanceof Number && RHSO instanceof Number)
            {
                /*LHSO = Convert.ToDouble(LHSO);
                RHSO = Convert.ToDouble(RHSO);*/
            	LHSO = LHSO;
                RHSO = RHSO;
            }


            switch (Operator_Enum.Convert(operation))
            {
                case add:
                    if ((LHSO instanceof Date) && (RHSO instanceof Date))
                    {
                    	long sum = ((Date)LHSO).getTime() + ((Date)RHSO).getTime();
                    	Calendar c = Calendar.getInstance();
                    	c.setTime(new Date(sum));
                        result =c.getTime();
                    }
                    else if ((LHSO instanceof Date) && (RHSO instanceof Double))
                    {
                    	Calendar c = Calendar.getInstance();
                    	c.setTime((Date)LHSO);
                    	c.add(Calendar.DATE, (int) Math.round((Double) RHSO));
                    	c.get(Calendar.MILLISECOND);
                        result =c.getTime();
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof Date))
                    {
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof Double))
                    {
                        result = (Double)LHSO + (Double)RHSO;
                    }
                    break;

                case sub:
                    if ((LHSO instanceof Date) && (RHSO instanceof Date))
                    {
                    	long sum = ((Date)LHSO).getTime() - ((Date)RHSO).getTime();
                    	Calendar c = Calendar.getInstance();
                    	c.setTime(new Date(sum));
                        result =c.getTime();
                    }
                    else if ((LHSO instanceof Date) && (RHSO instanceof Double))
                    {
                    	Calendar c = Calendar.getInstance();
                    	c.setTime((Date)LHSO);
                    	c.add(Calendar.DATE, (int) Math.round((Double)RHSO * -1));
                    	c.get(Calendar.MILLISECOND);
                        result =c.getTime();
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof Date))
                    {
                    }
                    else if ((LHSO instanceof Double) && (RHSO instanceof Double))
                    {
                        result = (Double)LHSO - (Double)RHSO;
                    }
                    break;
                default:
                	break;
            }
        }

        return result;
    }
}
