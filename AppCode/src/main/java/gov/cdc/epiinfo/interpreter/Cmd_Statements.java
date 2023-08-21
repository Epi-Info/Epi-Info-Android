package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;

public class Cmd_Statements implements ICommand {

	private Token statement0;
	private Token statement1;
	private FormLayoutManager controlHelper;
	
	public Cmd_Statements(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		statement0 = reduction.getToken(0);
		statement1 = reduction.getToken(1);
	}
	
	public void Execute()
	{
		Reduction statement0Reduction = (Reduction) statement0.getData();		
		ICommand statement0Command = CommandFactory.GetCommand(statement0Reduction, controlHelper);
		statement0Command.Execute();
				
		
		Reduction statement1Reduction = (Reduction) statement1.getData();		
		ICommand statement1Command = CommandFactory.GetCommand(statement1Reduction, controlHelper);
		statement1Command.Execute();
	}
	
}
