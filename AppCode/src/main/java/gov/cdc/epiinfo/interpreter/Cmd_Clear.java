package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import android.view.View;
import android.widget.EditText;

public class Cmd_Clear implements ICommand {

	private Token identifierList;
	private FormLayoutManager controlHelper;
	
	public Cmd_Clear(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		identifierList = reduction.getToken(1);
	}
	
	public void Execute()
	{
		Reduction idReduction = (Reduction)identifierList.getData();
		Token identifier = idReduction.getToken(0);
		System.out.println(".........Clearing " + identifier.getData() + " field");
		EditText control = (EditText) controlHelper.controlsByName.get(identifier.getData());
		control.setText("");
	}
	
}
