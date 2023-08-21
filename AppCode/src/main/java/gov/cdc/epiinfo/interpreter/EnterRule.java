package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.interpreter.CSymbol.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;
import com.creativewidgetworks.goldparser.parser.GOLDParser;
import com.creativewidgetworks.goldparser.parser.ProcessRule;

/*
@ProcessRule(rule={
	    "<Expression> ::= <Expression> > <Add Exp>",
	    "<Expression> ::= <Expression> < <Add Exp>",
	    "<Expression> ::= <Expression> <= <Add Exp>",
	    "<Expression> ::= <Expression> >= <Add Exp>",
	    "<Expression> ::= <Expression> '==' <Add Exp>",
	    "<Expression> ::= <Expression> <> <Add Exp>",
	    "<Expression> ::= <Add Exp>",
	    "<Add Exp> ::= <Add Exp> '+' <Mult Exp>",
	    "<Add Exp> ::= <Add Exp> - <Mult Exp>",
	    "<Add Exp> ::= <Add Exp> & <Mult Exp>",
	    "<Add Exp> ::= <Mult Exp>",
	    "<Mult Exp> ::= <Mult Exp> '*' <Negate Exp>",
	    "<Mult Exp> ::= <Mult Exp> '/' <Negate Exp>",
	    "<Mult Exp> ::= <Negate Exp>",
	    "<Negate Exp> ::= <Value>"
	})*/


public abstract class EnterRule 
{

    public Rule_Context Context;
    
    public EnterRule() 
    {
        //this.Context = new Rule_Context();
    }

    public EnterRule(Rule_Context pContext)
    {
        this.Context = pContext;
    }

    public abstract Object Execute();


    public String toString()
    {
        return "";
    }

    public boolean IsNull() 
    { 
        return false; 
    }
    
    public static boolean isNullOrEmpty(String param) 
    {
        return param == null || param.trim().length() == 0;
    }
    
    public static boolean isEmpty(Object obj)
    {
        if (obj == null) return true;
        else return isNullOrEmpty(obj.toString());
    }
    

