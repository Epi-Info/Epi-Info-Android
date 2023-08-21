package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import gov.cdc.epiinfo.FormLayoutManager;

public class CommandFactory {
	
	public static ICommand GetCommand(Reduction reduction, FormLayoutManager controlHelper)
	{
		ICommand command = new Cmd_Empty();
		if (reduction.getParentRule().getText().contains("<Unhide_Some_Statement>"))
			command = new Cmd_Unhide(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Hide_Some_Statement>"))
			command = new Cmd_Hide(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Clear_Statement>"))
			command = new Cmd_Clear(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Enable_Statement>"))
			command = new Cmd_Enable(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Disable_Statement>"))
			command = new Cmd_Disable(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Simple_Dialog_Statement>"))
			command = new Cmd_Dialog(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Assign_Statement>"))
			command = new Cmd_Assign(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Go_To_Variable_Statement>"))
			command = new Cmd_GoTo(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<If_Else_Statement>") || reduction.getParentRule().getText().contains("<If_Statement>"))
			command = new Cmd_If(reduction, controlHelper);
		else if (reduction.getParentRule().getText().contains("<Statements>"))
			command = new Cmd_Statements(reduction, controlHelper); 
		return command;
	}

}
