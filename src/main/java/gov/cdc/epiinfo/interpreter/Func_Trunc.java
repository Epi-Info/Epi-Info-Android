package gov.cdc.epiinfo.interpreter;

import goldengine.java.Reduction;
import gov.cdc.epiinfo.FormLayoutManager;
import android.view.View;
import android.widget.EditText;


public class Func_Trunc implements IFunction {

	private FormLayoutManager controlHelper;
	private Reduction funcReduction;
	
	public Func_Trunc(Reduction funcReduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		this.funcReduction = funcReduction;
	}
	
	public String Execute() {

		String retVal = "";
		
		if (funcReduction.getParentRule().name().toLowerCase().equals("<qualified id>"))
		{
			String variable = funcReduction.getToken(0).getData().toString();
			View control = controlHelper.controlsByName.get(variable);
			if (control != null)
			{
				if (control.getClass().equals(EditText.class))
				{
					retVal = ((EditText)control).getText().toString();
				}
			}
			else
			{
				retVal = VariableCollection.GetValue(variable);
			}
		}
		else if (funcReduction.getParentRule().name().toLowerCase().equals("<decimal_number>"))
		{
			retVal = funcReduction.getToken(0).getData().toString();
		}
		else
		{
			retVal = FunctionFactory.GetFunction(funcReduction, controlHelper).Execute();
		}
		
		if (Double.parseDouble(retVal) > 0)
		{
			return Math.floor(Double.parseDouble(retVal)) + "";
		}
		else
		{
			return Math.ceil(Double.parseDouble(retVal)) + "";
		}
	}
}
