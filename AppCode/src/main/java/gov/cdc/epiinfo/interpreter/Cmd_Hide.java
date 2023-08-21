package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import android.view.View;

public class Cmd_Hide implements ICommand {

	private Token identifierList;
	private FormLayoutManager controlHelper;
	
	public Cmd_Hide(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		identifierList = reduction.getToken(1);
	}
	
	public void Execute()
	{
		Reduction idReduction = (Reduction)identifierList.getData();
		Token identifier = idReduction.getToken(0);
		System.out.println(".........Hiding " + identifier.getData() + " field");
		View control = controlHelper.controlsByName.get(identifier.getData());
		control.setVisibility(View.INVISIBLE);
		
		View prompt = controlHelper.controlsByName.get(identifier.getData() + "|prompt");
		if (prompt != null)
		{
			prompt.setVisibility(View.INVISIBLE);
		}
	}
	
}
