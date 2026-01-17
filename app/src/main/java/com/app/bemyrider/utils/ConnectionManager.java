package com.app.bemyrider.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bemyrider.R;


public class ConnectionManager {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    private Context mContext;
    private Activity mActivity;
    private ViewGroup viewGroup;
    private View snackView;

    public ConnectionManager(Context context) {
        mContext = context;
        mActivity = (Activity) context;
    }

    public void checkConnection(Context mContext) {
        String status = getConnectivityStatusString(mContext);
        setSnackbarMessage(status, false);
    }

    public void registerInternetCheckReceiver() {
        IntentFilter internetFilter = new IntentFilter();
        internetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        try {
            mActivity.registerReceiver(broadcastReceiver, internetFilter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void unregisterReceiver() {
        if (broadcastReceiver != null) {
            mActivity.unregisterReceiver(broadcastReceiver);
        }
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInitialStickyBroadcast()) {
                String status = getConnectivityStatusString(context);
                setSnackbarMessage(status, true);
            }
        }
    };

    public static String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    private void setSnackbarMessage(String status, boolean showInternet) {
        viewGroup = mActivity.findViewById(android.R.id.content);
        snackView = mActivity.getLayoutInflater().inflate(R.layout.custom_snackbar, viewGroup);

        String internetStatus = "";
        if (status.equalsIgnoreCase("Wifi enabled") || status.equalsIgnoreCase("Mobile data enabled")) {
            internetStatus = mContext.getResources().getString(R.string.message_internet_connected);
        } else {
            internetStatus = mContext.getResources().getString(R.string.message_no_connection);
        }
        Log.e("checkConnection: ", internetStatus);
        if (internetStatus.equalsIgnoreCase(mContext.getResources().getString(R.string.message_no_connection))) {

            RelativeLayout relativeLayout = (snackView).findViewById(R.id.snackbar_relativelayout);
            TextView textView = (snackView).findViewById(R.id.snackbar_text);
            relativeLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.colorRed));
            textView.setText(internetStatus);
            relativeLayout.setVisibility(View.INVISIBLE);
            slideUp(relativeLayout);
        } else {
            if (showInternet) {

                final RelativeLayout relativeLayout = (snackView).findViewById(R.id.snackbar_relativelayout);
                TextView textView = (snackView).findViewById(R.id.snackbar_text);
                relativeLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.colorDarkGreen));
                textView.setText(internetStatus);

                relativeLayout.setVisibility(View.INVISIBLE);
                slideUp(relativeLayout);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        slideDown(relativeLayout);
                    }
                }, 2000);
            }
        }
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(300);
        animate.setFillAfter(true);
        animate.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animate);
    }

    public void slideDown(final View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(300);
        animate.setFillAfter(true);
        animate.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animate);
    }
}