package gov.cdc.epiinfo.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import gov.cdc.epiinfo.EpiDbHelper;

public class CloudFactory {

	public static ICloudClient GetCloudClient(String tableName, String surveyId, EpiDbHelper dbHelper, Context context)
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
		else if (serviceProvider.equals("EIWS"))
		{
			cloudClient = new EpiInfoCloudClient(surveyId, context);
		}
		else if (serviceProvider.equals("Couch"))
		{
			cloudClient = new CouchDbClient(tableName, dbHelper, context);
		}
		else
		{
			cloudClient = new AzureClient(tableName, context);
		}
		return cloudClient;
	}
	
}
