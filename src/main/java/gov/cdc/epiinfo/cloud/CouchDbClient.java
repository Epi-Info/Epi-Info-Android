package gov.cdc.epiinfo.cloud;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import gov.cdc.epiinfo.EpiDbHelper;

public class CouchDbClient implements ICloudClient {

    private String tableName;
    private String url;
    private String authHeader;
    private Context context;
    private EpiDbHelper dbHelper;

    public CouchDbClient(String tableName, EpiDbHelper dbHelper, Context context) {
        this.context = context;
        this.tableName = tableName.toLowerCase();
        this.dbHelper = dbHelper;

        dbHelper.AddTextColumn("_rev");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        url = sharedPref.getString("sftp_url", "");
        if (url.endsWith("/")) {
            url = url.substring(0, url.lastIndexOf("/"));
        }
        String userName = sharedPref.getString("cloud_user_name", "");
        String password = sharedPref.getString("cloud_pwd", "");

        String auth = userName + ":" + password;
        byte[] encodedAuth = Base64.encode(auth.getBytes(), Base64.DEFAULT);
        authHeader = "Basic " + new String(encodedAuth);

        createTable();
    }

    @Override
    public int getDailyTasks(Activity ctx, String deviceId) {
        return -1;
    }

    public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {

        try {
            JSONObject response = GetResponse(url + "/" + tableName + "/_all_docs?include_docs=true", null, Request.Method.GET);

            if (response != null) {
                JSONArray rows = response.getJSONArray("rows");
                if (rows != null) {
                    JSONArray results = new JSONArray();
                    for (int x = 0; x < rows.length(); x++) {
                        JSONObject item = (JSONObject) ((JSONObject) rows.get(x)).get("doc");
                        results.put(item);
                    }
                    return results;
                }
            }
        } catch (Exception ex) {

        }
        return null;
    }

    private boolean createTable() {
        try {
            HttpClient client = new DefaultHttpClient();

            HttpPut httpPut = new HttpPut(url + "/" + tableName);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            HttpResponse response = client.execute(httpPut);
            return response.getStatusLine().getStatusCode() < 300;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean insertRecord(ContentValues values) {

        String guid = values.getAsString("id");
        values.put("_id", guid);
        values.remove("id");

        if (values.containsKey("_updateStamp"))
        {
            values.remove("_updateStamp");
        }
        if (values.containsKey("_syncStatus"))
        {
            values.remove("_syncStatus");
        }

        JSONObject jsonObject = new JSONObject();
        try {

            for (String key : values.keySet()) {
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
                        jsonObject.put(key, value.toString());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject response = GetResponse(url + "/" + tableName + "/" + guid, jsonObject, Request.Method.PUT);

            if (response != null) {
                String rev = response.getString("rev");
                if (rev != null && !rev.equals("")) {
                    dbHelper.updateRevision(guid, rev);
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("CouchDB error " + e.toString());
        }

        return false;
    }

    public boolean deleteRecord(String recordId) {

        String rev = dbHelper.getFieldValue("_rev", recordId);
        if (rev != null && !rev.equals("")) {

            try {
                JSONObject response = GetResponse(url + "/" + tableName + "/" + recordId + "?rev=" + rev, null, Request.Method.DELETE);
                if (response != null) {
                    return response.getBoolean("ok");
                }
            }
            catch (Exception ex)
            {

            }
        }

        return false;
    }

    public boolean updateRecord(String recordId, ContentValues values) {

        if (!values.containsKey("_rev")) {
            String rev = dbHelper.getFieldValue("_rev", recordId);
            if (rev != null && !rev.equals("")) {
                values.put("_rev", rev);
            }
        }
        return insertRecord(values);
    }

    public JSONObject GetResponse(String url, JSONObject json, int method) {

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(method, url, json, future, future) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(HttpHeaders.AUTHORIZATION, authHeader);
                return params;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError != null && volleyError.networkResponse != null) {

                    String error = new String(volleyError.networkResponse.data);
                }
                return volleyError;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);

        try {
            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
