package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.interpreter.functions.Rule_Abs;
import gov.cdc.epiinfo.interpreter.functions.Rule_Cos;
import gov.cdc.epiinfo.interpreter.functions.Rule_CurrentUser;
import gov.cdc.epiinfo.interpreter.functions.Rule_Day;
import gov.cdc.epiinfo.interpreter.functions.Rule_Days;
import gov.cdc.epiinfo.interpreter.functions.Rule_Exp_Func;
import gov.cdc.epiinfo.interpreter.functions.Rule_GetCoordinates;
import gov.cdc.epiinfo.interpreter.functions.Rule_Hour;
import gov.cdc.epiinfo.interpreter.functions.Rule_Hours;
import gov.cdc.epiinfo.interpreter.functions.Rule_Ln;
import gov.cdc.epiinfo.interpreter.functions.Rule_Log;
import gov.cdc.epiinfo.interpreter.functions.Rule_Minute;
import gov.cdc.epiinfo.interpreter.functions.Rule_Minutes;
import gov.cdc.epiinfo.interpreter.functions.Rule_Month;
import gov.cdc.epiinfo.interpreter.functions.Rule_Months;
import gov.cdc.epiinfo.interpreter.functions.Rule_NumToDate;
import gov.cdc.epiinfo.interpreter.functions.Rule_NumToTime;
import gov.cdc.epiinfo.interpreter.functions.Rule_Rnd;
import gov.cdc.epiinfo.interpreter.functions.Rule_Round;
import gov.cdc.epiinfo.interpreter.functions.Rule_Second;
import gov.cdc.epiinfo.interpreter.functions.Rule_Seconds;
import gov.cdc.epiinfo.interpreter.functions.Rule_SendSMS;
import gov.cdc.epiinfo.interpreter.functions.Rule_Sin;
import gov.cdc.epiinfo.interpreter.functions.Rule_Strlen;
import gov.cdc.epiinfo.interpreter.functions.Rule_Substring;
import gov.cdc.epiinfo.interpreter.functions.Rule_Tan;
import gov.cdc.epiinfo.interpreter.functions.Rule_Trunc;
import gov.cdc.epiinfo.interpreter.functions.Rule_TxtToDate;
import gov.cdc.epiinfo.interpreter.functions.Rule_TxtToNum;
import gov.cdc.epiinfo.interpreter.functions.Rule_Uppercase;
import gov.cdc.epiinfo.interpreter.functions.Rule_Validate;
import gov.cdc.epiinfo.interpreter.functions.Rule_Year;
import gov.cdc.epiinfo.interpreter.functions.Rule_Years;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;



public class Rule_FunctionCall  extends EnterRule
{
	private String functionName = null;
    private EnterRule functionCall = null;

    private String ClassName = null;
    private String MethodName = null;
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();


