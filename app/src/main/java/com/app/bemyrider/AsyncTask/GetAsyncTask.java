package com.app.bemyrider.AsyncTask;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.app.bemyrider.utils.Log;

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by prats on 3/16/2015.
 */
public class GetAsyncTask extends AsyncTask<Void, Void, String> {

    private WebServiceConfig config;
    private String url;
    private OnAsyncResult onAsyncResult;
    private Boolean resultFlag;


    private LinkedHashMap<String, String> textParams;


    public GetAsyncTask(String url, OnAsyncResult listener, LinkedHashMap<String, String> textParams, WebServiceConfig config) {
        this.url = url;
        this.onAsyncResult = listener;
        resultFlag = false;
        this.textParams = textParams;
        this.config = config;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String final_url = url.replaceAll(" ", "%20");
        Log.e("URL", final_url);
        try {
            URL url = new URL(final_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(config.CONNECTION_TIMEOUT_MILLISECONDS);
            httpURLConnection.setReadTimeout(config.READ_TIMEOUT_MILLISECONDS);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);


            Uri.Builder builder = new Uri.Builder();

            for (Map.Entry<String, String> entry : textParams.entrySet()) {
                Log.e("PARAMS", entry.getKey() + ":" + entry.getValue());
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

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
            resultFlag = true;
            return response;
        } catch (SocketTimeoutException e1) {
            resultFlag = false;
            return WebServiceConfig.CONNECTION_TIMEOUT_ERROR;

        } catch (Exception e) {
            e.printStackTrace();
            resultFlag = false;
            return WebServiceConfig.UNEXPECTED_ERROR;
        }
    }


    @Override
    protected void onCancelled() {
        onAsyncResult.OnCancelled("onCancelled");
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(String result) {
        if (resultFlag) {
            if (onAsyncResult != null) {
                onAsyncResult.OnSuccess(result);
            }
        } else {
            if (onAsyncResult != null) {
                onAsyncResult.OnFailure(result);
            }
        }
    }


    public static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer)))
            writer.write(buffer, 0, n);
        return writer.toString();
    }

}