    protected Object ConvertStringToBoolean(String pValue)
    {
        Object result = null;

        if(
			pValue.equalsIgnoreCase("(+)") ||
			pValue.equalsIgnoreCase("YES") ||
			pValue.equalsIgnoreCase("Y") ||
			pValue.equalsIgnoreCase("TRUE") ||
			pValue.equalsIgnoreCase("T")
        	)
                result = true;

        if(
			pValue.equalsIgnoreCase("(-)") ||
			pValue.equalsIgnoreCase("NO") ||
			pValue.equalsIgnoreCase("N") ||
			pValue.equalsIgnoreCase("FALSE") ||
			pValue.equalsIgnoreCase("F")
        	)
                result = false;

        return result;
    }
    
    
    public String ExtractIdentifier(Token pToken)
    {
    	
    	
    	//return ((Reduction)pToken.getData()).getParentRule().definition();
    	Object o = pToken.getData();
    	
    	String result = null;
    	
    	if(o instanceof Reduction)
    	{
    		if (((Reduction)pToken.getData()).size() == 1)
    			result = ((Reduction)pToken.getData()).get(0).getData().toString();
    		else
    			result = ((Reduction)pToken.getData()).get(0).getData().toString() + " " + ExtractIdentifier(((Reduction)pToken.getData()).get(1));
    	}
    	else
    	{
    		result =  pToken.getData().toString();
    	}
    	
    	
    	return result;
    }
    
    

    
	public static EnterRule BuildStatements(Rule_Context pContext, Reduction pToken)
	{
		
        EnterRule result = null;
        //if (pToken.getParent().containsOneNonTerminal() == true)
        //{
        	Rule_Enum Test = Rule_Enum.Convert(pToken.getParent().getHead().getName());
        	if(Test == null)
        	{
        		System.out.print("nuff said!");
        		
        	}
        	else
        	{
	            switch (Test)
	            {
	            
	            	case CheckCodeBlock:
	            		result = new Rule_CheckCodeBlock(pContext, pToken);
	                	break;
		            case CheckCodeBlocks:
		                result = new Rule_CheckCodeBlocks(pContext, pToken);
		                break;
	               case Program:
	                    result = new Rule_Program(pContext, pToken);
	                    break;
	                case Always_Statement:
	                    result  = new Rule_Always(pContext, pToken);
	                    break;
	
	                case Simple_Assign_Statement:
	                case Let_Statement:
	                case Assign_Statement:
	                    result  = new Rule_Assign(pContext, pToken);
	                    break;
	                case If_Statement:
	                case If_Else_Statement:
	                    result  = new Rule_If_Then_Else_End(pContext, pToken);
	                    break;
	                case Define_Variable_Statement:
	                    result  = new Rule_Define(pContext, pToken);
	                    break;
	                case DefineVariables_Statement:
	                    result  = new Rule_DefineVariables_Statement(pContext, pToken);
	                    break;
	                case Field_Checkcode_Statement:
	                    result  = new Rule_Field_Checkcode_Statement(pContext, pToken);
	                    break;
	                case View_Checkcode_Statement:
	                    result  = new Rule_View_Checkcode_Statement(pContext, pToken);
	                    break;
	                case Record_Checkcode_Statement:
	                    result  = new Rule_Record_Checkcode_Statement(pContext, pToken);
	                    break;
	                case Page_Checkcode_Statement:
	                    result  = new Rule_Page_Checkcode_Statement(pContext, pToken);
	                    break;
	                case Begin_Before_statement:
	                    result = new Rule_Begin_Before_Statement(pContext, pToken); 
	                    break;
	                case Begin_After_statement:
	                    result = new Rule_Begin_After_Statement(pContext, pToken);
	                    break;
	                case Begin_Click_statement:
	                    result = new Rule_Begin_Click_Statement(pContext, pToken);
	                    break;      
	                case Subroutine_Statement:
	                    result  = new Rule_Subroutine_Statement(pContext, pToken);
	                    break;
	                case Call_Statement:
	                    result  = new Rule_Call(pContext, pToken);
	                    break;
	                case Expr_List:
	                    result = new Rule_ExprList(pContext, pToken);
	                    break;
	                case Expression:
	                    result = new Rule_Expression(pContext, pToken);
	                    break;
	                case And_Exp:
	                    result = new Rule_AndExp(pContext, pToken);
	                    break;
	                case Not_Exp:
	                    result = new Rule_NotExp(pContext, pToken);
	                    break;
	                case Compare_Exp:
	                    result = new Rule_CompareExp(pContext, pToken);
	                    break;
	                case Concat_Exp:
	                    result = new Rule_ConcatExp(pContext, pToken);
	                    break;
	                case Add_Exp:
	                    result = new Rule_AddExp(pContext, pToken);
	                    break;
	                case Mult_Exp:
	                    result = new Rule_MultExp(pContext, pToken);
	                    break;
	                case Pow_Exp:
	                    result = new Rule_PowExp(pContext, pToken);
	                    break;
	                case Negate_Exp:
	                    result = new Rule_NegateExp(pContext, pToken);
	                    break;   
	                case Statements:
	                    result = new Rule_Statements(pContext, pToken);
	                    break;
	                case Statement:
	                    result = new Rule_Statement(pContext, pToken);
	                    break;                 
	                case Hide_Some_Statement:
	                case Hide_Except_Statement:
	                    result  = new Rule_Hide(pContext, pToken);
	                    break;
	                case Go_To_Variable_Statement:
	                case Go_To_Page_Statement:
	                    result  = new Rule_GoTo(pContext, pToken);
	                    break;
	
	                case Unhide_Some_Statement:
	                case Unhide_Except_Statement:
	                    result  = new Rule_UnHide(pContext, pToken);
	                    break;	                    
	                    /*  case Assign_DLL_Statement:
	                    result  = new Rule_Assign_DLL_Statement(pContext, pToken);
	                    break;
	                    
	                case Else_If_Statement:
	                    result = new Rule_Else_If_Statement(pContext, pToken);
	                    break;
	
	                case Define_Dll_Statement:
	                    result = new Rule_DLL_Statement(pContext, pToken);
	                    break;
	                case FuncName2:
	                case FunctionCall:
	                    result = new Rule_FunctionCall(pContext, pToken);
	                    break;
	

	
	                case Go_To_Variable_Statement:
	                case Go_To_Page_Statement:
	                    result  = new Rule_GoTo(pContext, pToken);
	                    break;
	*/
	                case Simple_Dialog_Statement:
	                	result = new Rule_Dialog(pContext, pToken);
	                	break;
	                	/*
	                case Numeric_Dialog_Implicit_Statement:
	                case Numeric_Dialog_Explicit_Statement:
	                case TextBox_Dialog_Statement:
	                case Db_Values_Dialog_Statement:
	                case YN_Dialog_Statement:
	                case Db_Views_Dialog_Statement:
	                case Databases_Dialog_Statement:
	                case Db_Variables_Dialog_Statement:
	                case Multiple_Choice_Dialog_Statement:
	                case Dialog_Read_Statement:
	                case Dialog_Write_Statement:
	                case Dialog_Read_Filter_Statement:
	                case Dialog_Write_Filter_Statement:
	                case Dialog_Date_Statement:
	                case Dialog_Date_Mask_Statement:
	                    result  = new Rule_Dialog(pContext, pToken);
	                    break;
	                case Comment_Line:
	                    result  = new Rule_CommentLine(pContext, pToken);
	                    break;
	             */ case Simple_Execute_Statement:
	                case Execute_File_Statement:
	                case Execute_Url_Statement:
	                case Execute_Wait_For_Exit_File_Statement:
	                case Execute_Wait_For_Exit_String_Statement:
	                case Execute_Wait_For_Exit_Url_Statement:
	                case Execute_No_Wait_For_Exit_File_Statement:
	                case Execute_No_Wait_For_Exit_String_Statement:
	                case Execute_No_Wait_For_Exit_Url_Statement:
	                    result  = new Rule_Execute(pContext, pToken);
	                    break;/*
	                case Beep_Statement:
	                    result  = new Rule_Beep(pContext, pToken);
	                    break;
	                case Auto_Search_Statement:
	                    result  = new Rule_AutoSearch(pContext, pToken);
	                    break;
	                case Quit_Statement:
	                    result  = new Rule_Quit(pContext);
	                    break;
	             */ case Clear_Statement:
	                    result  = new Rule_Clear(pContext, pToken);
	                    break; /*
	                case New_Record_Statement:
	                    result  = new Rule_NewRecord(pContext, pToken);
	                    break;
	                case Simple_Undefine_Statement:
	                    result  = new Rule_Undefine(pContext, pToken);
	                    break;
	               */ case Geocode_Statement:
	                    result  = new Rule_Geocode(pContext, pToken);
	                    break;
	/*
	                case Begin_Before_statement:
	                    result = new Rule_Begin_Before_Statement(pContext, pToken); 
	                    break;
	                case Begin_After_statement:
	                    result = new Rule_Begin_After_Statement(pContext, pToken);
	                    break;
	                case Begin_Click_statement:
	                    result = new Rule_Begin_Click_Statement(pContext, pToken);
	                    break;
	                case CheckCodeBlock:
	                    result = new Rule_CheckCodeBlock(pContext, pToken);
	                    break;
	                case CheckCodeBlocks:
	                    result = new Rule_CheckCodeBlocks(pContext, pToken);
	                    break;
	                case Simple_Run_Statement:
	                    break;*/
	
	                case Define_Statement_Group:
	                    result = new Rule_DefineVariables_Statement(pContext, pToken);
	                    break;
	                case Define_Statement_Type:
	                    result = new Rule_Define(pContext, pToken);
	                    break;
	                case Highlight_Statement:
	                    result = new Rule_Highlight(pContext, pToken);
	                    break;
	                case UnHighlight_Statement:
	                    result = new Rule_UnHighlight(pContext, pToken);
	                    break;
	                case Enable_Statement:
	                    result = new Rule_Enable(pContext, pToken);
	                    break;
	                case Disable_Statement:
	                    result = new Rule_Disable(pContext, pToken);
	                    break;
	                case FunctionCall:
	                	result = new Rule_FunctionCall(pContext, pToken);
	                	break;
	                case Value:
	                case Decimal_Number:
	                case Qualified_ID:
	                case Literal_String:
	                case Literal:
	                case Number:
	                case Literal_Date:
	                case Boolean:
	                case Identifier:
	                
	                case Real_Number:
	                case Hex_Number:
	                case RealLiteral:
	                case DecLiteral:
	                case HexLiteral:
	                case Date:
	                case Time:
	                case String: 
	                	result = new Rule_Value(pContext, pToken);
	                	break;
	                default:
	                    result = new Rule_Value(pContext, pToken);
	                	//result = null;
	                    break;
	                
	                    //result = new Rule_Value(pContext, pToken);
	                    //throw new Exception("Missing rule in EnterRule.BuildStatments " + NT.Symbol.ToString());
	                    
	            }
        	}


        /*}
        else // terminal token
        {
            //TerminalToken TT = (TerminalToken)pToken;

            switch (Rule_Enum.Convert(pToken.getParent().getHead().getName()))
            {
                case Value:
                default:
                    result = new Rule_Value(pContext, pToken);
                    break;
            }
        }*/

        return result;
	}
	
