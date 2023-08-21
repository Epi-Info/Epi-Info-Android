package gov.cdc.epiinfo.interpreter;

import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;


public class Cmd_GoTo implements ICommand {
	
	private Token identifier;
	private FormLayoutManager controlHelper;
	private View view;
	
	public Cmd_GoTo(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		identifier = reduction.getToken(1);
	}
	
	public void Execute()
	{
		view = controlHelper.controlsByName.get(identifier.getData());
		try
		{
			controlHelper.ScrollTo(view.getTop() - 350);

			if (view.getId() != controlHelper.GetExecutingView().getId())
			{			
				view.setFocusable(true);
				view.setFocusableInTouchMode(true);
				view.requestFocus();
			}
		}
		catch (Exception ex)
		{
			
		}
		
	}
	
}
