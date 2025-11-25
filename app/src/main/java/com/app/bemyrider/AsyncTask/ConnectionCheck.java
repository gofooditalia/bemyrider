package com.app.bemyrider.AsyncTask;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;

import com.app.bemyrider.R;


@SuppressWarnings("unused")
public class ConnectionCheck {

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public AlertDialog.Builder showConnectionDialog(Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setCancelable(false)
                .setMessage(WebServiceConfig.INTERNET_ERROR)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder;
    }

    public AlertDialog.Builder showDialogWithMessage(Context context, String message) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Error!")
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder;
    }
}