package gov.cdc.epiinfo.interpreter;

import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;

import android.view.View;
import android.widget.EditText;


public class Func_Add implements IFunction {
	
	private Token funcToken1;
	private Token funcToken2;
	private Token operatorToken;
	private FormLayoutManager controlHelper;
	
	public Func_Add(Token funcToken1, Token funcToken2, Token operatorToken, FormLayoutManager controlHelper)
	{
		this.funcToken1 = funcToken1;
		this.funcToken2 = funcToken2;
		this.operatorToken = operatorToken;
		this.controlHelper = controlHelper;
	}

	public String Execute() {
		Reduction funcReduction1 = (Reduction)funcToken1.getData();
		Reduction funcReduction2 = (Reduction)funcToken2.getData();
		
		String retVal1 = "";
		String retVal2 = "";
		
		if (funcReduction1.getParentRule().name().toLowerCase().equals("<qualified id>"))
		{
			String variable = funcReduction1.getToken(0).getData().toString();
			View control = controlHelper.controlsByName.get(variable);
			if (control != null)
			{
				if (control.getClass().equals(EditText.class))
				{
					retVal1 = ((EditText)control).getText().toString();
				}
			}
			else
			{
				retVal1 = VariableCollection.GetValue(variable);
			}
		}
		else if (funcReduction1.getParentRule().name().toLowerCase().equals("<decimal_number>"))
		{
			retVal1 = funcReduction1.getToken(0).getData().toString();
		}
		else
		{
			retVal1 = FunctionFactory.GetFunction(funcReduction1, controlHelper).Execute();
		}
		
		if (funcReduction2.getParentRule().name().toLowerCase().equals("<qualified id>"))
		{
			String variable = funcReduction2.getToken(0).getData().toString();
			View control = controlHelper.controlsByName.get(variable);
			if (control != null)
			{
				if (control.getClass().equals(EditText.class))
				{
					retVal2 = ((EditText)control).getText().toString();
				}
			}
			else
			{
				retVal2 = VariableCollection.GetValue(variable);
			}
		}
		else if (funcReduction2.getParentRule().name().toLowerCase().equals("<decimal_number>"))
		{
			retVal2 = funcReduction2.getToken(0).getData().toString();
		}
		else
		{
			retVal2 = FunctionFactory.GetFunction(funcReduction2, controlHelper).Execute();
		}
		
		double result = 0;
		
		if (operatorToken.getData().toString().equals("+"))
		{
			result = Double.parseDouble(retVal1) + Double.parseDouble(retVal2);
		}
		else if (operatorToken.getData().toString().equals("-"))
		{
			result = Double.parseDouble(retVal1) - Double.parseDouble(retVal2);
		}
		
		return result + "";
	}

}
