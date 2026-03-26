package com.app.bemyrider.AsyncTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.app.bemyrider.R;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.utils.SecurePrefsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Gestione chiamate API con supporto alla cancellazione.
 * Optimized by Gemini - 2024.
 */
public class WebServiceCall {

    private static final String TAG = "WebServiceCall";
    private WebServiceConfig config;
    private ProgressBar progressBar;
    private Context mContext;

    private String url;
    private Object model;

    private OnResultListener OnResultListener;
    private ConnectionCheck cd;
    private boolean isInternetAvailable;
    private Dialog mdialog;

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Future<?> currentTask;
    private boolean isCancelled = false;

    public WebServiceCall(Context mContext, String url, LinkedHashMap<String, String> textParams,
                          Object model, boolean showDialog, OnResultListener OnResultListener) {

        this.url = url;
        this.model = model;
        this.mContext = mContext;
        this.OnResultListener = OnResultListener;
        cd = new ConnectionCheck();
        config = new WebServiceConfig();
        isInternetAvailable = cd.isNetworkConnected(mContext);
        if (isInternetAvailable) {
            executeRequest(textParams, null, showDialog);
        }
    }

    public WebServiceCall(Context mContext, String url, LinkedHashMap<String, String> textParams,
                          LinkedHashMap<String, File> fileParams, Object model, boolean showDialog,
                          OnResultListener OnResultListener) {
        this.url = url;
        this.model = model;
        this.mContext = mContext;
        this.OnResultListener = OnResultListener;
        cd = new ConnectionCheck();
        isInternetAvailable = cd.isNetworkConnected(mContext);
        config = new WebServiceConfig();
        if (isInternetAvailable) {
            executeRequest(textParams, fileParams, showDialog);
        }
    }