    /// <summary>
    /// Constructor for Rule_FunctionCall
    /// </summary>
    /// <param name="pToken">The token to build the reduction with.</param>
    public Rule_FunctionCall(Rule_Context pContext, Reduction pToken)
    {
        /*
        <FunctionCall> ::= <FuncName1> '(' <FunctionParameterList> ')'
		    | <FuncName1> '(' <FunctionCall> ')' 
		    | <FuncName2>
         */

	    	super(pContext);
	    	Token T = null;
	        //if (pToken.Tokens.Length == 1)
	    	if (pToken.size() == 1)
	        {
	            if (pToken.getParent().getHead().getName().equalsIgnoreCase("<FunctionCall>"))
	            {
	                T = pToken.get(0);
	            }
	            else
	            {
	                T = pToken.get(0);
	            }
	        }
	        else
	        {
	            T = pToken.get(2);
	        }
	
	
	        String temp = null;
	        String[] temp2 = null;
	
	        //if (pToken.Tokens[0] is NonterminalToken)
	        if (pToken.get(0).getType() == SymbolType.NON_TERMINAL)
	        {
	            //temp = this.ExtractTokens(((NonterminalToken)pToken.Tokens[0]).Tokens).Replace(" . ", ".");
	        	temp = this.ExtractIdentifier(pToken.get(0)).replace(" . ", ".");
	        	temp2 = temp.split(".");
	
	        }
	        else
	        {
	            temp = pToken.get(0).getData().toString().replace(" . ", ".");
	        }
	
	        if(temp2 != null && temp2.length > 1)
	        {
	            this.ClassName = temp2[0].trim();
	            this.MethodName = temp2[1].trim();
	
	            this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken.get(2).asReduction());
	        }
	        else
	        {
	            //functionName = EnterRule.GetIdentifier(pToken.get(0).toString());
	        	functionName = this.ExtractIdentifier(pToken.get(0));
	        	
	
	            switch (Function_Enum.Convert(functionName))
	            {
	                case ABS:
	                    functionCall = new Rule_Abs(pContext, T.asReduction());
	                    break;
	                case VALIDATE:
	                    functionCall = new Rule_Validate(pContext, T.asReduction());
	                    break;
	                case COS:
	                    functionCall = new Rule_Cos(pContext, T.asReduction());
	                    break;
	                case CURRENTUSER:
                        functionCall = new Rule_CurrentUser(pContext, T.asReduction());
                        break;
	                case DAY:
	                    functionCall = new Rule_Day(pContext, T.asReduction());
	                    break;
	                case DAYS:
	                    functionCall = new Rule_Days(pContext, T.asReduction());
	                    break;/*
	                case FORMAT:
	                    functionCall = new Rule_Format(pContext, T);
	                    break;*/
	                case HOUR:
	                    functionCall = new Rule_Hour(pContext, T.asReduction());
	                    break;
	                case HOURS:
	                    functionCall = new Rule_Hours(pContext, T.asReduction());
	                    break;
	                case MINUTE:
	                    functionCall = new Rule_Minute(pContext, T.asReduction());
	                    break;
	                case MINUTES:
	                    functionCall = new Rule_Minutes(pContext, T.asReduction());
	                    break;
	                case MONTH:
	                    functionCall = new Rule_Month(pContext, T.asReduction());
	                    break;
	                case MONTHS:
	                    functionCall = new Rule_Months(pContext, T.asReduction());
	                    break;
	                case NUMTODATE:
	                    functionCall = new Rule_NumToDate(pContext, T.asReduction());
	                    break;
	                case NUMTOTIME:
	                    functionCall = new Rule_NumToTime(pContext, T.asReduction());
	                    break;/*
	                case RECORDCOUNT:
	                    functionCall = new Rule_RecordCount(pContext, T);
	                    break;*/
	                case SECOND:
	                    functionCall = new Rule_Second(pContext, T.asReduction());
	                    break;
	                case SECONDS:
	                    functionCall = new Rule_Seconds(pContext, T.asReduction());
	                    break;/*
	                case SYSTEMDATE:
	                    functionCall = new Rule_SystemDate(pContext, T);
	                    break;
	                case SYSTEMTIME:
	                    functionCall = new Rule_SystemTime(pContext, T);
	                    break;*/
	                case TXTTODATE:
	                    functionCall = new Rule_TxtToDate(pContext, T.asReduction());
	                    break;
	                case TXTTONUM:
	                    functionCall = new Rule_TxtToNum(pContext, T.asReduction());
	                    break;
	                case YEAR:
	                    functionCall = new Rule_Year(pContext, T.asReduction());
	                    break;
	                case YEARS:
	                    functionCall = new Rule_Years(pContext, T.asReduction());
	                    break;
	                case STRLEN:
	                    functionCall = new Rule_Strlen(pContext, T.asReduction());
	                    break;
	                case SUBSTRING:
	                    functionCall = new Rule_Substring(pContext, T.asReduction());
	                    break;
	                case RND:
	                    functionCall = new Rule_Rnd(pContext, T.asReduction());
	                    break;
	                case EXP:
	                    functionCall = new Rule_Exp_Func(pContext, T.asReduction());
	                    break;
	                case LN:
	                    functionCall = new Rule_Ln(pContext, T.asReduction());
	                    break;
	                case ROUND:
	                    functionCall = new Rule_Round(pContext, T.asReduction());
	                    break;
	                case LOG:
	                    functionCall = new Rule_Log(pContext, T.asReduction());
	                    break;
	                case SIN:
	                    functionCall = new Rule_Sin(pContext, T.asReduction());
	                    break;
	                case TAN:
	                    functionCall = new Rule_Tan(pContext, T.asReduction());
	                    break;
	                case TRUNC:
	                    functionCall = new Rule_Trunc(pContext, T.asReduction());
	                    break;/*
	                case STEP:
	                    functionCall = new Rule_Step(pContext, T);
	                    break;*/
	                case UPPERCASE:
	                    functionCall = new Rule_Uppercase(pContext, T.asReduction());
	                    break;/*
	                case FINDTEXT:
	                    functionCall = new Rule_FindText(pContext, T);
	                    break;
	                case ENVIRON:
	                    functionCall = new Rule_Environ(pContext, T);
	                    break;
	                case EXISTS:
	                    functionCall = new Rule_Exists(pContext, T);
	                    break;
	                case FILEDATE:
	                    functionCall = new Rule_FileDate(pContext, T);
	                    break;
	                case ZSCORE:
	                    functionCall = new Rule_ZSCORE(pContext, T);
	                    break;
	                case PFROMZ:
	                    functionCall = new Rule_PFROMZ(pContext, T);
	                    break;
	                case EPIWEEK:
	                    functionCall = new Rule_EPIWEEK(pContext, T);
	                    break;*/
	                case SendSMS:
	                	functionCall = new Rule_SendSMS(pContext, T.asReduction());
	                	break;
	                case GetCoordinates:
	                	functionCall = new Rule_GetCoordinates(pContext, T.asReduction());
	                	break;
	                default:
					try {
						throw new Exception("Function name " + functionName.toUpperCase() + " is not a recognized function.");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
	            }
	        }
        }
        