	public static String GetIdentifier(String pValue)
	{
		String result = pValue.replaceAll("\\]$", "").replaceAll("^\\[", "");
		return result;
	}
	
	
	
    static public List<EnterRule> GetFunctionParameters(Rule_Context pContext, Reduction pToken)
    {
        List<EnterRule> result = new ArrayList<EnterRule>();


        if (pToken.getParent().getHead().getType() == SymbolType.NON_TERMINAL)
        {

            //NonterminalToken NT = (NonterminalToken)pToken;

            //switch (NT.Symbol.ToString())
        	Rule_Enum Test = Rule_Enum.Convert(pToken.getParent().getHead().getName());
        	if(Test == null)
        	{
        		System.out.print("check code issue ");
        		
        	}
        	else
        	{
	            switch (Test)

	            {
	                case  NonEmptyFunctionParameterList:
	
	
	                    //this.paramList.Push(new Rule_NonEmptyFunctionParameterList(T, this.paramList));
	                    result.addAll(EnterRule.GetFunctionParameters(pContext, pToken));
	                    break;
	                case SingleFunctionParameterList:
	
	                    result.addAll(EnterRule.GetFunctionParameters(pContext, pToken));
	                    break;
	                case EmptyFunctionParameterList:
	                    //this.paramList = new Rule_EmptyFunctionParameterList(T);
	                    // do nothing the parameterlist is empty
	                    break;
	                case MultipleFunctionParameterList:
	
	                    //this.MultipleParameterList = new Rule_MultipleFunctionParameterList(pToken);
	                    //<NonEmptyFunctionParameterList> ',' <Expression>
	                    //result.Add(AnalysisRule.BuildStatments(pContext, NT.Tokens[0]));
	                    result.addAll(EnterRule.GetFunctionParameters(pContext, pToken.get(0).asReduction()));
	                    result.add(EnterRule.BuildStatements(pContext, pToken.get(2).asReduction()));
	                    break;
	                case FuncName2:
	                case Expression:
	                case FunctionCall:
	                default:
	                    result.add(EnterRule.BuildStatements(pContext, pToken));
	                    break;
	            }
        	}
        }
        else
        {
            //TerminalToken TT = (TerminalToken)pToken;
            if (pToken.get(0).getData().toString()!= ",")
            {
                result.add(new Rule_Value(pContext, pToken));
            }
        }


        /*
            <FunctionCall> ::= Identifier '(' <FunctionParameterList> ')'
               | FORMAT '(' <FunctionParameterList> ')'
                | <FuncName2>
            !           | <FuncName1> '(' <FunctionCall> ')'
            <FunctionParameterList> ::= <EmptyFunctionParameterList> | <NonEmptyFunctionParameterList>
            <NonEmptyFunctionParameterList> ::= <MultipleFunctionParameterList> | <SingleFunctionParameterList>

            <MultipleFunctionParameterList> ::= <NonEmptyFunctionParameterList> ',' <Expression>
            <SingleFunctionParameterList> ::= <expression>
            <EmptyFunctionParameterList> ::=
         */





        return result;
    }
	
	
	
