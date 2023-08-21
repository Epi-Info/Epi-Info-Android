package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.FormLayoutManager;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class Func_Substring implements IFunction {

	private String variable;
	private int start;
	private int length;
	private FormLayoutManager controlHelper;
	
	public Func_Substring(FormLayoutManager controlHelper, String variable, int start, int length)
	{
		this.controlHelper = controlHelper;
		this.variable = variable;
		this.start = start;
		this.length = length;
	}
	
	public String Execute()
	{
		View control = controlHelper.controlsByName.get(variable);
		if (control != null)
		{
			if (control.getClass().equals(EditText.class))
			{
				try
				{
					String text = ((EditText)control).getText().toString();
					return text.substring(start - 1, start + length - 1);
				}
				catch (Exception ex)
				{
					return "";
				}
			}
		}
		else
		{
			try
			{
				String text = VariableCollection.GetValue(variable);
				return text.substring(start - 1, start + length - 1);
			}
			catch (Exception ex)
			{
				return "";
			}
		}
		
		return "";
	}
	
}
