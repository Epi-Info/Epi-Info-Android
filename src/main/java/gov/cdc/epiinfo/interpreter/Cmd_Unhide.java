package gov.cdc.epiinfo.interpreter;


import android.view.View;
import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;

public class Cmd_Unhide implements ICommand {

	private Token identifierList;
	private FormLayoutManager controlHelper;
	
	public Cmd_Unhide(Reduction reduction, FormLayoutManager controlHelper)
	{
		identifierList = reduction.getToken(1);
		this.controlHelper = controlHelper;
	}
	
	public void Execute()
	{
		Reduction idReduction = (Reduction)identifierList.getData();
		Token identifier = idReduction.getToken(0);
		System.out.println(".........Unhiding " + identifier.getData() + " field");
		View control = controlHelper.controlsByName.get(identifier.getData());
		control.setVisibility(View.VISIBLE);
		
		View prompt = controlHelper.controlsByName.get(identifier.getData() + "|prompt");
		if (prompt != null)
		{
			prompt.setVisibility(View.VISIBLE);
		}
	}
	
}
