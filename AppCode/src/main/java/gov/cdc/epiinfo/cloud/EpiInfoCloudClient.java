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
import org.apache.http.client.methods.HttpPut;
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

public class EpiInfoCloudClient implements ICloudClient {

	private String surveyId;
	private Context context;

	public EpiInfoCloudClient(String surveyId, Context context)
	{
		this.context = context;
		this.surveyId = surveyId;
	}


	@Override
	public int getDailyTasks(Activity ctx, String deviceId) {
		return -1;
	}

	public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String serviceUrl = sharedPref.getString("sftp_url", "") + "/api/SurveyResponse";
		String bearer = sharedPref.getString("EPI-INFO-API-TOKEN","");

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();

		HttpGet httpGet = new HttpGet(serviceUrl);
		httpGet.setHeader("Accept", "*/*");
		httpGet.setHeader("SurveyId", surveyId);
		httpGet.setHeader("Authorization", "Bearer " + bearer);


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

	private boolean saveRecord(ContentValues values)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String serviceUrl = sharedPref.getString("sftp_url", "") + "/api/SurveyResponse";
		String bearer = sharedPref.getString("EPI-INFO-API-TOKEN","");

		HttpClient client = new DefaultHttpClient();

		HttpPut httpPut = new HttpPut(serviceUrl);
		httpPut.setHeader("Accept", "*/*");
		httpPut.setHeader("Content-type", "application/json");
		httpPut.setHeader("SurveyId", surveyId);
		httpPut.setHeader("Authorization", "Bearer " + bearer);

		JSONObject jsonObject = new JSONObject();
		try {

			for (String key : values.keySet()) {
				if (!key.equals("_syncStatus")) {
					Object value = values.get(key);
					if (value != null) {
						if (value instanceof Integer) {
							jsonObject.put(key, value);
						} else if (value instanceof Double) {
							if (((Double) value) < Double.POSITIVE_INFINITY) {
								jsonObject.put(key, value);
							}
						} else if (value instanceof Long) {
							jsonObject.put(key, value);
						} else if (value instanceof Boolean) {
							jsonObject.put(key, value);
						} else {
							if (key.equals("id")) {
								jsonObject.put("ResponseId", value.toString());
							} else {
								jsonObject.put(key, value.toString());
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			httpPut.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			HttpResponse response = client.execute(httpPut);
			StatusLine statusLine = response.getStatusLine();
			int status = statusLine.getStatusCode();
			return (status == 201 || status == 200);
		} catch (Exception e) {
			System.out.println("Azure error " + e.toString());
			return false;
		}
	}

	public boolean insertRecord(ContentValues values) {
		return saveRecord(values);
	}

	public boolean deleteRecord(String recordId) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String serviceUrl = sharedPref.getString("sftp_url", "");

		HttpClient client = new DefaultHttpClient();

		HttpDelete httpDelete = new HttpDelete(serviceUrl);
		httpDelete.setHeader("Accept", "application/json");
		httpDelete.setHeader("Content-type", "application/json");
		httpDelete.setHeader("SurveyId", surveyId);


		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ResponseId", recordId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			httpDelete.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


		try {
			client.execute(httpDelete);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean updateRecord(String recordId, ContentValues values) {

		return saveRecord(values);
/*
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String serviceUrl = sharedPref.getString("sftp_url", "");

		HttpClient client = new DefaultHttpClient();
		HttpPatch httpPatch = new HttpPatch(serviceUrl);

		httpPatch.setHeader("Accept", "application/json");
		httpPatch.setHeader("Content-type", "application/json");
		httpPatch.setHeader("SurveyId", surveyId);

		JSONObject jsonObject = new JSONObject();
		try {
			for (String key : values.keySet()) {
				if (!key.equals("_syncStatus")) {
					Object value = values.get(key);
					if (value instanceof Integer) {
						jsonObject.put(key, (Integer) value);
					} else if (value instanceof Double) {
						if (((Double) value) < Double.POSITIVE_INFINITY) {
							jsonObject.put(key, (Double) value);
						}
					} else if (value instanceof Long) {
						jsonObject.put(key, (Long) value);
					} else if (value instanceof Boolean) {
						jsonObject.put(key, (Boolean) value);
					} else {
						if (key.equals("id")) {
							jsonObject.put("ResponseId", value.toString());
						} else {
							jsonObject.put(key, value.toString());
						}
					}
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
			if (client.execute(httpPatch).getStatusLine().getStatusCode() == 200) {
				return true;
			} else {
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
