package gov.cdc.epiinfo.interpreter;

import java.util.Hashtable;


public class VariableCollection {
	
	private static Hashtable<String, Variable> variables;
	
	static
	{
		variables = new Hashtable<String, Variable>();
	}
	
	public static void Initialize(String defineStatements)
	{
		
		defineStatements = defineStatements.substring(defineStatements.indexOf("DEFINE"));
		String[] splits = defineStatements.split("DEFINE");
		for (int x=0; x<splits.length; x++)
		{
			String[] secondLevel = splits[x].trim().split(" ");
			if (secondLevel.length > 1)
			{
				String varName = secondLevel[0];
				String varType = secondLevel[secondLevel.length - 1];
				
				variables.put(varName.toLowerCase(), new Variable(varName,varType,""));
			}
		}
	}
	
	public static boolean VariableExists(String varName)
	{
		return variables.containsKey(varName.toLowerCase());
		
	}
	
	public static void Assign(String varName, String value)
	{
		if (variables.containsKey(varName.toLowerCase()))
		{
			variables.get(varName.toLowerCase()).setValue(value);
		}
		else
		{
			variables.put(varName.toLowerCase(), new Variable(varName,"unknown",value));
		}
	}
	
	public static String GetValue(String varName)
	{
		if (variables.containsKey(varName.toLowerCase()))
		{
			return variables.get(varName.toLowerCase()).getValue();
		}

		return "";
	}
	
	public static String GetVariableType(String varName)
	{
		if (variables.containsKey(varName.toLowerCase()))
		{
			return variables.get(varName.toLowerCase()).getVarType();
		}
		return "";
	}

}
