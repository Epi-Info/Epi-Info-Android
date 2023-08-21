package gov.cdc.epiinfo.interpreter;

import java.util.HashMap;

public class Rule_Context 
{
	public ICheckCodeHost CheckCodeInterface;
	
    public Rule_DefineVariables_Statement DefineVariablesCheckcode;
    public Rule_View_Checkcode_Statement View_Checkcode;
    public Rule_Record_Checkcode_Statement Record_Checkcode;
	
    public HashMap<String, EnterRule> Page_Checkcode;
    public HashMap<String, EnterRule> Field_Checkcode;

    
    public HashMap<String, EnterRule> BeforeCheckCode;
    public HashMap<String, EnterRule> AfterCheckCode;
    public HashMap<String, EnterRule> PageBeforeCheckCode;
    public HashMap<String, EnterRule> PageAfterCheckCode;
    public HashMap<String, EnterRule> FieldBeforeCheckCode;
    public HashMap<String, EnterRule> FieldAfterCheckCode;
    public HashMap<String, EnterRule> FieldClickCheckCode;
    public HashMap<String, EnterRule> Subroutine;
    
    public HashMap<String, String> AssignVariableCheck;
    
    private CSymbolTable currentScope;
    public CSymbolTable GetCurrentScope() 
    { 
    	return this.currentScope; 
    }
    
    
    public Rule_Context()
    {
    	this.DefineVariablesCheckcode = null;
    	this.View_Checkcode = null;
        this.Record_Checkcode = null;
    	
        this.Page_Checkcode = new HashMap<String, EnterRule>();
        this.Field_Checkcode = new HashMap<String, EnterRule>();

        
        this.BeforeCheckCode = new HashMap<String, EnterRule>();
        this.AfterCheckCode = new HashMap<String, EnterRule>();
        this.PageBeforeCheckCode = new HashMap<String, EnterRule>();
        this.PageAfterCheckCode = new HashMap<String, EnterRule>();
        this.FieldBeforeCheckCode = new HashMap<String, EnterRule>();
        this.FieldAfterCheckCode = new HashMap<String, EnterRule>();
        this.FieldClickCheckCode = new HashMap<String, EnterRule>();
        this.Subroutine = new HashMap<String, EnterRule>();
        
        this.AssignVariableCheck = new HashMap<String, String>();
        
        this.currentScope = new CSymbolTable();
    	
    }
    
    
    