    /**
     * Cancella la richiesta corrente.
     */
    public void cancel() {
        isCancelled = true;
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            Log.d(TAG, "Task cancelled for URL: " + url);
        }
        if (mdialog != null && mdialog.isShowing()) {
            mdialog.dismiss();
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    private void executeRequest(final LinkedHashMap<String, String> textParams, 
                                final LinkedHashMap<String, File> fileParams, 
                                final boolean showDialog) {

        SecurePrefsUtil prefs = SecurePrefsUtil.with(mContext);
        textParams.put("lId", prefs.readString("lanId"));
        if (!TextUtils.isEmpty(prefs.readString("UserId"))) {
            textParams.put("login_userid", prefs.readString("UserId"));
        }

        if (showDialog) {
            showProgressDialog();
        }

        if (OnResultListener != null) {
            OnResultListener.onAsync(this);
        }

        currentTask = executor.submit(() -> {
            if (isCancelled) return;

            String result = "";
            boolean success = false;
            long startTime = System.currentTimeMillis();

            try {
                if (fileParams != null && !fileParams.isEmpty()) {
                    result = performMultipartRequest(url, textParams, fileParams);
                } else {
                    result = performPostRequest(url, textParams);
                }

                if (!result.equals(WebServiceConfig.CONNECTION_TIMEOUT_ERROR) 
                        && !result.equals(WebServiceConfig.UNEXPECTED_ERROR)) {
                    success = true;
                }
            } catch (Exception e) {
                result = WebServiceConfig.UNEXPECTED_ERROR;
            }

            if (isCancelled) return;

            long duration = System.currentTimeMillis() - startTime;
            Log.i(TAG, "Request completed in " + duration + "ms: " + url);
            
            final String finalResult = result;
            final boolean finalSuccess = success;

            mainHandler.post(() -> {
                if (isCancelled) return;
                
                if (showDialog) {
                    hideProgressDialog();
                }

                if (finalSuccess) {
                    handleSuccessResponse(finalResult);
                } else {
                    handleFailureResponse(finalResult, showDialog, textParams, fileParams);
                }
            });
        });
    }

    private String performMultipartRequest(String requestUrl, LinkedHashMap<String, String> textParams, 
                                           LinkedHashMap<String, File> fileParams) {
        String final_url = requestUrl.replaceAll(" ", "%20");
        String charset = "UTF-8";
        
        try {
            MultipartUtility multipart = new MultipartUtility(final_url, charset, config);
            for (Map.Entry<String, String> entry : textParams.entrySet()) {
                if (isCancelled) return "";
                multipart.addFormField(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, File> entry : fileParams.entrySet()) {
                if (isCancelled) return "";
                File file = entry.getValue();
                if (file.exists()) {
                    multipart.addFilePart(entry.getKey(), file);
                }
            }
            
            return multipart.finish();
            
        } catch (SocketTimeoutException e1) {
            return WebServiceConfig.CONNECTION_TIMEOUT_ERROR;
        } catch (IOException e) {
            return WebServiceConfig.UNEXPECTED_ERROR;
        } catch (Exception e) {
            return WebServiceConfig.UNEXPECTED_ERROR;
        }
    }

    private String performPostRequest(String requestUrl, LinkedHashMap<String, String> params) {
        String final_url = requestUrl.replaceAll(" ", "%20");
        try {
            URL urlObj = new URL(final_url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(config.CONNECTION_TIMEOUT_MILLISECONDS);
            conn.setReadTimeout(config.READ_TIMEOUT_MILLISECONDS);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            android.net.Uri.Builder builder = new android.net.Uri.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            String query = builder.build().getEncodedQuery();
            if (!TextUtils.isEmpty(query)) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }

            if (isCancelled) return "";
            conn.connect();
            
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = getStringFromInputStream(in);
            return response;

        } catch (SocketTimeoutException e1) {
            return WebServiceConfig.CONNECTION_TIMEOUT_ERROR;
        } catch (Exception e) {
            return WebServiceConfig.UNEXPECTED_ERROR;
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

    private void handleSuccessResponse(String result) {
        try {
            String cleanResult = result.trim().replace("\"", "");
            if (cleanResult.equalsIgnoreCase("s")) {
                if (model == CommonPojo.class) {
                    CommonPojo pojo = new CommonPojo();
                    pojo.setStatus(true);
                    pojo.setMessage("Success");
                    if (OnResultListener != null) OnResultListener.onResult(true, pojo);
                    return;
                } else if (model == String.class) {
                    if (OnResultListener != null) OnResultListener.onResult(true, result);
                    return;
                }
            }

            JSONObject object = new JSONObject(result);
            if (model == String.class) {
                if (OnResultListener != null) OnResultListener.onResult(true, result);
                return;
            } 
            
            if (object.has("status") && object.getBoolean("status")) {
                Gson gson = new GsonBuilder().setDateFormat("M/d/yy hh:mm a").create();
                if (OnResultListener != null) OnResultListener.onResult(true, gson.fromJson(result, (Class) model));
            } else {
                String msg = object.has("message") ? object.getString("message") : "Unknown error";
                if (OnResultListener != null) OnResultListener.onResult(false, msg);
            }

        } catch (Exception e) {
            if (OnResultListener != null) OnResultListener.onResult(false, "Parsing Error");
        }
    }

    private void handleFailureResponse(String result, boolean showDialog, 
                                     final LinkedHashMap<String, String> textParams,
                                     final LinkedHashMap<String, File> fileParams) {
        if (showDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.error);
            builder.setMessage(result);
            builder.setPositiveButton(R.string.retry, (dialog, which) -> {
                dialog.dismiss();
                executeRequest(textParams, fileParams, showDialog);
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                dialog.dismiss();
                if (OnResultListener != null) OnResultListener.onResult(false, "User Cancelled");
            });
            builder.show();
        } else {
            if (OnResultListener != null) OnResultListener.onResult(false, result);
        }
    }

    private void showProgressDialog() {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.VISIBLE);
            } else {
                if (mdialog == null) {
                    mdialog = new Dialog(mContext);
                    mdialog.setCancelable(false);
                    mdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    mdialog.setContentView(R.layout.layout_progress);
                }
                if (!mdialog.isShowing()) mdialog.show();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hideProgressDialog() {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(android.view.View.GONE);
            } else if (mdialog != null && mdialog.isShowing()) {
                mdialog.dismiss();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public interface OnResultListener {
        void onResult(boolean status, Object obj);
        void onAsync(Object obj);
        void onCancelled();
    }
}
