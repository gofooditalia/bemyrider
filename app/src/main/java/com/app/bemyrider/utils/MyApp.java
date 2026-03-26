package com.app.bemyrider.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.app.bemyrider.R;
import com.google.firebase.FirebaseApp;
import com.stripe.android.PaymentConfiguration;

/**
 * Optimized by Gemini - 2024.
 */
public class MyApp extends Application {

    private static final String TAG = "MyApp";
    private static final String CHANNEL_ID = "gorider_01";

    @Override
    protected void attachBaseContext(Context base) {
        // Inizializza la lingua a livello di Application per ridurre i refresh delle Activity
        super.attachBaseContext(LocaleManager.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        
        createNotificationChannel();
        
        // client live - PRODUCTION
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_live_51LSl2XJZvpOu0Pgr61B8L0FeCIyOoSM9yveiIIJJH4KuUbPMtcli9zknQ0cSX6ZjkWwOzgwYmgAnTFTssdfgV3uP00fyN2HAdo"
        );
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.notify);
            
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existingChannel != null && existingChannel.getSound() == null) {
                notificationManager.deleteNotificationChannel(CHANNEL_ID);
                existingChannel = null;
            }
            
            if (existingChannel == null) {
                CharSequence channelName = getString(R.string.channel_name);
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
                
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build();
                channel.setSound(defaultSoundUri, audioAttributes);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
