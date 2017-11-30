package gov.cdc.epiinfo.interpreter;

public class Variable {
	
	private String name;
	private String variableType;
	private String variableScope;
	private String value;
	
	public Variable(String name, String variableType, String variableScope)
	{
		this.name = name;
		this.variableType = variableType;
		this.variableScope = variableScope;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getVarType()
	{
		return variableType;
	}
	
	public String getVarScope()
	{
		return variableScope;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return value;
	}

}