	public enum Rule_Enum
    {
		Program(0),
		Always_Statement(1),
		Simple_Assign_Statement(2),
    Let_Statement(3),
    Assign_Statement(4),
    Assign_DLL_Statement(5),
    If_Statement(6),
    If_Else_Statement(7),
    Else_If_Statement(8),
    Define_Variable_Statement(9),
    Define_Dll_Statement(10),
    FuncName2(11),
    FunctionCall(12),
    Hide_Some_Statement(13),
    Hide_Except_Statement(14),
    Unhide_Some_Statement(15),
    Unhide_Except_Statement(16),
    Go_To_Variable_Statement(17),
    Go_To_Page_Statement(18),
    Simple_Dialog_Statement(19),
    Numeric_Dialog_Implicit_Statement(20),
    Numeric_Dialog_Explicit_Statement(21),
    TextBox_Dialog_Statement(22),
    Db_Values_Dialog_Statement(23),
    YN_Dialog_Statement(24),
    Db_Views_Dialog_Statement(25),
    Databases_Dialog_Statement(26),
    Db_Variables_Dialog_Statement(27),
    Multiple_Choice_Dialog_Statement(28),
    Dialog_Read_Statement(29),
    Dialog_Write_Statement(30),
    Dialog_Read_Filter_Statement(31),
    Dialog_Write_Filter_Statement(32),
    Dialog_Date_Statement(33),
    Dialog_Date_Mask_Statement(34),
    Comment_Line(35),
    Simple_Execute_Statement(36),
    Execute_File_Statement(37),
    Execute_Url_Statement(38),
    Execute_Wait_For_Exit_File_Statement(39),
    Execute_Wait_For_Exit_String_Statement(40),
    Execute_Wait_For_Exit_Url_Statement(41),
    Execute_No_Wait_For_Exit_File_Statement(42),
    Execute_No_Wait_For_Exit_String_Statement(43),
    Execute_No_Wait_For_Exit_Url_Statement(44),
    Beep_Statement(45),
    Auto_Search_Statement(46),
    Quit_Statement(47),
    Clear_Statement(48),
    New_Record_Statement(49),
    Simple_Undefine_Statement(50),
    Geocode_Statement(51),
    DefineVariables_Statement(52),
    Field_Checkcode_Statement(53),
    View_Checkcode_Statement(54),
    Record_Checkcode_Statement(55),
    Page_Checkcode_Statement(56),
    Subroutine_Statement(57),
    Call_Statement(58),
    Expr_List(59),
    Expression(60),
    And_Exp(61),
    Not_Exp(62),
    Compare_Exp(63),
    Concat_Exp(64),
    Add_Exp(65),
    Mult_Exp(66),
    Pow_Exp(67),
    Negate_Exp(68),
    Begin_Before_statement(69),
    Begin_After_statement(70),
    Begin_Click_statement(71),
    CheckCodeBlock(72),
    CheckCodeBlocks(73),
    Simple_Run_Statement(74),
    Statements(75),
    Statement(76),
    Define_Statement_Group(77),
    Define_Statement_Type(78),
    Highlight_Statement(79),
    UnHighlight_Statement(80),
    Enable_Statement(81),
    Disable_Statement(82),
    Value(83),
    Decimal_Number(84),
	Qualified_ID(85),
	Identifier(86),
	Literal_Date(87),
	Literal(88),
	Literal_String(89),
	Number(90),
	Real_Number(91),
	Hex_Number(92),
	Boolean(93),
	RealLiteral(94),
	DecLiteral(95),
	HexLiteral(96),
	Date(97),
	Time(98),
	String(99),
	Literal_Char(100),
    CharLiteral(101),
	   NonEmptyFunctionParameterList(102),
    SingleFunctionParameterList(103),
    EmptyFunctionParameterList(104),
    MultipleFunctionParameterList(105);

	
		
		private int value;  
		public int getValue() 
		{  
			return value;  
		}
		Rule_Enum() {}
	       
		Rule_Enum(int pValue)
		{  
			this.value = pValue;  
		}  
		
		static HashMap<String,Rule_Enum> StringEnum;
		
