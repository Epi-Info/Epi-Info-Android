package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.interpreter.CSymbol.DataType;
import gov.cdc.epiinfo.interpreter.EnterRule.Rule_Enum;

import java.text.DateFormat;
import java.util.Date;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;


public class Rule_Value extends EnterRule 
{

    public String Id = null;
    String Namespace = null;
    public Object value = null;
    public DataType VariableDataType;
    boolean UseParenthesis = false;

    //object ReturnResult = null;

    public Rule_Value(Rule_Context pContext, Reduction pToken) 
    {
        /* ::= Identifier	| <Literal> | Boolean | '(' <Expr List> ')' */
    	super(pContext);
    	
        //if (pToken.getParent().containsOneNonTerminal())
        //{
            //NonterminalToken T = (NonterminalToken)pToken;
            if (pToken.size() == 1)
            {
                switch (Rule_Enum.Convert(pToken.getParent().getHead().getName()))
                {

                    case Qualified_ID:
                    case Identifier:
                        this.Id = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case FunctionCall:
                        //this.value = new Rule_FunctionCall(pContext, (NonterminalToken)T.Tokens[0]);
                        this.value = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(0).getData());
                        break;

                    case Literal_Date:
                        this.VariableDataType = CSymbol.DataType.Date;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case Literal:
                    case Literal_String:
                    case String:
                        this.VariableDataType = CSymbol.DataType.Text;
                        this.value = this.ExtractIdentifier(pToken.get(0)).replaceAll("^\"|\"$","");
                        break;
                    case Number:
                    case Real_Number:
                    case Decimal_Number:
                    case Hex_Number:
                        this.VariableDataType = CSymbol.DataType.Number;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case Subroutine_Statement:

                        break;
                    case Boolean:
                        this.VariableDataType = DataType.Boolean;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case RealLiteral:
                    case DecLiteral:
                    case HexLiteral:
                        this.VariableDataType = DataType.Number;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case Date:
                        this.VariableDataType = DataType.Date;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case Time:
                        this.VariableDataType = DataType.Time;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        break;
                    case Literal_Char:
                    case CharLiteral:
                        this.VariableDataType = DataType.Text;
                        this.value = this.ExtractIdentifier(pToken.get(0)).replaceAll("^'|'$","");
                        break;
                    default:
                    	this.VariableDataType = DataType.Unknown;
                        this.value = this.ExtractIdentifier(pToken.get(0));
                        
                        	if(this.value.toString().equalsIgnoreCase("(+)"))
                        	{
                                this.VariableDataType = CSymbol.DataType.Boolean;
                                this.value = true;
                        	}
                        	else if(this.value.toString().equalsIgnoreCase("(-)"))
                        	{
                                this.VariableDataType = CSymbol.DataType.Boolean;
                                this.value = false;
                        	}
                        	else if(this.value.toString().equalsIgnoreCase("(.)"))
                        	{
                                this.VariableDataType = CSymbol.DataType.Boolean;
                                this.value = null;
                        	}
                            
                            break;
                        

                }
                
                
            }
            else
            {
                //this.value = new Rule_ExprList(pContext, (NonterminalToken)T.Tokens[1]);
                if (pToken.size() == 0)
                {
                    this.value = EnterRule.BuildStatements(pContext, pToken);

                }
                else if (pToken.getParent().getHead().getName() == "<Fully_Qualified_Id>" || pToken.getParent().getHead().getName() == "<Qualified ID>")
                {
                    //String[] temp = this.ExtractTokens(T.Tokens).Split(' ');
                	String[] temp = this.ExtractIdentifier(pToken.get(0)).split(" ");
                    this.Namespace = temp[0];
                    this.Id = temp[2];
                }
                else
                {
                    if (this.ExtractIdentifier(pToken.get(0)) == "(")
                    {
                        UseParenthesis = true;
                    }
                    
                    //this.value = new Rule_ExprList(pContext, (NonterminalToken)T.Tokens[1]);
                    if (pToken.get(1).getData().getClass() == Reduction.class)
                    {
                    	this.value = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
                    }
                    else
                    {
                    	this.value = pToken.get(1).getData().toString();
                    }

                }
            }
        /*}
        else
        {
            Token TT = pToken.get(0);

            switch (Rule_Enum.Convert(pToken.getParent().getHead().getName()))
            {
                case Boolean:
                    this.VariableDataType = DataType.Boolean;
                    this.value = this.ExtractIdentifier(TT);
                    break;
                case RealLiteral:
                case DecLiteral:
                case HexLiteral:
                    this.VariableDataType = DataType.Number;
                    this.value = this.ExtractIdentifier(TT);
                    break;
                case Date:
                    this.VariableDataType = DataType.Date;
                    this.value = this.ExtractIdentifier(TT);
                    break;
                case Time:
                    this.VariableDataType = DataType.Time;
                    this.value = this.ExtractIdentifier(TT);
                    break;
                case Identifier:
                    this.Id = this.ExtractIdentifier(TT);
                    this.VariableDataType = DataType.Unknown;
                    break;
                case String:
                    this.VariableDataType = DataType.Text;
                    this.value = this.ExtractIdentifier(TT);
                    break;
                default:
                    this.VariableDataType = DataType.Unknown;
                    this.value = this.ExtractIdentifier(TT);
                    break;
            }
        }*/

