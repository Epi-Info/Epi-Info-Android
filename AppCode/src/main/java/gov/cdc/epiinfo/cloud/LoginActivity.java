package gov.cdc.epiinfo.cloud;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import gov.cdc.epiinfo.R;

public class LoginActivity extends AppCompatActivity {

    private Activity context;
    private String REDIRECT_URI = "https://localhost:5003/home/index";
    private String AUTH_URI;
    private String BASE_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        BASE_URI = sharedPref.getString("sftp_url", "");
        AUTH_URI = BASE_URI + "/authorization/signin";

        Login();
    }

    private String extractToken(String uri) {
        return uri.substring(uri.indexOf("token=") + 6);
    }

    private void ShowLoginPage() {
        WebView webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith(REDIRECT_URI)) {

                    // extract OAuth2 access_token appended in url
                    if (url.indexOf("token=") != -1) {
                        String token = extractToken(url);

                        // store in default SharedPreferences
                        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        e.putString("EPI-INFO-API-TOKEN", token);
                        e.commit();

                        setResult(1);
                        finish();
                    }

                    // don't go to redirectUri
                    return true;
                }

                // load the webpage from url: login and grant access
                return super.shouldOverrideUrlLoading(view, url); // return false;
            }
        });

        webview.loadUrl(AUTH_URI + "?ReturnURL=" + REDIRECT_URI);

    }

    private void Login() {
        String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString("EPI-INFO-API-TOKEN", null);
        if (accessToken != null) {
            Date expiration = new JWT(accessToken).getExpiresAt();
            Date now = new Date();
            if (expiration.compareTo(now) > 0) {
                refreshToken(accessToken);
            } else {
                ShowLoginPage();
            }
        } else {
            ShowLoginPage();
        }
    }

    private void refreshToken(String token) {
        new TokenRefresher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token);
    }


    public class TokenRefresher extends AsyncTask<String, Double, String> {
        private int id;

        @Override
        protected String doInBackground(String... params) {

            try {
                String token = params[0];

                StringBuilder builder = new StringBuilder();
                HttpClient client = new DefaultHttpClient();

                HttpGet httpGet = new HttpGet(BASE_URI + "/Refreshtoken?jwtToken=" + token);
                httpGet.setHeader("Authorization", "Bearer " + token + " ");

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
                    return builder.toString();
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        }


        @Override
        protected void onPostExecute(String accessToken) {
            if (accessToken != null) {

                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
                e.putString("EPI-INFO-API-TOKEN", accessToken);
                e.commit();

                Toast.makeText(context, "Already logged in. Validity period has been extended.", Toast.LENGTH_LONG).show();

                setResult(1);
                finish();
            } else {
                ShowLoginPage();
            }
        }
    }


}