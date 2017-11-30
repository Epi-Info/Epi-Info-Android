package gov.cdc.epiinfo.interpreter;

public enum Operator_Enum
{
	e(0),
	ne(1),
	lt(2),
	gt(3),
	gte(4),
	lte(5),
	add(6),
	sub(7),
	mul(8),
	div(9),
	mod(10);
	
	private int value;  
	public int getValue() 
	{  
		return value;  
	}
	Operator_Enum() {}
       
	Operator_Enum(int pValue)
	{  
		this.value = pValue;  
	} 
	
   static Operator_Enum Convert(String pValue) 
   {  
	   
	   if(pValue.equalsIgnoreCase("="))
		   return e;
	   
	   if(pValue.equalsIgnoreCase("<>"))
			return ne;
	   if(pValue.equalsIgnoreCase("<"))
			return lt;
	   if(pValue.equalsIgnoreCase(">"))
			return gt;
	   if(pValue.equalsIgnoreCase(">="))
			return gte;
		if(pValue.equalsIgnoreCase("<="))
			return lte;  
		
		if(pValue.equalsIgnoreCase("+"))
			return add;  
		
		if(pValue.equalsIgnoreCase("-"))
			return sub;  
		
		if(pValue.equalsIgnoreCase("*"))
			return mul;  
		if(pValue.equalsIgnoreCase("/"))
			return div;  
		if(pValue.equalsIgnoreCase("%"))
			return mod;  
		
		return null;
   }
}