		static
		{
			
			StringEnum = new HashMap<String,Rule_Enum>(); 

	           StringEnum.put("Program".toLowerCase(), Program);
               StringEnum.put("Always_Statement".toLowerCase(),Always_Statement);
               StringEnum.put("Simple_Assign_Statement".toLowerCase(),Simple_Assign_Statement);
               StringEnum.put("Let_Statement".toLowerCase(),Let_Statement);
			   StringEnum.put("Assign_Statement".toLowerCase(),
			    Assign_Statement);
			   
			   StringEnum.put("Assign_DLL_Statement".toLowerCase(),
			    Assign_DLL_Statement);
			   
			   StringEnum.put("If_Statement".toLowerCase(),
			    If_Statement);
			   
			   StringEnum.put("If_Else_Statement".toLowerCase(),
			    If_Else_Statement);
			   
			   StringEnum.put("Else_If_Statement".toLowerCase(),
			    Else_If_Statement);
			   
			   StringEnum.put("Define_Variable_Statement".toLowerCase(),
			    Define_Variable_Statement);
			   
			   StringEnum.put("Define_Dll_Statement".toLowerCase(),
			    Define_Dll_Statement);
			   
			   StringEnum.put("FuncName2".toLowerCase(),
			    FuncName2);
			   
			   StringEnum.put("FunctionCall".toLowerCase(),
			    FunctionCall);
			   
			   StringEnum.put("Hide_Some_Statement".toLowerCase(),
			    Hide_Some_Statement);
			   
			   StringEnum.put("Hide_Except_Statement".toLowerCase(),
			    Hide_Except_Statement);
			   
			   StringEnum.put("Unhide_Some_Statement".toLowerCase(),
			    Unhide_Some_Statement);
			   
			   StringEnum.put("Unhide_Except_Statement".toLowerCase(),
			    Unhide_Except_Statement);
			   
			   StringEnum.put("Go_To_Variable_Statement".toLowerCase(),
			    Go_To_Variable_Statement);
			   
			   StringEnum.put("Go_To_Page_Statement".toLowerCase(),
			    Go_To_Page_Statement);
			   
			   StringEnum.put("Simple_Dialog_Statement".toLowerCase(),
			    Simple_Dialog_Statement);
			   
			   StringEnum.put("Numeric_Dialog_Implicit_Statement".toLowerCase(),
			    Numeric_Dialog_Implicit_Statement);
			   
			   StringEnum.put("Numeric_Dialog_Explicit_Statement".toLowerCase(),
			    Numeric_Dialog_Explicit_Statement);
			   
			   StringEnum.put("TextBox_Dialog_Statement".toLowerCase(),
			    TextBox_Dialog_Statement);
			   
			   StringEnum.put("Db_Values_Dialog_Statement".toLowerCase(),
			    Db_Values_Dialog_Statement);
			   
			   StringEnum.put("YN_Dialog_Statement".toLowerCase(),
			    YN_Dialog_Statement);
			   
			   StringEnum.put("Db_Views_Dialog_Statement".toLowerCase(),
			    Db_Views_Dialog_Statement);
			   
			   StringEnum.put("Databases_Dialog_Statement".toLowerCase(),
			    Databases_Dialog_Statement);
			   
			   StringEnum.put("Db_Variables_Dialog_Statement".toLowerCase(),
			    Db_Variables_Dialog_Statement);
			   
			   StringEnum.put("Multiple_Choice_Dialog_Statement".toLowerCase(),
			    Multiple_Choice_Dialog_Statement);
			   
			   StringEnum.put("Dialog_Read_Statement".toLowerCase(),
			    Dialog_Read_Statement);
			   
			   StringEnum.put("Dialog_Write_Statement".toLowerCase(),
			    Dialog_Write_Statement);
			   
			   StringEnum.put("Dialog_Read_Filter_Statement".toLowerCase(),
			    Dialog_Read_Filter_Statement);
			   
			   StringEnum.put("Dialog_Write_Filter_Statement".toLowerCase(),
			    Dialog_Write_Filter_Statement);
			   
			   StringEnum.put("Dialog_Date_Statement".toLowerCase(),
			    Dialog_Date_Statement);
			   
			   StringEnum.put("Dialog_Date_Mask_Statement".toLowerCase(),
			    Dialog_Date_Mask_Statement);
			   
			   StringEnum.put("Comment_Line".toLowerCase(),
			    Comment_Line);
			   
			   StringEnum.put("Simple_Execute_Statement".toLowerCase(),
			    Simple_Execute_Statement);
			   
			   StringEnum.put("Execute_File_Statement".toLowerCase(),
			    Execute_File_Statement);
			   
			   StringEnum.put("Execute_Url_Statement".toLowerCase(),
			    Execute_Url_Statement);
			   
			   StringEnum.put("Execute_Wait_For_Exit_File_Statement".toLowerCase(),
			    Execute_Wait_For_Exit_File_Statement);
			   
			   StringEnum.put("Execute_Wait_For_Exit_String_Statement".toLowerCase(),
			    Execute_Wait_For_Exit_String_Statement);
			   
			   StringEnum.put("Execute_Wait_For_Exit_Url_Statement".toLowerCase(),
			    Execute_Wait_For_Exit_Url_Statement);
			   
			   StringEnum.put("Execute_No_Wait_For_Exit_File_Statement".toLowerCase(),
			    Execute_No_Wait_For_Exit_File_Statement);
			   
			   StringEnum.put("Execute_No_Wait_For_Exit_String_Statement".toLowerCase(),
			    Execute_No_Wait_For_Exit_String_Statement);
			   
			   StringEnum.put("Execute_No_Wait_For_Exit_Url_Statement".toLowerCase(),
			    Execute_No_Wait_For_Exit_Url_Statement);
			   
			   StringEnum.put("Beep_Statement".toLowerCase(),
			    Beep_Statement);
			   
			   StringEnum.put("Auto_Search_Statement".toLowerCase(),
			    Auto_Search_Statement);
			   
			   StringEnum.put("Quit_Statement".toLowerCase(),
			    Quit_Statement);
			   
			   StringEnum.put("Clear_Statement".toLowerCase(),
			    Clear_Statement);
			   
			   StringEnum.put("New_Record_Statement".toLowerCase(),
			    New_Record_Statement);
			   
			   StringEnum.put("Simple_Undefine_Statement".toLowerCase(),
			    Simple_Undefine_Statement);
			   
			   StringEnum.put("Geocode_Statement".toLowerCase(),
			    Geocode_Statement);
			   
			   StringEnum.put("DefineVariables_Statement".toLowerCase(),
			    DefineVariables_Statement);
			   
			   StringEnum.put("Field_Checkcode_Statement".toLowerCase(),
			    Field_Checkcode_Statement);
			   
			   StringEnum.put("View_Checkcode_Statement".toLowerCase(),
			    View_Checkcode_Statement);
			   
			   StringEnum.put("Record_Checkcode_Statement".toLowerCase(),
			    Record_Checkcode_Statement);
			   
			   StringEnum.put("Page_Checkcode_Statement".toLowerCase(),
			    Page_Checkcode_Statement);
			   
			   StringEnum.put("Subroutine_Statement".toLowerCase(),
			    Subroutine_Statement);
			   
			   StringEnum.put("Call_Statement".toLowerCase(),
			    Call_Statement);
			   
			   StringEnum.put("Expr_List".toLowerCase(),
			    Expr_List);
			   
			   StringEnum.put("Expression".toLowerCase(),
			    Expression);
			   
			   StringEnum.put("And Exp".toLowerCase(),
			    And_Exp);
			   
			   StringEnum.put("Not Exp".toLowerCase(),
			    Not_Exp);
			   
			   StringEnum.put("Compare Exp".toLowerCase(),
			    Compare_Exp);
			   
			   StringEnum.put("Concat Exp".toLowerCase(),
			    Concat_Exp);
			   
			   StringEnum.put("Add Exp".toLowerCase(),
			    Add_Exp);
			   
			   StringEnum.put("Mult Exp".toLowerCase(),
			    Mult_Exp);
			   
			   StringEnum.put("Pow Exp".toLowerCase(),
			    Pow_Exp);
			   
			   StringEnum.put("Negate Exp".toLowerCase(),
			    Negate_Exp);
			   
			   StringEnum.put("Begin_Before_statement".toLowerCase(),
			    Begin_Before_statement);
			   
			   StringEnum.put("Begin_After_statement".toLowerCase(),
			    Begin_After_statement);
			   
			   StringEnum.put("Begin_Click_statement".toLowerCase(),
			    Begin_Click_statement);
			   
			   StringEnum.put("CheckCodeBlock".toLowerCase(),
			    CheckCodeBlock);
			   
			   StringEnum.put("CheckCodeBlocks".toLowerCase(),
			    CheckCodeBlocks);
			   
			   StringEnum.put("Simple_Run_Statement".toLowerCase(),
			    Simple_Run_Statement);
			   
			   StringEnum.put("Statements".toLowerCase(),
			    Statements);
			   
			   StringEnum.put("Statement".toLowerCase(),
			    Statement);
			   
			   StringEnum.put("Define_Statement_Group".toLowerCase(),
			    Define_Statement_Group);
			   
			   StringEnum.put("Define_Statement_Type".toLowerCase(),
			    Define_Statement_Type);
			   
			   StringEnum.put("Highlight_Statement".toLowerCase(),
			    Highlight_Statement);
			   
			   StringEnum.put("UnHighlight_Statement".toLowerCase(),
			    UnHighlight_Statement);
			   
			   StringEnum.put("Enable_Statement".toLowerCase(),
			    Enable_Statement);
			   
			   StringEnum.put("Disable_Statement".toLowerCase(),
			    Disable_Statement);
			   
			   StringEnum.put("Value".toLowerCase(),
			    Value);
			   
			   StringEnum.put("Decimal_Number".toLowerCase(),
					    Decimal_Number);
			   
			   
			   
			   StringEnum.put("Qualified_ID".toLowerCase(),  Qualified_ID);
			   
			   StringEnum.put("Qualified ID".toLowerCase(), Qualified_ID);
				   
			   StringEnum.put("Identifier".toLowerCase(),  Identifier);
				   
			   StringEnum.put("FunctionCall".toLowerCase(),  FunctionCall);
				   
			   StringEnum.put("Literal_Date".toLowerCase(),  Literal_Date);
				   
			   StringEnum.put("Literal".toLowerCase(),  Literal);
				   
			   StringEnum.put("Literal_String".toLowerCase(),  Literal_String);
				   
			   StringEnum.put("Number".toLowerCase(),  Number);
				   
			   StringEnum.put("Real_Number".toLowerCase(),  Real_Number);
				   
			   StringEnum.put("Decimal_Number".toLowerCase(),  Decimal_Number);
				   
			   StringEnum.put("Hex_Number".toLowerCase(),  Hex_Number);
				   
			   StringEnum.put("Boolean".toLowerCase(),  Boolean);
				   
			   StringEnum.put("RealLiteral".toLowerCase(),  RealLiteral);
				   
			   StringEnum.put("DecLiteral".toLowerCase(),  DecLiteral);
				   
			   StringEnum.put("HexLiteral".toLowerCase(), HexLiteral);
				   
			   StringEnum.put("Date".toLowerCase(), Date);
				   
			   StringEnum.put("Time".toLowerCase(), Time);
				   
			   StringEnum.put("String".toLowerCase(), String);
				   
			   
			   StringEnum.put("Literal_Char".toLowerCase(), Literal_Char);
				   
			   StringEnum.put("CharLiteral".toLowerCase(),CharLiteral);

			   StringEnum.put("NonEmptyFunctionParameterList".toLowerCase(),NonEmptyFunctionParameterList);
			   StringEnum.put("SingleFunctionParameterList".toLowerCase(),SingleFunctionParameterList);
			   StringEnum.put("EmptyFunctionParameterList".toLowerCase(),EmptyFunctionParameterList);
			   StringEnum.put("MultipleFunctionParameterList".toLowerCase(),MultipleFunctionParameterList);

			   
			
		}
		
