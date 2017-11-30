package gov.cdc.epiinfo.interpreter;

import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;


public class Cmd_If implements ICommand {
	
	private Token expression;
	private Token thenStatements;
	private Token elseStatements;
	private FormLayoutManager controlHelper;

	public Cmd_If(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		expression = reduction.getToken(1);
		thenStatements = reduction.getToken(3);
		elseStatements = reduction.getToken(5);
	}
	
	private boolean Compare(Token concatToken, Token operatorToken, Token compareToken)
	{
		String operator = (String)operatorToken.getData();
		String value = (String)((Reduction)compareToken.getData()).getToken(0).getData();
		if (((Reduction)concatToken.getData()).getParentRule().name().equals("<FunctionCall>"))
		{
			String val = value.replace("\"", "");
			String text = FunctionFactory.GetFunction((Reduction)concatToken.getData(), controlHelper).Execute();
			if (operator.equals("="))
			{
				return text.equals(val);
			}
			else if (operator.equals("<>"))
			{
				return !text.equals(val);
			}
		}
		String variable = (String)((Reduction)concatToken.getData()).getToken(0).getData();
		
		View control = controlHelper.controlsByName.get(variable);
		if (control != null)
		{
			if (control.getClass().equals(CheckBox.class))
			{
				if (operator.equals("="))
				{
					if (((CheckBox)control).isChecked() && value.equals("(+)"))
					{
						return true;
					}
					else if (!((CheckBox)control).isChecked() && value.equals("(-)"))
					{
						return true;
					}
				}
			}
			else if (control.getClass().equals(LinearLayout.class))
			{
				LinearLayout step1 = (LinearLayout)control;
				RadioGroup step2 = (RadioGroup)step1.getChildAt(0);
				int rawRadioId = step2.getCheckedRadioButtonId();
				int radioId = rawRadioId % 1000;
				
				int intVal = Integer.parseInt(value);
				
				if (operator.equals("="))
				{
					return radioId == intVal;
				}
			}
			else if (control.getClass().equals(EditText.class))
			{
				String text = ((EditText)control).getText().toString();
				int numericInput = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
				if (((EditText)control).getInputType() == numericInput)
				{
					double doubleText = Double.parseDouble(text);
					double doubleVal = Double.parseDouble(value);
					if (operator.equals("="))
					{
						return (doubleText == doubleVal);
					}
					else if (operator.equals("<"))
					{
						return (doubleText < doubleVal);
					}
					else if (operator.equals("<="))
					{
						return (doubleText <= doubleVal);
					}
					else if (operator.equals(">"))
					{
						return (doubleText > doubleVal);
					}
					else if (operator.equals(">="))
					{
						return (doubleText >= doubleVal);
					}
				}
			}
			else if (control.getClass().equals(Spinner.class))
			{
				String text = ((Spinner)control).getSelectedItem().toString();
				if (operator.equals("="))
				{
					return (text.equals(value.replace("\"", "")));
				}
			}
		}
		else
		{
			String text = VariableCollection.GetValue(variable);
			if (VariableCollection.GetVariableType(variable).toLowerCase().equals("numeric"))
			{
				double doubleText = Double.parseDouble(text);
				double doubleVal = 0;
				
				View valueControl = controlHelper.controlsByName.get(value);
				if (valueControl != null)
				{
					String valueText = ((EditText)valueControl).getText().toString();
					doubleVal = Double.parseDouble(valueText);
				}
				else if (VariableCollection.VariableExists(value))
				{
					String valueText = VariableCollection.GetValue(value);
					doubleVal = Double.parseDouble(valueText);
				}
				else
				{
					doubleVal = Double.parseDouble(value);
				}
				if (operator.equals("="))
				{
					return (doubleText == doubleVal);
				}
				else if (operator.equals("<"))
				{
					return (doubleText < doubleVal);
				}
				else if (operator.equals("<="))
				{
					return (doubleText <= doubleVal);
				}
				else if (operator.equals(">"))
				{
					return (doubleText > doubleVal);
				}
				else if (operator.equals(">="))
				{
					return (doubleText >= doubleVal);
				}
				else if (operator.equals("<>"))
				{
					return (doubleText != doubleVal);
				}
			}
			else if (VariableCollection.GetVariableType(variable).toLowerCase().equals("textinput"))
			{
				String val = "";
				
				View valueControl = controlHelper.controlsByName.get(value);
				if (valueControl != null)
				{
					val = ((EditText)valueControl).getText().toString();
				}
				else if (VariableCollection.VariableExists(value))
				{
					val = VariableCollection.GetValue(value);
				}
				else
				{
					val = value.replace("\"", "");
				}
				if (operator.equals("="))
				{
					return text.equals(val);
				}
				else if (operator.equals("<>"))
				{
					return !text.equals(val);
				}
			}
		}
		
		return false;
	}
	
	public void Execute()
	{
		Reduction expReduction = (Reduction) expression.getData();
		boolean result = false;
		if (expReduction.getParentRule().getText().contains("<And Exp>"))
		{
			Reduction leftRed = (Reduction) expReduction.getToken(0).getData();
			Reduction rightRed = (Reduction) expReduction.getToken(2).getData();
			boolean isAnd = expReduction.getToken(1).getData().toString().equals("AND");
			
			if (leftRed.getParentRule().getText().contains("<Compare Exp>") && rightRed.getParentRule().getText().contains("<Compare Exp>"))
			{
				if (isAnd)
				{
					result = Compare(leftRed.getToken(0), leftRed.getToken(1), leftRed.getToken(2)) && Compare(rightRed.getToken(0), rightRed.getToken(1), rightRed.getToken(2));
				}
				else
				{
					result = Compare(leftRed.getToken(0), leftRed.getToken(1), leftRed.getToken(2)) || Compare(rightRed.getToken(0), rightRed.getToken(1), rightRed.getToken(2));
				}
			}
			
			int x = 5;
			x++;
		}
		else if (expReduction.getParentRule().getText().contains("<Compare Exp>"))
		{
			result = Compare(expReduction.getToken(0), expReduction.getToken(1), expReduction.getToken(2));
		}
		
		if (result)
		{				
			Reduction thenReduction = (Reduction) thenStatements.getData();
			ICommand thenCommand = CommandFactory.GetCommand(thenReduction, controlHelper);
			thenCommand.Execute();
		}
		else
		{		
			if (elseStatements != null)
			{
				Reduction elseReduction = (Reduction) elseStatements.getData();		
				ICommand elseCommand = CommandFactory.GetCommand(elseReduction, controlHelper);       
				elseCommand.Execute();
			}
		}

	}
	
}
