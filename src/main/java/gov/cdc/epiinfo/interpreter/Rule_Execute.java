package gov.cdc.epiinfo.interpreter;

import android.app.Activity;
import android.content.Intent;

import com.creativewidgetworks.goldparser.engine.Reduction;

import gov.cdc.epiinfo.RecordList;

public class Rule_Execute extends EnterRule
{
    boolean IsExceptList = false;
    String ExecutionItem = null;

    public Rule_Execute(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);

    	this.ExecutionItem = this.ExtractIdentifier(pToken.get(2)).replace("\"", "");
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        if (this.ExecutionItem.toLowerCase().startsWith("sign") && this.ExecutionItem.contains(","))
        {
            try {
                String[] parts = this.ExecutionItem.split(",");
                this.Context.CheckCodeInterface.CaptureHandwriting(parts[1],parts[2],parts[3]);
            }
            catch (Exception ex)
            {

            }
        }
        else if (this.ExecutionItem.toLowerCase().contains("loadrecord"))
        {
            String[] params = this.ExecutionItem.substring(10).split("&");
            String viewName = "";
            if (params[0].split("=")[1].startsWith("_"))
            {
                viewName = params[0].split("=")[1].toLowerCase().trim();
            }
            else
            {
                viewName = params[0].split("=")[1].trim();
            }
            String varName = params[1].split("=")[0].trim();
            String varValue = "";
            if (params[1].split("=")[1].contains("["))
            {
                varValue = this.Context.CheckCodeInterface.GetValue(params[1].split("=")[1].replace("[","").replace("]","").trim()).toString();
            }
            else
            {
                varValue = params[1].split("=")[1].trim();
            }


            Intent recordList = new Intent((Activity)this.Context.CheckCodeInterface, RecordList.class);
            recordList.putExtra("ViewName", viewName);
            recordList.putExtra("CheckCodeQuery", varName + "='" + varValue + "'");


            ((Activity)this.Context.CheckCodeInterface).startActivity(recordList);

        }
        else if (this.ExecutionItem.toLowerCase().equals("save"))
        {
            try {
                this.Context.CheckCodeInterface.ForceSave();
            }
            catch (Exception ex)
            {

            }
        }
        else if (this.ExecutionItem.contains(":"))
        {
            this.Context.CheckCodeInterface.ExecuteUrl(this.ExecutionItem.replace("::", "://"));
        }
    	else if (this.ExecutionItem.toLowerCase().contains(".pdf") || this.ExecutionItem.toLowerCase().contains(".png") || this.ExecutionItem.toLowerCase().contains(".gif") || this.ExecutionItem.toLowerCase().contains(".jpg") || this.ExecutionItem.toLowerCase().contains(".m4v") || this.ExecutionItem.toLowerCase().contains(".mov") || this.ExecutionItem.toLowerCase().contains(".avi") || this.ExecutionItem.toLowerCase().contains(".wmv"))
    	{
    		this.Context.CheckCodeInterface.DisplayMedia(this.ExecutionItem);
    	}
        return null;
    }
}
