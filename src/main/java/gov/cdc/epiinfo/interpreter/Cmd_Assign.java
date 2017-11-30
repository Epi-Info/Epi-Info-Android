package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import android.view.View;

public class Cmd_Assign implements ICommand {

	private Token identifierList;
	private Token function;
	private FormLayoutManager controlHelper;
	
	public Cmd_Assign(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		identifierList = reduction.getToken(1);
		function = reduction.getToken(3);
	}
	
	public void Execute()
	{		
		Reduction idReduction = (Reduction)identifierList.getData();
		Token identifier = idReduction.getToken(0);
		String variableName = identifier.getData().toString();
		Reduction funcReduction = (Reduction)function.getData();
		
		if (funcReduction.getParentRule().name().toLowerCase().equals("<decimal_number>"))
		{
			String result = funcReduction.getToken(0).getData().toString();
			VariableCollection.Assign(variableName, result);
		}
		else
		{
			if (funcReduction.getToken(0).getData().getClass() == String.class)
			{
				String result = funcReduction.getToken(0).getData().toString();				
				VariableCollection.Assign(variableName, VariableCollection.GetValue(result));
			}
			else
			{
				String assignVal = FunctionFactory.GetFunction(funcReduction, controlHelper).Execute();
				VariableCollection.Assign(variableName, assignVal);
			}
		}
	}
	
}