package com.app.bemyrider.AsyncTask;

import android.os.Handler;
import android.os.Looper;

import com.app.bemyrider.utils.Log;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modernized by Gemini
 */
public class UploadImageAsyncTask {

    private WebServiceConfig config;
    private String url;
    private OnAsyncResult onAsyncResult;
    private Boolean resultFlag;
    private String charset = "UTF-8";

    private HashMap<String, String> textParams;
    private HashMap<String, File> fileParams;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public UploadImageAsyncTask(String url, OnAsyncResult listener, HashMap<String, String> textParams, HashMap<String, File> fileParams, WebServiceConfig config) {
        this.url = url;
        this.onAsyncResult = listener;
        resultFlag = false;
        this.textParams = textParams;
        this.fileParams = fileParams;
        this.config = config;
    }

    public void execute() {
        executor.execute(() -> {
            String result = doInBackground();
            handler.post(() -> onPostExecute(result));
        });
    }

    private String doInBackground() {
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

    private void onPostExecute(String result) {
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