        /// <summary>
        /// Executes the reduction.
        /// </summary>
        /// <returns>Returns the result of executing the reduction.</returns>
        @Override
        public Object Execute()
        {
            Object result = null;
            /*
            if (string.IsNullOrEmpty(this.functionName))
            {

                if (this.Context.DLLClassList.ContainsKey(this.ClassName.ToLower()))
                {
                	Object[] args = this.ParameterList.ToArray();
                    if (this.ParameterList.Count > 0)
                    {
                        args = new Object[this.ParameterList.Count];
                        for (int i = 0; i < this.ParameterList.Count; i++)
                        {
                            args[i] = this.ParameterList[i].Execute();
                        }
                    }
                    else
                    {
                        args = new Object[0];
                    }

                    result = this.Context.DLLClassList[this.ClassName].Execute(this.MethodName, args);
                }
            }
            else
            {*/
                if (this.functionCall != null)
                {
                    result = this.functionCall.Execute();
                }
            //}
            return result;
        }

        public enum Function_Enum
        {
        	ABS(0),
        	COS(1),
        	DAY(2),
        	DAYS(3),
        	FORMAT(4),
        	HOUR(5),
        	HOURS(6),
        	MINUTE(7),
        	MINUTES(8),
        	MONTH(9),
        	MONTHS(10),
        	NUMTODATE(11),
        	NUMTOTIME(12),
        	RECORDCOUNT(13),
        	SECOND(14),
        	SECONDS(15),
        	SYSTEMDATE(16),
        	SYSTEMTIME(17),
        	TXTTODATE(18),
        	TXTTONUM(19),
        	YEAR(20),
        	YEARS(21),
        	STRLEN(22),
        	SUBSTRING(23),
        	RND(24),
        	EXP(25),
        	LN(26),
        	ROUND(27),
        	LOG(28),
        	SIN(29),
        	TAN(30),
        	TRUNC(31),
        	STEP(32),
        	UPPERCASE(33),
        	FINDTEXT(34),
        	ENVIRON(35),
        	EXISTS(36),
        	FILEDATE(37),
        	ZSCORE(38),
        	PFROMZ(39),
        	EPIWEEK(30),
        	GetCoordinates(31),
        	SendSMS(32),
        	CURRENTUSER(33),
        	VALIDATE(34);
        	
        	
	        private int value;  
			public int getValue() 
			{  
				return value;  
			}
			Function_Enum() {}
		       
			Function_Enum(int pValue)
			{  
				this.value = pValue;  
			}  
	        
