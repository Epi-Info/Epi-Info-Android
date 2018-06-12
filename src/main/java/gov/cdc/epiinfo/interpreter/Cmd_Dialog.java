package gov.cdc.epiinfo.interpreter;


import goldengine.java.Reduction;
import goldengine.java.Token;
import gov.cdc.epiinfo.FormLayoutManager;
import gov.cdc.epiinfo.RecordEditor;

public class Cmd_Dialog implements ICommand {

	private Token identifierList;
	private FormLayoutManager controlHelper;
	
	public Cmd_Dialog(Reduction reduction, FormLayoutManager controlHelper)
	{
		this.controlHelper = controlHelper;
		identifierList = reduction.getToken(1);
	}
	
	public void Execute()
	{
		String message = identifierList.getData().toString();
		
		if (message.startsWith("\"FILE:"))
		{
			String fileName = message.replace('"', ' ').split(":")[1].trim();
			if (fileName.toLowerCase().endsWith(".pdf"))
			{
				((RecordEditor)controlHelper.getContainer()).DisplayMedia(fileName);
			}
		}
		else
		{
			((RecordEditor)controlHelper.getContainer()).Alert(message.replace('"', ' '));
		}

	}
	
}
