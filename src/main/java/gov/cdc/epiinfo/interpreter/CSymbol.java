package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.interpreter.EnterRule.Rule_Enum;

public class CSymbol 
{
    public enum DataType
    {

    	Unknown(9),
        Object(0),
        Number(1),
        Text(2),
        Date(3),
        Time(4),
        DateTime(5),
        Boolean(6),
        PhoneNumber(7),
        YesNo(8),
        GUID(10),
        Class(11),
        Function(12);
        
        private int code;  
   	   
       DataType(int code)
       {  
           this.code = code;  
       }  
      
       public int getCode() {  
           return code;  
       }  
       
       static DataType convert(String pValue) 
       {  
           if(pValue.equalsIgnoreCase("Unknown"))
           	return Unknown;

           if(pValue.equalsIgnoreCase("Object"))
           return Object;
			
           if(pValue.equalsIgnoreCase("Number"))
           return Number;
			
           if(pValue.equalsIgnoreCase("Text"))
           return Text;
			
           if(pValue.equalsIgnoreCase("Date"))
           return Date;
			
           if(pValue.equalsIgnoreCase("Time"))
           return Time;
			
           if(pValue.equalsIgnoreCase("DateTime"))
           return DateTime;
			
           if(pValue.equalsIgnoreCase("Boolean"))
           return Boolean;
			
           if(pValue.equalsIgnoreCase("PhoneNumber"))
           return PhoneNumber;
			
           if(pValue.equalsIgnoreCase("YesNo"))
           return YesNo;
           
           if(pValue.equalsIgnoreCase("GUID"))
           return GUID;
			
           if(pValue.equalsIgnoreCase("Class"))
           return Class;
			
           if(pValue.equalsIgnoreCase("Function"))
           return Function;
           
           return Unknown;
       }
    }

    
    public enum VariableScope
    {
        Undefined(0),
        Permanent(1),
        Global(2),
        Standard(4),
        System(8),
        DataSource(16),
        DataSourceRedefined(32);
        
       private int code;  
    	   
       VariableScope(int code)
       {  
           this.code = code;  
       }  
      
       public int getCode() {  
           return code;  
       }  
    }
    
    	 
    public String Name;
    public DataType Type;
    public VariableScope VariableScope;
    public Object Value;
    public EnterRule Rule;

    public CSymbol(String pName, DataType pType)
    {
        this.Name = pName;
        this.Type = pType;
    }

	public CSymbol() {}

}
