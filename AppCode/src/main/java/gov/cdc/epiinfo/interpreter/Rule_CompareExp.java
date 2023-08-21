package gov.cdc.epiinfo.interpreter;

import java.util.Date;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_CompareExp extends EnterRule 
{
    EnterRule ConcatExp = null;
    String op = null;
    EnterRule CompareExp = null;
    //String STRING = null;
    
    public Rule_CompareExp(Rule_Context pContext, Reduction pToken)
    {
    	 super(pContext);
        // <Concat Exp> LIKE String
        // <Concat Exp> '=' <Compare Exp>
        // <Concat Exp> '<>' <Compare Exp>
        // <Concat Exp> '>' <Compare Exp>
        // <Concat Exp> '>=' <Compare Exp>
        // <Concat Exp> '<' <Compare Exp>
        // <Concat Exp> '<=' <Compare Exp>
        // <Concat Exp>
        
        this.ConcatExp = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
        if (pToken.size() > 1)
        {
            op = pToken.get(1).toString().replace("'", "").toLowerCase();

            if (op.equalsIgnoreCase("LIKE"))
            {
                //this.STRING = pToken.get(2).toString();
            	Object data = pToken.get(2).getData();
            	if(data instanceof Reduction)
            	{
            		this.CompareExp = EnterRule.BuildStatements(pContext, (Reduction) data);
            	}
            	else
            	{
            		Rule_Value value = new Rule_Value(data.toString());
            		value.Context = pContext;
            		value.Id = data.toString();
            		
            		this.CompareExp = value;
            	}
            }
            else
            {
                this.CompareExp = EnterRule.BuildStatements(pContext, pToken.get(2).asReduction());
            }
        }
    }


    /// <summary>
    /// perfoms comparison operations on expression ie (=, <=, >=, Like, >, <, and <)) returns a boolean
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (op == null)
        {
            result = this.ConcatExp.Execute();
        }
        else
        {
            Object LHSO = this.ConcatExp.Execute();
            Object RHSO = this.CompareExp.Execute();
            try
            {
            	if (LHSO.toString().toLowerCase().equals("systemdate") || LHSO.toString().toLowerCase().equals("systemtime"))
            	{
            		LHSO = new Date();
            	}
            	if (RHSO.toString().toLowerCase().equals("systemdate") || RHSO.toString().toLowerCase().equals("systemtime"))
            	{
            		RHSO = new Date();
            	}
            }
            catch (Exception ex)
            {
            	
            }
            Double TryValue = 0.0;
            int i;

            if (Util.IsEmpty(LHSO) && Util.IsEmpty(RHSO) && op.equals("="))
            {
                result = true;
            }
            else if (Util.IsEmpty(LHSO) && Util.IsEmpty(RHSO) && op.equals("<>"))
            {
                return false;
            }
            else if ((Util.IsEmpty(LHSO) || Util.IsEmpty(RHSO)))
            {
                if (op.equals("<>"))
                {
                    return !(Util.IsEmpty(LHSO) && Util.IsEmpty(RHSO));
                }
                else
                {
                    result = false;
                }
            }
            else if (op.equalsIgnoreCase("LIKE"))
            {
                //String testValue = "^" + RHSO.toString().Replace("*", "(\\s|\\w)*") + "$";
                String testValue = "^" + RHSO.toString().replace("*", ".*") + "$";
                //java.util.regex.Pattern  re = new java.util.regex.Pattern(testValue, System.Text.RegularExpressions.RegexOptions.IgnoreCase);


                result = java.util.regex.Pattern.matches(testValue, LHSO.toString());
            }
            else
            {

                if (LHSO instanceof Number && RHSO instanceof Number)
                {
                    /*LHSO = Convert.ToDouble(LHSO);
                    RHSO = Convert.ToDouble(RHSO);*/
                	LHSO = LHSO;
                    RHSO = RHSO;
                }

                if (!LHSO.getClass().equals(RHSO.getClass()))
                {
                    if (RHSO instanceof Boolean && op.equals("="))
                    {
                        result = (RHSO.equals(!Util.IsEmpty(LHSO)));
                    }
                    else if (LHSO instanceof String && RHSO instanceof Number)
                    {
                    	
                    	TryValue = Double.parseDouble(LHSO.toString());
                    	
                        i = TryValue.compareTo((Double)RHSO);

                        switch (Operator_Enum.Convert(op))
                        {
                            case e:
                                result = (i == 0);
                                break;
                            case ne:
                                result = (i != 0);
                                break;
                            case lt:
                                result = (i < 0);
                                break;
                            case gt:
                                result = (i > 0);
                                break;
                            case gte:
                                result = (i >= 0);
                                break;
                            case lte:
                                result = (i <= 0);
                                break;
                            default:
                            	break;
                        }

                    }
                    else if (RHSO instanceof String && LHSO instanceof Number)
                    {
                    	TryValue = Double.parseDouble(RHSO.toString());
                    	
                        i = TryValue.compareTo((Double)LHSO);

                        switch (Operator_Enum.Convert(op))
                        {
                            case e:
                                result = (i == 0);
                                break;
                            case ne:
                                result = (i != 0);
                                break;
                            case lt:
                                result = (i < 0);
                                break;
                            case gt:
                                result = (i > 0);
                                break;
                            case gte:
                                result = (i >= 0);
                                break;
                            case lte:
                                result = (i <= 0);
                                break;
                            default:
                            	break;
                        }
                    }
                    else if (op.equals("=") && (LHSO instanceof Boolean || RHSO instanceof Boolean))
                    {
                        if (LHSO instanceof Boolean && RHSO instanceof Boolean)
                        {
                            result = LHSO == RHSO;   
                        }
                        else if (LHSO instanceof Boolean)
                        {
                            result = LHSO == this.ConvertStringToBoolean(RHSO.toString());
                        }
                        else
                        {
                            result = this.ConvertStringToBoolean(LHSO.toString()) == RHSO;
                        }
                    }
                    else
                    {
                        i =LHSO.toString().compareToIgnoreCase(RHSO.toString());

                        switch (Operator_Enum.Convert(op))
                        {
                            case e:
                                result = (i == 0);
                                break;
                            case ne:
                                result = (i != 0);
                                break;
                            case lt:
                                result = (i < 0);
                                break;
                            case gt:
                                result = (i > 0);
                                break;
                            case gte:
                                result = (i >= 0);
                                break;
                            case lte:
                                result = (i <= 0);
                                break;
                            default:
                            	break;
                        }
                    }
                }
                else
                {
                    i = 0;

                    if (LHSO.getClass().getName().toUpperCase() == "STRING" && RHSO.getClass().getName().toUpperCase() == "STRING")
                    {
                        i = LHSO.toString().trim().compareToIgnoreCase(RHSO.toString().trim());
                    }
                    else if (LHSO instanceof Comparable && RHSO instanceof Comparable)
                    {
                        i = ((Comparable)LHSO).compareTo(RHSO);
                    }

                    switch (Operator_Enum.Convert(op))
                    {
                        case e:
                            result = (i == 0);
                            break;
                        case ne:
                            result = (i != 0);
                            break;
                        case lt:
                            result = (i < 0);
                            break;
                        case gt:
                            result = (i > 0);
                            break;
                        case gte:
                            result = (i >= 0);
                            break;
                        case lte:
                            result = (i <= 0);
                            break;
                        default:
                        	break;
                    }
                }
            }
        }
        return result;
    }

    
	
}
