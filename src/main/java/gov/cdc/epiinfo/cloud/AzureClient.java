package gov.cdc.epiinfo.cloud;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import gov.cdc.epiinfo.EpiDbHelper;

public class AzureClient implements ICloudClient {

	private String tableName;
	private Context context;

	public AzureClient(String tableName, Context context)
	{
		this.context = context;
		this.tableName = tableName;

		if (tableName.startsWith("_"))
		{
			this.tableName = tableName.replaceFirst("_", "");
		}
	}

	@Override
	public int getDailyTasks(Activity ctx, String deviceId) {
		return -1;
	}

	public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mobileServiceName = sharedPref.getString("service_name", "");
		String applicationKey = sharedPref.getString("application_key", "");
		Boolean useClassic = sharedPref.getBoolean("azure_classic", true);

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		String url;
		if (useClassic)
			url = "https://" + mobileServiceName + ".azure-mobile.net/tables/" + tableName;// + "?$top=9999";
		else
			url = "https://" + mobileServiceName + ".azurewebsites.net/tables/" + tableName;// + "?$top=9999";

		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("Content-type", "application/json");
		httpGet.setHeader("X-ZUMO-APPLICATION", applicationKey);
		if (!useClassic)
		{
			httpGet.setHeader("ZUMO-API-VERSION", "2.0.0");
			httpGet.setHeader("epi-token", applicationKey);
		}


		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				return new JSONArray(builder.toString());

			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		} 
	}

	public boolean insertRecord(ContentValues values) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mobileServiceName = sharedPref.getString("service_name", "");
		String applicationKey = sharedPref.getString("application_key", "");
		Boolean useClassic = sharedPref.getBoolean("azure_classic", true);

		HttpClient client = new DefaultHttpClient();
		String url;
		if (useClassic)
			url = "https://" + mobileServiceName + ".azure-mobile.net/tables/" + tableName;
		else
			url = "https://" + mobileServiceName + ".azurewebsites.net/tables/" + tableName;

		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("X-ZUMO-APPLICATION", applicationKey);
		if (!useClassic)
		{
			httpPost.setHeader("ZUMO-API-VERSION", "2.0.0");
			httpPost.setHeader("epi-token", applicationKey);
		}

		JSONObject jsonObject = new JSONObject();
		try {

			for (String key : values.keySet())
			{
				Object value = values.get(key);
				if (value != null)
				{
					if (value instanceof Integer)
					{
						jsonObject.put(key, value);
					}
					else if (value instanceof Double)
					{
						if (((Double)value) < Double.POSITIVE_INFINITY)
						{
							jsonObject.put(key, value);
						}
					}
					else if (value instanceof Long)
					{
						jsonObject.put(key, value);
					}
					else if (value instanceof Boolean)
					{
						jsonObject.put(key, value);
					}
					else
					{
						jsonObject.put(key, value.toString());
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			httpPost.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			int status = client.execute(httpPost).getStatusLine().getStatusCode();
            return status == 201;
		} catch (Exception e) {
			System.out.println("Azure error " + e.toString());
			return false;
		}
	}

	public boolean deleteRecord(String recordId) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mobileServiceName = sharedPref.getString("service_name", "");
		String applicationKey = sharedPref.getString("application_key", "");
		Boolean useClassic = sharedPref.getBoolean("azure_classic", true);

		HttpClient client = new DefaultHttpClient();
		String url;
		if (useClassic)
			url = "https://" + mobileServiceName + ".azure-mobile.net/tables/" + tableName + "/" + recordId;
		else
			url = "https://" + mobileServiceName + ".azurewebsites.net/tables/" + tableName + "/" + recordId;

		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.setHeader("Accept", "application/json");
		httpDelete.setHeader("Content-type", "application/json");
		httpDelete.setHeader("X-ZUMO-APPLICATION", applicationKey);
		if (!useClassic)
		{
			httpDelete.setHeader("ZUMO-API-VERSION", "2.0.0");
			httpDelete.setHeader("epi-token", applicationKey);
		}

		try {
			client.execute(httpDelete);
			return true;
		} catch (Exception e) {
			return false;
		} 
	}

	public boolean updateRecord(String recordId, ContentValues values) {

		try
		{
			deleteRecord(recordId);
		}
		catch (Exception ex)
		{

		}
		return insertRecord(values);
		/*		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mobileServiceName = sharedPref.getString("service_name", "");
		String applicationKey = sharedPref.getString("application_key", "");

		HttpClient client = new DefaultHttpClient();
		HttpPatch httpPatch = new HttpPatch("https://" + mobileServiceName + ".azure-mobile.net/tables/" + tableName + "/" + recordId);

		httpPatch.setHeader("Accept", "application/json");
		httpPatch.setHeader("Content-type", "application/json");
		httpPatch.setHeader("X-ZUMO-APPLICATION", applicationKey);

		JSONObject jsonObject = new JSONObject();
		try {
			for (String key : values.keySet())
			{
				Object value = values.get(key);
				if (value instanceof Integer)
				{
					jsonObject.put(key, (Integer)value);
				}
				else if (value instanceof Double)
				{
					if (((Double)value) < Double.POSITIVE_INFINITY)
					{
						jsonObject.put(key, (Double)value);
					}
				}
				else if (value instanceof Long)
				{
					jsonObject.put(key, (Long)value);
				}
				else if (value instanceof Boolean)
				{
					jsonObject.put(key, (Boolean)value);
				}
				else
				{
					jsonObject.put(key, value.toString());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			httpPatch.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			HttpResponse resp = client.execute(httpPatch);
			if (client.execute(httpPatch).getStatusLine().getStatusCode() == 200)
			{
				return true;
			}
			else
			{
				return false;
			}
		} catch (Exception e) {
			return false;
		} 
		 */
	}

	public class HttpDelete extends HttpEntityEnclosingRequestBase {

		public final static String METHOD_NAME = "DELETE";

		public HttpDelete() {
			super();
		}

		public HttpDelete(final URI uri) {
			super();
			setURI(uri);
		}

		public HttpDelete(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		@Override
		public String getMethod() {
			return METHOD_NAME;
		}

	}

	public class HttpPatch extends HttpEntityEnclosingRequestBase {

		public final static String METHOD_NAME = "PATCH";

		public HttpPatch() {
			super();
		}

		public HttpPatch(final URI uri) {
			super();
			setURI(uri);
		}

		public HttpPatch(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		@Override
		public String getMethod() {
			return METHOD_NAME;
		}

	}


}
