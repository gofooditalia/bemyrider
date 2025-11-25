/*
package com.app.bemyrider.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.app.bemyrider.utils.LocaleManager;
import com.app.bemyrider.utils.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.app.bemyrider.R;
import com.app.bemyrider.utils.ConnectionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class LinkedInLoginWebViewActivity extends AppCompatActivity {


    */
/*CONSTANT FOR THE AUTHORIZATION PROCESS*//*


    */
/****FILL THIS WITH YOUR INFORMATION*********//*

    //This is the public api key of our application
    //ashvin.dethariya@ncrypted.com / Ncrypted_123
    private static final String CLIENT_ID = "86ibbj08lo4zv6";
    //This is the private api key of our application
    private static final String CLIENT_SECRET = "leUKN9XPergpJXC4";
    //This is any string we want to use. This will be used for avoiding CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private static final String STATE = "E3ZYKC1T6H2yP4z";
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
    //We use a made up url that we will intercept when redirecting. Avoid Uppercases.


    private static final String REDIRECT_URI = "https://gotasker.ncryptedprojects.com/includes-nct/linkedin/callback.php";
    */
/*********************************************//*


    //These are constants used for build the urls
    private static final String AUTHORIZATION_URL = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String SCOPES = "r_emailaddress%20r_liteprofile";
    private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String SECRET_KEY_PARAM = "client_secret";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String GRANT_TYPE_PARAM = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String SCOPE_PARAM = "scope";
    private static final String SCOPES_PARAM = "scopes";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    */
/*---------------------------------------*//*

    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";
    private static final String PROFILE_URL = "https://api.linkedin.com/v2/";
    private static String TAG = "LoginWithLinkedIn";
    private String user_id, socialFirstName, socialLastName, socialEmail, socialImageUrl = "", accessToken = "";

    private WebView linkedInWebView;
    private ProgressDialog pd;

    private Context context;
    private ConnectionManager connectionManager;

    */
/**
     * Method that generates the url for get the authorization token from the Service
     *
     * @return Url
     *//*

    private static String getAuthorizationUrl() {
        String URL = AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + CLIENT_ID
                + AMPERSAND + SCOPE_PARAM + EQUALS + SCOPES
                + AMPERSAND + STATE_PARAM + EQUALS + STATE
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI;
        Log.i("authorization URL", "" + URL);
        return URL;
    }

    private static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer)))
            writer.write(buffer, 0, n);
        return writer.toString().trim();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_linked_in);

        context = LinkedInLoginWebViewActivity.this;

        */