    public EnterRule GetCommand(String pSearchText)
    {
    	EnterRule result = null;
        String Level = null;
        String Event = null;
        String Identifier = null;

        //String SearchRule = null;

        String[] Parameters = parseGetCommandSearchText(pSearchText);
        Level = Parameters[0].toLowerCase();
        Event = Parameters[1].toLowerCase();
        Identifier = Parameters[2].toLowerCase();

        switch (ConvertLevelToEnum(Level))
        {
            case view:
            case form:
                if (Event.equalsIgnoreCase(""))
                {
                    result = this.View_Checkcode;
                }
                else
                if (Event.equalsIgnoreCase("before"))
                {
                    if (this.BeforeCheckCode.containsKey("view"))
                    {
                        result = this.BeforeCheckCode.get("view");
                    }
                }
                else if (Event.equalsIgnoreCase("after"))
                {
                    if (this.AfterCheckCode.containsKey("view"))
                    {
                        result = this.AfterCheckCode.get("view");
                    }
                }
                break;
            case record:
                if (Event.equalsIgnoreCase(""))
                {
                    result = this.Record_Checkcode;
                }
                else
                if (Event.equalsIgnoreCase("before"))
                {
                    if (this.BeforeCheckCode.containsKey("record"))
                    {
                        result = this.BeforeCheckCode.get("record");
                    }
                }
                else if (Event.equalsIgnoreCase("after"))
                {
                    if (this.AfterCheckCode.containsKey("record"))
                    {
                        result = this.AfterCheckCode.get("record");
                    }
                }
                break;
            case page:
                if (Event.equalsIgnoreCase(""))
                {
                    if (this.Page_Checkcode.containsKey(Identifier))
                    {
                        result = this.Page_Checkcode.get(Identifier);
                    }
                }
                else
                if (Event.equalsIgnoreCase("before"))
                {
                    if (this.PageBeforeCheckCode.containsKey(Identifier))
                    {
                        result = this.PageBeforeCheckCode.get(Identifier);
                    }
                }
                else if (Event.equalsIgnoreCase("after"))
                {
                    if (this.PageAfterCheckCode.containsKey(Identifier))
                    {
                        result = this.PageAfterCheckCode.get(Identifier);
                    }
                }
                break;
            case field:
                if (Event.equalsIgnoreCase(""))
                {
                    if (this.Field_Checkcode.containsKey(Identifier))
                    {
                        result = this.Field_Checkcode.get(Identifier);
                    }
                }
                else
                if (Event.equalsIgnoreCase("before"))
                {
                    if (this.FieldBeforeCheckCode.containsKey(Identifier))
                    {
                        result = this.FieldBeforeCheckCode.get(Identifier);
                    }
                }
                else if (Event.equalsIgnoreCase("after"))
                {
                    if (this.FieldAfterCheckCode.containsKey(Identifier))
                    {
                        result = this.FieldAfterCheckCode.get(Identifier);
                    }
                }
                else if (Event.equalsIgnoreCase("click"))
                {
                    if (this.FieldClickCheckCode.containsKey(Identifier))
                    {
                        result = this.FieldClickCheckCode.get(Identifier);
                    }
                }
                break;
            case sub:
                if (this.Subroutine.containsKey(Event))
                {
                    result = this.Subroutine.get(Event);
                }
                break;
            case definevariables:
                result = this.DefineVariablesCheckcode;
                break;
		default:
			break;
        }

        return result;
    }

    private LevelEnum ConvertLevelToEnum(String pValue)
    {
		if(pValue.equalsIgnoreCase("view"))
			return LevelEnum.view;
		if(pValue.equalsIgnoreCase("form"))
			return LevelEnum.form;
		if(pValue.equalsIgnoreCase("record"))
			return LevelEnum.record;
		if(pValue.equalsIgnoreCase("page"))
			return LevelEnum.page;
		if(pValue.equalsIgnoreCase("field"))
			return LevelEnum.field;
		if(pValue.equalsIgnoreCase("sub"))
			return LevelEnum.sub;
		if(pValue.equalsIgnoreCase("definevariables"))
			return LevelEnum.definevariables;
		    						
		return LevelEnum.none;
    }
    
    public enum LevelEnum
    {
		none(0), view(1),form(2),record(3),page(4),field(5),sub(6),definevariables(7);
		
		final int value;
		LevelEnum(int pValue)
		{  
			this.value = pValue;  
		}  
	      
		public int getValue() 
		{  
			return value;  
		} 
		
		LevelEnum(String pValue)
		{
			String Level = pValue.toLowerCase();
			if(Level == "view")
			{
				this.value = 1;
			}
			else if(Level == "form")
			{
				this.value = 2;
			} else if(Level == "record")
			{
				this.value = 3;
			}else if(Level == "page")
			{
				this.value = 4;
			} else if(Level == "field")
			{
				this.value = 5;
			} else if(Level == "sub")
			{
				this.value = 6;
			} 
			else if(Level == "definevariables")
			{
				this.value = 7;
			}
			else
			{
				this.value = 0;
			}
		
		}
		

    
    }
    

    private String[] parseGetCommandSearchText(String pSearchText)
    {
        String[] result = null;

        String[] temp = pSearchText.split("&");
        result = new String[temp.length];
        for (int i = 0; i < temp.length; i++)
        {
        	String[] temp2 = temp[i].split("="); 
        	if(temp2.length > 1)
        	{
        		result[i] = temp2[1];
        	}
        	else
        	{
        		result[i] = "";
        	}
        }

        return result;
    }
    
}
