package gov.cdc.epiinfo;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.maps.android.data.kml.KmlPlacemark;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import gov.cdc.epiinfo.etc.AudioProcessor;

public class AppManager {

	private static LinkedList<Activity> activities;
	private static EpiDbHelper currentDatabase; 
	private static Hashtable<String, FormMetadata> forms;
	private static Hashtable<Activity, String> guids;
	private static Hashtable<String, Object> permanentVariables;
	private static String defaultForm;
	private static int defaultLayoutMode;
	private static GeoLocation geoLocation;
	private static List<KmlPlacemark> loadedPlacemarks;

	public static void AddFormMetadata(String name, FormMetadata formMetadata)
	{
		if (forms == null)
		{
			forms = new Hashtable<String, FormMetadata>();
		}
		forms.put(name, formMetadata);
	}

	public static FormMetadata GetFormMetadata(String name)
	{
		return forms.get(name);
	}

	public static void AddFormGuid(Activity activity, String guid)
	{
		if (guids == null)
		{
			guids = new Hashtable<Activity, String>();
		}
		guids.put(activity, guid);
	}

	public static String GetFormGuid(Activity activity)
	{
		return guids.get(activity);
	}

	public static void SetPermanentVariable(String varName, Object value)
	{
		if (permanentVariables == null)
		{
			permanentVariables = new Hashtable<String,Object>();
		}
		permanentVariables.put(varName, value);
	}

	public static Object GetPermanentVariable(String varName)
	{
		if (permanentVariables == null)
		{
			return null;
		}
		return permanentVariables.get(varName);
	}

	public static void SetCurrentDatabase(EpiDbHelper db)
	{
		currentDatabase = db;
	}

	public static EpiDbHelper GetCurrentDatabase()
	{
		return currentDatabase;
	}

	public static List<KmlPlacemark> GetPlacemarks()
	{
		return loadedPlacemarks;
	}

	public static void SetPlacemarks(List<KmlPlacemark> placemarks)
	{
		loadedPlacemarks = placemarks;
	}

	public static void Started(Activity activity)
	{
		if (activities == null)
		{
			activities = new LinkedList<Activity>();
		}
		activities.add(activity);
		if (activities.size() == 1)
		{
			if (geoLocation == null)
			{
				geoLocation = new GeoLocation();
			}
			geoLocation.BeginListening(activity);
		}
	}

	public static void Closed(Activity activity)
	{
		GoogleApiClient.Builder b = new GoogleApiClient.Builder(activity);
		if (activities.contains(activity))
		{
			activities.remove(activity);
		}
		if (activities.size() == 0)
		{
			if (geoLocation != null)
			{
				geoLocation.StopListening();
			}
		}
		
		try
		{
			AudioProcessor.DisposeAll();
		}
		catch (Exception ex)
		{
			
		}
	}

	public static String getDefaultForm()
	{
		return defaultForm;
	}

	public static void setDefaultForm(String formName)
	{
		defaultForm = formName;
	}

}
