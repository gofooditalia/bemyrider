package com.app.bemyrider.AsyncTask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.app.bemyrider.R;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // Executor per sostituire AsyncTask
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Deprecated legacy references to avoid breaking existing code immediately if referenced directly
    // private GetAsyncTask getAsyncTask; 
    // private UploadImageAsyncTask uploadImageAsyncTask;

    public WebServiceCall(Context mContext, String url, LinkedHashMap<String, String> textParams,
                          Object model, boolean showDialog, OnResultListener OnResultListener) {

        Log.e(TAG, "WebServiceCall: " + textParams.toString());
        this.url = url;
        this.model = model;
        this.mContext = mContext;
        this.OnResultListener = OnResultListener;
        cd = new ConnectionCheck();
        config = new WebServiceConfig();
        isInternetAvailable = cd.isNetworkConnected(mContext);
        if (isInternetAvailable) {
            executeRequest(textParams, null, showDialog);
        } else {
            // Gestione mancanza rete
            if (OnResultListener != null) {
                // OnResultListener.onResult(false, mContext.getString(R.string.no_internet));
            }
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
        } else {
             // Gestione mancanza rete
        }
    }

    private void executeRequest(final LinkedHashMap<String, String> textParams, 
                                final LinkedHashMap<String, File> fileParams, 
                                final boolean showDialog) {

        // Iniettiamo parametri comuni (token, lingua, userid)
        SecurePrefsUtil prefs = SecurePrefsUtil.with(mContext);
        textParams.put("lId", prefs.readString("lanId"));
        if (!TextUtils.isEmpty(prefs.readString("UserId"))) {
            textParams.put("login_userid", prefs.readString("UserId"));
        }

        // Show Dialog UI
        if (showDialog) {
            showProgressDialog();
        }

        // Notifica start
        if (OnResultListener != null) {
            OnResultListener.onAsync(null); // Passiamo null perché non c'è più AsyncTask
        }

        // Esecuzione in background
        executor.execute(() -> {
            String result = "";
            boolean success = false;

            if (fileParams != null && !fileParams.isEmpty()) {
                // TODO: Implementare logica Multipart se servono upload file (UploadImageAsyncTask)
                // Per ora gestiamo solo richieste testuali standard per login/registrazione
                // result = performMultipartRequest(url, textParams, fileParams);
                // Fallback temporaneo: logica upload complessa da migrare separatamente se necessario
                success = false;
                result = "File upload migration pending";
            } else {
                // Richiesta Standard POST
                result = performPostRequest(url, textParams);
                // Se il risultato non è un messaggio di errore di rete, assumiamo successo HTTP
                if (!result.equals(WebServiceConfig.CONNECTION_TIMEOUT_ERROR) 
                        && !result.equals(WebServiceConfig.UNEXPECTED_ERROR)) {
                    success = true;
                }
            }

            final String finalResult = result;
            final boolean finalSuccess = success;

            // Post result on UI Thread
            mainHandler.post(() -> {
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

    private String performPostRequest(String requestUrl, LinkedHashMap<String, String> params) {
        String final_url = requestUrl.replaceAll(" ", "%20");
        Log.e("URL", final_url);
        try {
            URL urlObj = new URL(final_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlObj.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(config.CONNECTION_TIMEOUT_MILLISECONDS);
            httpURLConnection.setReadTimeout(config.READ_TIMEOUT_MILLISECONDS);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    Log.e("PARAMS", entry.getKey() + ":" + entry.getValue());
                    builder.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }

            String query = builder.build().getEncodedQuery();
            if (!TextUtils.isEmpty(query)) {
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }

            httpURLConnection.connect();
            Log.e("Response Code:", "Response Code: " + httpURLConnection.getResponseCode());
            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
            String response = getStringFromInputStream(in);
            Log.e("Response : ", response);
            return response;

        } catch (SocketTimeoutException e1) {
            return WebServiceConfig.CONNECTION_TIMEOUT_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
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
            JSONObject object = new JSONObject(result);
            if (model == String.class) {
                if (OnResultListener != null) OnResultListener.onResult(true, result);
                return; // Stop here for String requests
            } 
            
            if (object.has("status") && object.getBoolean("status")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                Gson gson = gsonBuilder.create();
                if (OnResultListener != null) OnResultListener.onResult(true, gson.fromJson(result, (Class) model));
            } else {
                String msg = object.has("message") ? object.getString("message") : "Unknown error";
                if (OnResultListener != null) OnResultListener.onResult(false, msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (OnResultListener != null) OnResultListener.onResult(false, "Parsing Error: " + e.getMessage());
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
                // Retry logic
                executeRequest(textParams, fileParams, showDialog);
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                dialog.dismiss();
                if (OnResultListener != null) OnResultListener.onResult(false, "User Cancelled Retry");
            });
            builder.show();
        } else {
            if (OnResultListener != null) OnResultListener.onResult(false, result);
        }
    }

    private void showProgressDialog() {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                if (mdialog == null) {
                    mdialog = new Dialog(mContext);
                    mdialog.setTitle("");
                    mdialog.setCancelable(false);
                    mdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    mdialog.setContentView(R.layout.layout_progress);
                }
                if (!mdialog.isShowing()) mdialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            } else {
                if (mdialog != null && mdialog.isShowing()) {
                    mdialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnResultListener {
        void onResult(boolean status, Object obj);

        // Mantenuto per compatibilità, ma deprecato
        void onAsync(AsyncTask asyncTask);

        void onCancelled();
    }
}