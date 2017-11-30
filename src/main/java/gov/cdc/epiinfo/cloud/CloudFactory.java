package gov.cdc.epiinfo.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CloudFactory {

	public static ICloudClient GetCloudClient(String tableName, Context context)
	{
		ICloudClient cloudClient;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String serviceProvider = sharedPref.getString("cloud_service", "");
		if (serviceProvider.equals("Box"))
		{
			cloudClient = new BoxClient(tableName, context); 
		}
		else if (serviceProvider.equals("SFTP"))
		{
			cloudClient = new SecureFTPClient(tableName, context);
		}
		else
		{
			cloudClient = new AzureClient(tableName, context);
		}
		return cloudClient;
	}
	
}