/*Init Internet Connection Class For No Internet Banner*//*

        connectionManager = new ConnectionManager(context);
        connectionManager.registerInternetCheckReceiver();
        connectionManager.checkConnection(context);

        //get the webView from the layout
        linkedInWebView = findViewById(R.id.main_activity_web_view);

        //Request focus for the webview
        linkedInWebView.requestFocus(View.FOCUS_DOWN);

        //Show a progress dialog to the user
        pd = new ProgressDialog(this);
        pd.setTitle(getString(R.string.loading));
        pd.setMessage(getString(R.string.wait_while_loading));
        pd.setCancelable(false);
        pd.show();

        //Set a custom web view client
        linkedInWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
                //This method will be called when the Auth proccess redirect to our RedirectUri.
                //We will check the url looking for our RedirectUri.
                if (authorizationUrl.startsWith(REDIRECT_URI)) {
                    Log.e("Authorize", "");
                    Uri uri = Uri.parse(authorizationUrl);
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the ServiceA is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        Log.e("Authorize", "State token doesn't match");
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Log.e("Authorize", "The user doesn't allow authorization.");
                        return true;
                    }
                    Log.e("Authorize", "Auth token received: " + authorizationToken);

                    //accessTokenServiceCall(authorizationToken);
                    new GetAccessTokenAsyncTask().execute(authorizationToken);

                } else {
                    //Default behaviour
                    Log.e("Authorize", "Redirecting to: " + authorizationUrl);
                    linkedInWebView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        //Get the authorization Url
        String authUrl = getAuthorizationUrl();
        Log.e("Authorize", "Loading Auth Url: " + authUrl);
        //Load the authorization URL into the webView
        linkedInWebView.loadUrl(authUrl);
    }

    private void getLinkedInDetails() {
        if (accessToken != null) {
            new GetProfileRequestAsyncTask().execute(PROFILE_URL +
                            "me?projection=(id,localizedFirstName,localizedLastName,profilePicture(displayImage~:playableStreams))",
                    accessToken);
        }
    }

    private class GetAccessTokenAsyncTask extends AsyncTask<String, Void, String> {

        boolean getAccessTokenResultFlag = false;

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length > 0) {
                String final_url = ACCESS_TOKEN_URL.replaceAll(" ", "%20");
                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter(GRANT_TYPE_PARAM, GRANT_TYPE);
                builder.appendQueryParameter(RESPONSE_TYPE_VALUE, urls[0]);
                builder.appendQueryParameter(REDIRECT_URI_PARAM, REDIRECT_URI);
                builder.appendQueryParameter(CLIENT_ID_PARAM, CLIENT_ID);
                builder.appendQueryParameter(SECRET_KEY_PARAM, CLIENT_SECRET);
                Log.e("FINAL URL =>", "" + final_url);
                try {
                    URL url = new URL(final_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);

                    String query = builder.build().getEncodedQuery();
                    if (!TextUtils.isEmpty(query)) {
                        OutputStream os = httpURLConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(os, "UTF-8"));
                        writer.write(query);
                        writer.flush();
                        writer.close();
                        os.close();
                    }


                    httpURLConnection.connect();
                    Log.e("Response Code:", "Response Code: " + httpURLConnection.getResponseCode());
                    InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                    //String response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
                    String response = getStringFromInputStream(in);
                    Log.e("Response : ", response);
                    getAccessTokenResultFlag = true;
                    return response;
                } catch (SocketTimeoutException e1) {
                    getAccessTokenResultFlag = false;
                    return "Connection has timed out. Do you want to retry?";

                } catch (Exception e) {
                    e.printStackTrace();
                    getAccessTokenResultFlag = false;
                    return "Unexpected error has occurred";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (getAccessTokenResultFlag) {
                pd.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int expiresIn = jsonObject.getInt("expires_in");
                    accessToken = jsonObject.getString("access_token");

                    if (expiresIn > 0 && accessToken != null) {
                        Log.e("Authorize", "This is the access Token: " + accessToken
                                + ". It will expires in " + expiresIn + " secs");

                        getLinkedInDetails();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "OnFailure: " + result);
            }
        }
    }

    private class GetProfileRequestAsyncTask extends AsyncTask<String, Void, String> {
        boolean resultFlag = false;

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length > 0) {
                String raw_url = urls[0];
                String final_url = raw_url.replaceAll(" ", "%20");
                try {
                    Log.e("FINAL URL", final_url);
                    URL url = new URL(final_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("x-li-format", "json");
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + urls[1]);
                    httpURLConnection.setUseCaches(false);

                    httpURLConnection.connect();
                    Log.e("Response Code:", "Response Code: " + httpURLConnection.getResponseCode());
                    String response = "";
                    if (httpURLConnection.getResponseCode() == 200) {
                        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                        response = getStringFromInputStream(in);
                        Log.e("Response : ", response);
                    } else {
                        InputStream in = new BufferedInputStream(httpURLConnection.getErrorStream());
                        response = getStringFromInputStream(in);
                        Log.e("Error Response : ", response);
                    }

                    resultFlag = true;
                    return response;
                } catch (SocketTimeoutException ste) {
                    resultFlag = false;
                    return "Connection has timed out. Do you want to retry?";
                } catch (Exception e) {
                    e.printStackTrace();
                    resultFlag = false;
                    return "Unexpected error has occurred";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (LinkedInLoginWebViewActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
            }
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (result != null) {
                try {
                    JSONObject data = new JSONObject(result);
                    user_id = data.getString("id");
                    socialFirstName = data.getString("localizedFirstName");
                    socialLastName = data.getString("localizedLastName");

                    Log.e("LoginActivity",
                            "User Id : " + user_id +
                                    "\nFirstName : " + socialFirstName +
                                    "\nLastName : " + socialLastName +
                                    "\nProfile Url : " + socialImageUrl);

                    new GetEmailAddressRequestAsyncTask().execute(PROFILE_URL +
                                    "emailAddress?q=members&projection=(elements*(handle~))",
                            accessToken);
                } catch (JSONException e) {
                    Log.e("Authorize", "Error Parsing json " + e.getLocalizedMessage());
                    pd.dismiss();
                }
            }
        }
    }

    private class GetEmailAddressRequestAsyncTask extends AsyncTask<String, String, String> {
        boolean resultFlag = false;

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length > 0) {
                String raw_url = urls[0];
                String final_url = raw_url.replaceAll(" ", "%20");
                try {
                    URL url = new URL(final_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("x-li-format", "json");
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + urls[1]);
                    httpURLConnection.setUseCaches(false);

                    httpURLConnection.connect();
                    Log.e("Response Code:", "Response Code: " + httpURLConnection.getResponseCode());
                    InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                    //String response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
                    String response = getStringFromInputStream(in);
                    Log.e("Response : ", response);
                    resultFlag = true;
                    return response;
                } catch (SocketTimeoutException ste) {
                    resultFlag = false;
                    return "Connection has timed out. Do you want to retry?";
                } catch (Exception e) {
                    e.printStackTrace();
                    resultFlag = false;
                    return "Unexpected error has occurred";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (LinkedInLoginWebViewActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
            }
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (result != null) {
                try {
                    JSONObject data = new JSONObject(result);
                    data.getJSONArray("elements");
                    JSONArray jsonArray = data.getJSONArray("elements");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject actor = jsonArray.getJSONObject(i);
                        socialEmail = actor.getJSONObject("handle~").getString("emailAddress");

                    }

                    Log.e("LoginActivity",
                            "User Id : " + user_id +
                                    "\nFirstName : " + socialFirstName +
                                    "\nLatName : " + socialLastName +
                                    "\nEmail : " + socialEmail +
                                    "\nProfile Url : " + socialImageUrl);

                    Intent i = new Intent();
                    i.putExtra("User Id", user_id);
                    i.putExtra("FirstName", socialFirstName);
                    i.putExtra("LatName", socialLastName);
                    i.putExtra("Email", socialEmail);
                    i.putExtra("Profile Url", socialImageUrl);
                    setResult(RESULT_OK, i);
                    finish();

                } catch (JSONException e) {
                    Log.e("Authorize", "Error Parsing json " + e.getLocalizedMessage());
                    pd.dismiss();
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        try {
            connectionManager.unregisterReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.onAttach(newBase));
    }
}
*/