			static HashMap<String,Function_Enum> StringEnum;
			
			static
			{
				
				StringEnum = new HashMap<String,Function_Enum>(); 

				StringEnum.put("ABS".toLowerCase(),ABS);
				StringEnum.put("VALIDATE".toLowerCase(),VALIDATE);
				StringEnum.put("COS".toLowerCase(),COS);
				StringEnum.put("CURRENTUSER".toLowerCase(),CURRENTUSER);
				StringEnum.put("DAY".toLowerCase(),DAY);
				StringEnum.put("DAYS".toLowerCase(),DAYS);
				StringEnum.put("FORMAT".toLowerCase(),FORMAT);
				StringEnum.put("HOUR".toLowerCase(),HOUR);
				StringEnum.put("HOURS".toLowerCase(),HOURS);
				StringEnum.put("MINUTE".toLowerCase(),MINUTE);
				StringEnum.put("MINUTES".toLowerCase(),MINUTES);
				StringEnum.put("MONTH".toLowerCase(),MONTH);
				StringEnum.put("MONTHS".toLowerCase(),MONTHS);
				StringEnum.put("NUMTODATE".toLowerCase(),NUMTODATE);
				StringEnum.put("NUMTOTIME".toLowerCase(),NUMTOTIME);
				StringEnum.put("RECORDCOUNT".toLowerCase(),RECORDCOUNT);
				StringEnum.put("SECOND".toLowerCase(),SECOND);
				StringEnum.put("SECONDS".toLowerCase(),SECONDS);
				StringEnum.put("SYSTEMDATE".toLowerCase(),SYSTEMDATE);
				StringEnum.put("SYSTEMTIME".toLowerCase(),SYSTEMTIME);
				StringEnum.put("TXTTODATE".toLowerCase(),TXTTODATE);
				StringEnum.put("TXTTONUM".toLowerCase(),TXTTONUM);
				StringEnum.put("YEAR".toLowerCase(),YEAR);
				StringEnum.put("YEARS".toLowerCase(),YEARS);
				StringEnum.put("STRLEN".toLowerCase(),STRLEN);
				StringEnum.put("SUBSTRING".toLowerCase(),SUBSTRING);
				StringEnum.put("RND".toLowerCase(),RND);
				StringEnum.put("EXP".toLowerCase(),EXP);
				StringEnum.put("LN".toLowerCase(),LN);
				StringEnum.put("ROUND".toLowerCase(),ROUND);
				StringEnum.put("LOG".toLowerCase(),LOG);
				StringEnum.put("SIN".toLowerCase(),SIN);
				StringEnum.put("TAN".toLowerCase(),TAN);
				StringEnum.put("TRUNC".toLowerCase(),TRUNC);
				StringEnum.put("STEP".toLowerCase(),STEP);
				StringEnum.put("UPPERCASE".toLowerCase(),UPPERCASE);
				StringEnum.put("FINDTEXT".toLowerCase(),FINDTEXT);
				StringEnum.put("ENVIRON".toLowerCase(),ENVIRON);
				StringEnum.put("EXISTS".toLowerCase(),EXISTS);
				StringEnum.put("FILEDATE".toLowerCase(),FILEDATE);
				StringEnum.put("ZSCORE".toLowerCase(),ZSCORE);
				StringEnum.put("PFROMZ".toLowerCase(),PFROMZ);
				StringEnum.put("EPIWEEK".toLowerCase(),EPIWEEK);  
				StringEnum.put("SendSMS".toLowerCase(),SendSMS);
				StringEnum.put("GetCoordinates".toLowerCase(),GetCoordinates);
			}
			
			
	       static Function_Enum Convert(String pValue) 
	       {  
	    	   Function_Enum result = null;
	    	   
	    	   
	    	   
	    	   
	    	   String value = pValue.replace("<", "");
	    	   value = value.replace(">", "");
	    	   
	    	   value = value.trim();
	    	   
	    	   
	    	   if(StringEnum.containsKey(value.toLowerCase()))
	    	   {
	    		   return StringEnum.get(value.toLowerCase());
	    	   }
	    	   else
	    	   {
	    		   return  result;
	    	   }
	       }
        	
        }
        
		
}
