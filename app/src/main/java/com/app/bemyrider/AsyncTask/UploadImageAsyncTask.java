package com.app.bemyrider.AsyncTask;


import android.os.AsyncTask;
import com.app.bemyrider.utils.Log;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nct58 on 19/01/2017.
 * http://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
 */
public class UploadImageAsyncTask extends AsyncTask<Void, Void, String> {

    private WebServiceConfig config;
    private String url;
    private OnAsyncResult onAsyncResult;
    private Boolean resultFlag;
    private String charset = "UTF-8";


    private HashMap<String, String> textParams;
    private HashMap<String, File> fileParams;


    public UploadImageAsyncTask(String url, OnAsyncResult listener, HashMap<String, String> textParams, HashMap<String, File> fileParams, WebServiceConfig config) {
        this.url = url;
        this.onAsyncResult = listener;
        resultFlag = false;
        this.textParams = textParams;
        this.fileParams = fileParams;
        this.config = config;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            MultipartUtility multipart;
            multipart = new MultipartUtility(String.valueOf(url), charset, config);

            Log.e("LINK", url);

            multipart.addHeaderField("User-Agent", "CodeJava");
            multipart.addHeaderField("Test-Header", "Header-Value");

            for (Map.Entry<String, String> entry : textParams.entrySet()) {
                Log.e("PARAMS", entry.getKey() + ":" + entry.getValue());
                multipart.addFormField(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, File> entry : fileParams.entrySet()) {
                Log.e("FILE PARAMS", entry.getKey() + ":" + entry.getValue());
                multipart.addFilePart(entry.getKey(), entry.getValue());
            }

            String response = multipart.finish();
            Log.e("AsyncTask", "doInBackground: " + response);
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
}