        if (this.Id == null && this.value == null)
        {

        }
    }

    public Rule_Value(String pValue)
    {
        this.value = pValue;
    }


    /// <summary>
    /// performs execution of retrieving the value of a variable or expression
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        if (this.Id != null)
        {
            CSymbol var;
            DataType dataType = DataType.Unknown;
            String dataValue = null;

            var = this.Context.GetCurrentScope().resolve(this.Id, this.Namespace);

            if (var != null)
            {
                dataType = var.Type;
                if (this.VariableDataType == null)
                {
                	this.VariableDataType = dataType;
                }
                //dataValue = var.Value;
                switch(this.VariableDataType)
                {
	                case Date:
	                	if( var.Value instanceof Date)
	                	{
	                		result =  var.Value;
	                	}
						else
						{
							DateFormat dateFormat = DateFormat.getDateInstance();
							try
							{
								Date convertedDate = dateFormat.parse( var.Value.toString()); 
								result = convertedDate;
							}
							catch(Exception ex)
							{
								// do nothing for now
							}
						}
	                    break;
	                case Text:
	                	result = var.Value;
	                    break;
	                case Number:
	                	result = var.Value;
	                    break;
	                case Boolean:
	                	result = var.Value;
	                    break;
	                case Time:
	                	result = var.Value;
	                    break;
	                case Unknown:
	                default:
	                	if (value instanceof EnterRule)
	                    {
	                        result = ((EnterRule)value).Execute();
	                    }
	                	else
	                	{
	                		result = var.Value;
	                	}
	                	break;
                }
            }
            else
            {
                if (this.Context.CheckCodeInterface != null)
                {
                    //DataType dt = DataType.Unknown;

                    result = this.Context.CheckCodeInterface.GetValue(this.Id.toLowerCase());
                    //dataType = dt;
                }
            }

            //result = ConvertEpiDataTypeToSystemObject(dataType, dataValue);
        }
        else
        {
            if (value instanceof EnterRule)
            {
                result = ((EnterRule)value).Execute();
            }
            else 
            {
            	switch(this.VariableDataType)
                {
	                case Date:
	                	if(this.value instanceof Date)
	                	{
	                		result = this.value;
	                	}
						else
						{
							DateFormat dateFormat = DateFormat.getDateInstance();
							try
							{
								Date convertedDate = dateFormat.parse(this.value.toString()); 
								result = convertedDate;
							}
							catch(Exception ex)
							{
								// do nothing for now
							}
						}
	                	
	                    break;
	                case Text:
	                	result = this.value;
	                    break;
	                case Number:
	                	result = Double.parseDouble(this.value.toString());
	                    break;
	                case Boolean:
	                	result = this.value;
	                    break;
	                case Time:
	                	result = this.value;
	                    break;
	                case Unknown:
	                default:
	                	result = this.value;
	                	break;
                }
            }
        }
        
        return result;
    }

     /*
    private object ConvertEpiDataTypeToSystemObject(DataType dataType, String dataValue)
    {
        Object result = null;
        if (dataValue != null)
        {
            switch (dataType)
            {
                case Boolean:
                case YesNo:
                    result = boolean;
                    if (dataValue.toString() == "(+)" || dataValue.toString().equalsIgnoreCase("true") || dataValue.toString().equalsIgnoreCase("1") || dataValue.toString().equalsIgnoreCase("yes"))
                        result = true;
                    else if (dataValue == "(-)" || dataValue.toString().equalsIgnoreCase("false") || dataValue.toString().equalsIgnoreCase("0") || dataValue.toString().equalsIgnoreCase("no"))
                        result = false;
                    else
                        result = null;
                    break;

                case Number:
                    double num;
                    if (double.TryParse(dataValue, out num))
                        result = num;
                    else
                        result = null;
                    break;

                case Date:
                case DateTime:
                case Time:
                    DateTime dateTime;
                    if (DateTime.TryParse(dataValue, out dateTime))
                        result = dateTime;
                    else
                        result = null;
                    break;
                case PhoneNumber:
                case GUID:
                case Text:
                    if (dataValue != null)
                        result = dataValue.Trim().Trim('\"');
                    else
                        result = null;
                    break;
                case Unknown:
                default:
                    double double_compare;
                    DateTime DateTime_compare;
                    bool bool_compare;

                    if (double.TryParse(dataValue, out double_compare))
                    {
                        result = double_compare;
                    }
                    else
                        if (DateTime.TryParse(dataValue, out DateTime_compare))
                        {
                            result = DateTime_compare;
                        }
                        else
                            if (bool.TryParse(dataValue, out bool_compare))
                            {
                                result = bool_compare;
                            }
                            else { result = dataValue; }
                    break;
            }
        }
        return result;
    }
   
    
    private Object ParseDataStrings(Object subject)
    {
        Object result = null;
        if (subject is Rule_ExprList)
        {
            result = ((Rule_ExprList)subject).Execute();
        }
        else if (subject is Rule_FunctionCall)
        {
            result = ((Rule_FunctionCall)subject).Execute();
        }
        else if (subject is String)
        {
            Double number;
            DateTime dateTime;

            result = ((String)subject).Trim('\"');
            //removing the "1" and "0" conditions here because an expression like 1 + 0 was evaluating as two booleans
            //if ((String)subject == "1" || (String)subject == "(+)" || ((String)subject).ToLower() == "true")
            if ((String)subject == "(+)" || ((String)subject).ToLower() == "true" || ((String)subject).ToLower() == "yes")
            {
                result = new Boolean();
                result = true;
            }
            //else if ((String)subject == "0" || (String)subject == "(-)" || ((String)subject).ToLower() == "false")
            else if ((String)subject == "(-)" || ((String)subject).ToLower() == "false" || ((String)subject).ToLower() == "no")
            {
                result = new Boolean();
                result = false;
            }
            else if ((String)subject == "(.)")
            {
                result = null;
            }
            else if (Double.TryParse(result.ToString(), out number))
            {
                result = number;
            }
            else if (DateTime.TryParse(result.ToString(), out dateTime))
            {
                result = dateTime;
            }
        }
        else if (subject is Boolean)
        {
            result = subject;
        }
        return result;
    }*/


    
}
