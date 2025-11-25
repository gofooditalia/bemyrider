package com.app.bemyrider.AsyncTask;

/**
 * Created by prats on 3/3/2015.
 */
public interface OnAsyncResult {
    void OnSuccess(String result);
    void OnFailure(String result);
    void OnCancelled(String result);
}