       static Rule_Enum Convert(String pValue) 
       {  
    	   Rule_Enum result = null;
    	   
    	   
    	   
    	   
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
    	   
    	   /*
           if(value.equalsIgnoreCase("Program"))
    	   return Program;

           if(value.equalsIgnoreCase("Always_Statement"))
    	   return Always_Statement;
    	   
           if(value.equalsIgnoreCase("Simple_Assign_Statement"))
    	   return Simple_Assign_Statement;
    	   
           if(value.equalsIgnoreCase("Let_Statement"))
    	   return Let_Statement;
    	   
           if(value.equalsIgnoreCase("Assign_Statement"))
    	   return Assign_Statement;
           
           if(value.equalsIgnoreCase("Assign_DLL_Statement"))
    	   return Assign_DLL_Statement;
    	   
           if(value.equalsIgnoreCase("If_Statement"))
    	   return If_Statement;
    	   
           if(value.equalsIgnoreCase("If_Else_Statement"))
    	   return If_Else_Statement;
    	   
           if(value.equalsIgnoreCase("Else_If_Statement"))
    	   return Else_If_Statement;
    	   
           if(value.equalsIgnoreCase("Define_Variable_Statement"))
    	   return Define_Variable_Statement;
    	   
           if(value.equalsIgnoreCase("Define_Dll_Statement"))
    	   return Define_Dll_Statement;
    	   
           if(value.equalsIgnoreCase("FuncName2"))
    	   return FuncName2;
    	   
           if(value.equalsIgnoreCase("FunctionCall"))
    	   return FunctionCall;
    	   
           if(value.equalsIgnoreCase("Hide_Some_Statement"))
    	   return Hide_Some_Statement;
    	   
           if(value.equalsIgnoreCase("Hide_Except_Statement"))
    	   return Hide_Except_Statement;
    	   
           if(value.equalsIgnoreCase("Unhide_Some_Statement"))
    	   return Unhide_Some_Statement;
    	   
           if(value.equalsIgnoreCase("Unhide_Except_Statement"))
    	   return Unhide_Except_Statement;
    	   
           if(value.equalsIgnoreCase("Go_To_Variable_Statement"))
    	   return Go_To_Variable_Statement;
    	   
           if(value.equalsIgnoreCase("Go_To_Page_Statement"))
    	   return Go_To_Page_Statement;
    	   
           if(value.equalsIgnoreCase("Simple_Dialog_Statement"))
    	   return Simple_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Numeric_Dialog_Implicit_Statement"))
    	   return Numeric_Dialog_Implicit_Statement;
    	   
           if(value.equalsIgnoreCase("Numeric_Dialog_Explicit_Statement"))
    	   return Numeric_Dialog_Explicit_Statement;
    	   
           if(value.equalsIgnoreCase("TextBox_Dialog_Statement"))
    	   return TextBox_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Db_Values_Dialog_Statement"))
    	   return Db_Values_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("YN_Dialog_Statement"))
    	   return YN_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Db_Views_Dialog_Statement"))
    	   return Db_Views_Dialog_Statement;
           
           if(value.equalsIgnoreCase("Databases_Dialog_Statement"))
    	   return Databases_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Db_Variables_Dialog_Statement"))
    	   return Db_Variables_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Multiple_Choice_Dialog_Statement"))
    	   return Multiple_Choice_Dialog_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Read_Statement"))
    	   return Dialog_Read_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Write_Statement"))
    	   return Dialog_Write_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Read_Filter_Statement"))
    	   return Dialog_Read_Filter_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Write_Filter_Statement"))
    	   return Dialog_Write_Filter_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Date_Statement"))
    	   return Dialog_Date_Statement;
    	   
           if(value.equalsIgnoreCase("Dialog_Date_Mask_Statement"))
    	   return Dialog_Date_Mask_Statement;
    	   
           if(value.equalsIgnoreCase("Comment_Line"))
    	   return Comment_Line;
    	   
           if(value.equalsIgnoreCase("Simple_Execute_Statement"))
    	   return Simple_Execute_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_File_Statement"))
    	   return Execute_File_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_Url_Statement"))
    	   return Execute_Url_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_Wait_For_Exit_File_Statement"))
    	   return Execute_Wait_For_Exit_File_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_Wait_For_Exit_String_Statement"))
    	   return Execute_Wait_For_Exit_String_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_Wait_For_Exit_Url_Statement"))
    	   return Execute_Wait_For_Exit_Url_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_No_Wait_For_Exit_File_Statement"))
    	   return Execute_No_Wait_For_Exit_File_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_No_Wait_For_Exit_String_Statement"))
    	   return Execute_No_Wait_For_Exit_String_Statement;
    	   
           if(value.equalsIgnoreCase("Execute_No_Wait_For_Exit_Url_Statement"))
    	   return Execute_No_Wait_For_Exit_Url_Statement;
    	   
           if(value.equalsIgnoreCase("Beep_Statement"))
    	   return Beep_Statement;
    	   
           if(value.equalsIgnoreCase("Auto_Search_Statement"))
    	   return Auto_Search_Statement;
    	   
           if(value.equalsIgnoreCase("Quit_Statement"))
    	   return Quit_Statement;
    	   
           if(value.equalsIgnoreCase("Clear_Statement"))
    	   return Clear_Statement;
    	   
           if(value.equalsIgnoreCase("New_Record_Statement"))
    	   return New_Record_Statement;
    	   
           if(value.equalsIgnoreCase("Simple_Undefine_Statement"))
    	   return Simple_Undefine_Statement;
    	   
           if(value.equalsIgnoreCase("Geocode_Statement"))
    	   return Geocode_Statement;
    	   
           if(value.equalsIgnoreCase("DefineVariables_Statement"))
    	   return DefineVariables_Statement;
    	   
           if(value.equalsIgnoreCase("Field_Checkcode_Statement"))
    	   return Field_Checkcode_Statement;
    	   
           if(value.equalsIgnoreCase("View_Checkcode_Statement"))
    	   return View_Checkcode_Statement;
    	   
           if(value.equalsIgnoreCase("Record_Checkcode_Statement"))
    	   return Record_Checkcode_Statement;
    	   
           if(value.equalsIgnoreCase("Page_Checkcode_Statement"))
    	   return Page_Checkcode_Statement;
    	   
           if(value.equalsIgnoreCase("Subroutine_Statement"))
    	   return Subroutine_Statement;
    	   
           if(value.equalsIgnoreCase("Call_Statement"))
    	   return Call_Statement;
    	   
           if(value.equalsIgnoreCase("Expr_List"))
    	   return Expr_List;
    	   
           if(value.equalsIgnoreCase("Expression"))
    	   return Expression;
    	   
           if(value.equalsIgnoreCase("And Exp"))
    	   return And_Exp;
    	   
           if(value.equalsIgnoreCase("Not Exp"))
    	   return Not_Exp;
    	   
           if(value.equalsIgnoreCase("Compare Exp"))
    	   return Compare_Exp;
    	   
           if(value.equalsIgnoreCase("Concat Exp"))
    	   return Concat_Exp;
    	   
           if(value.equalsIgnoreCase("Add Exp"))
    	   return Add_Exp;
    	   
           if(value.equalsIgnoreCase("Mult Exp"))
    	   return Mult_Exp;
    	   
           if(value.equalsIgnoreCase("Pow Exp"))
    	   return Pow_Exp;
    	   
           if(value.equalsIgnoreCase("Negate Exp"))
    	   return Negate_Exp;
    	   
           if(value.equalsIgnoreCase("Begin_Before_statement"))
    	   return Begin_Before_statement;
    	   
           if(value.equalsIgnoreCase("Begin_After_statement"))
    	   return Begin_After_statement;
    	   
           if(value.equalsIgnoreCase("Begin_Click_statement"))
    	   return Begin_Click_statement;
    	   
           if(value.equalsIgnoreCase("CheckCodeBlock"))
    	   return CheckCodeBlock;
    	   
           if(value.equalsIgnoreCase("CheckCodeBlocks"))
    	   return CheckCodeBlocks;
    	   
           if(value.equalsIgnoreCase("Simple_Run_Statement"))
    	   return Simple_Run_Statement;
    	   
           if(value.equalsIgnoreCase("Statements"))
    	   return Statements;
    	   
           if(value.equalsIgnoreCase("Statement"))
    	   return Statement;
    	   
           if(value.equalsIgnoreCase("Define_Statement_Group"))
    	   return Define_Statement_Group;
    	   
           if(value.equalsIgnoreCase("Define_Statement_Type"))
    	   return Define_Statement_Type;
    	   
           if(value.equalsIgnoreCase("Highlight_Statement"))
    	   return Highlight_Statement;
    	   
           if(value.equalsIgnoreCase("UnHighlight_Statement"))
    	   return UnHighlight_Statement;
    	   
           if(value.equalsIgnoreCase("Enable_Statement"))
    	   return Enable_Statement;
    	   
           if(value.equalsIgnoreCase("Disable_Statement"))
    	   return Disable_Statement;
    	   
           if(value.equalsIgnoreCase("Value"))
    	   return Value;
           
           if(value.equalsIgnoreCase("Decimal_Number"))
        		   return Decimal_Number;
           
           
           
		   if(value.equalsIgnoreCase("Qualified_ID"))
			   return Qualified_ID;
		   
		   if(value.equalsIgnoreCase("Qualified ID"))
			   return Qualified_ID;
			   
		   if(value.equalsIgnoreCase("Identifier"))
			   return Identifier;
			   
		   if(value.equalsIgnoreCase("FunctionCall"))
			   return FunctionCall;
			   
		   if(value.equalsIgnoreCase("Literal_Date"))
			   return Literal_Date;
			   
		   if(value.equalsIgnoreCase("Literal"))
			   return Literal;
			   
		   if(value.equalsIgnoreCase("Literal_String"))
			   return Literal_String;
			   
		   if(value.equalsIgnoreCase("Number"))
			   return Number;
			   
		   if(value.equalsIgnoreCase("Real_Number"))
			   return Real_Number;
			   
		   if(value.equalsIgnoreCase("Decimal_Number"))
			   return Decimal_Number;
			   
		   if(value.equalsIgnoreCase("Hex_Number"))
			   return Hex_Number;
			   
		   if(value.equalsIgnoreCase("Boolean"))
			   return Boolean;
			   
		   if(value.equalsIgnoreCase("RealLiteral"))
			   return RealLiteral;
			   
		   if(value.equalsIgnoreCase("DecLiteral"))
			   return DecLiteral;
			   
		   if(value.equalsIgnoreCase("HexLiteral"))
			   return HexLiteral;
			   
		   if(value.equalsIgnoreCase("Date"))
			   return Date;
			   
		   if(value.equalsIgnoreCase("Time"))
			   return Time;
			   
		   if(value.equalsIgnoreCase("String"))
			   return String;
			   
		   
		   if(value.equalsIgnoreCase("Literal_Char"))
			   return Literal_Char;
			   
		   if(value.equalsIgnoreCase("CharLiteral"))
			   return CharLiteral;
		   
           return result;*/
       }  
       
      
       
         
    
    }
	
	
	
}
