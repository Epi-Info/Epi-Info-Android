package gov.cdc.epiinfo.interpreter;

import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;


public class FunctionFactory {
	
	public static IFunction GetFunction(Reduction funcReduction, FormLayoutManager controlHelper)
	{
		if (funcReduction.getParentRule().name().toLowerCase().equals("<value>"))
		{
			Reduction funcRed1 = (Reduction)funcReduction.getToken(1).getData();
			return GetFunction(funcRed1, controlHelper);
		}
		else if (funcReduction.getParentRule().name().toLowerCase().equals("<functioncall>"))
		{
			Token funcName = ((Reduction)funcReduction.getToken(0).getData()).getToken(0);
		
			if (funcName.getData().toString().toLowerCase().equals("substring"))
			{
				Token funcToken1 = ((Reduction)funcReduction.getToken(2).getData()).getToken(0);
				Token funcToken5 = ((Reduction)funcReduction.getToken(2).getData()).getToken(2);
				Token funcToken2 = ((Reduction)funcToken1.getData()).getToken(0);
				Token funcParam1 = ((Reduction)funcToken2.getData()).getToken(0);
				Token funcToken3 = ((Reduction)funcToken1.getData()).getToken(2);
				Token funcParam2 = ((Reduction)funcToken3.getData()).getToken(0);
				Token funcParam3 = ((Reduction)funcToken5.getData()).getToken(0);
			
				return new Func_Substring(controlHelper, funcParam1.getData().toString(), Integer.parseInt(funcParam2.getData().toString()), Integer.parseInt(funcParam3.getData().toString()));
			}
			else if (funcName.getData().toString().toLowerCase().equals("txttonum"))
			{
				Reduction funcRed0 = (Reduction)funcReduction.getToken(2).getData();
			
				return GetFunction(funcRed0, controlHelper);
			}
			else if (funcName.getData().toString().toLowerCase().equals("round"))
			{
				Reduction funcRed0 = (Reduction)funcReduction.getToken(2).getData();
			
				return new Func_Round(funcRed0, controlHelper);
			}
			else if (funcName.getData().toString().toLowerCase().equals("trunc"))
			{
				Reduction funcRed0 = (Reduction)funcReduction.getToken(2).getData();
			
				return new Func_Trunc(funcRed0, controlHelper);
			}
			else
			{
				return new Func_Empty();
			}
		}
		else if (funcReduction.getParentRule().name().toLowerCase().equals("<add exp>"))
		{
			Token funcToken1 = funcReduction.getToken(0);
			Token operatorToken = funcReduction.getToken(1);
			Token funcToken2 = funcReduction.getToken(2);
			return new Func_Add(funcToken1, funcToken2, operatorToken, controlHelper);
		}
		else if (funcReduction.getParentRule().name().toLowerCase().equals("<mult exp>"))
		{
			Token funcToken1 = funcReduction.getToken(0);
			Token operatorToken = funcReduction.getToken(1);
			Token funcToken2 = funcReduction.getToken(2);
			return new Func_Multiply(funcToken1, funcToken2, operatorToken, controlHelper);
		}
		else
		{
			return new Func_Empty();
		}
	}

}
