package com.app.bemyrider.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.app.bemyrider.R;
import com.google.firebase.FirebaseApp;
import com.stripe.android.PaymentConfiguration;

/**
 * Created by nct33 on 26/10/17.
 */

public class MyApp extends Application {

    private static final String TAG = "MyApp";
    private static final String CHANNEL_ID = "gorider_01";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        
        // Crea il canale di notifica all'avvio dell'app
        // Questo assicura che il canale sia sempre disponibile, anche quando Firebase
        // gestisce automaticamente le notifiche in background
        createNotificationChannel();
        
        // Non forzare la lingua qui, lascia che LocaleManager la legga dalle preferenze
        // LocaleManager.setLocale(getApplicationContext(), "it");
        // Our nct test
        /* PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51IJCahIKCo79n13atXb6zADvI1wsPhhBNBUHZi90qlxoc1KNCYTjKUEAUqZAPK5k6bQJY7GOWWBAsW2hlYbwRTfZ00onHaC0ZW"
        ); */

        // client test
        /*PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51LSl2XJZvpOu0PgrR5R8dmsO4uMdLVJLeuCfH5MHJJnDz80CPKHscsAsaDnY7jlMkBTyuRQJp3d4TXvQdihQtju500Lsd1sY9r"
        );*/

        // client live - PRODUCTION
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_live_51LSl2XJZvpOu0Pgr61B8L0FeCIyOoSM9yveiIIJJH4KuUbPMtcli9zknQ0cSX6ZjkWwOzgwYmgAnTFTssdfgV3uP00fyN2HAdo"
        );
    }
    
    /**
     * Crea il canale di notifica all'avvio dell'app.
     * Questo assicura che il canale sia sempre configurato correttamente con suono e vibrazione,
     * anche quando Firebase gestisce automaticamente le notifiche in background.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    getSystemService(NotificationManager.class);
            
            // Verifica se il canale esiste già
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            
            // IMPORTANTE: Se il canale esiste ma non ha suono, eliminalo e ricrealo
            // Android non permette di modificare il suono di un canale esistente
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + 
                    getPackageName() + "/" + R.raw.notify);
            
            if (existingChannel != null && existingChannel.getSound() == null) {
                Log.w(TAG, "Channel exists but has no sound. Deleting and recreating...");
                notificationManager.deleteNotificationChannel(CHANNEL_ID);
                existingChannel = null;
            }
            
            if (existingChannel == null) {
                // Crea il canale se non esiste
                CharSequence channelName = getString(R.string.channel_name);
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
                
                // Configura il suono
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED) // Forza la riproduzione
                        .build();
                channel.setSound(defaultSoundUri, audioAttributes);
                
                // Configura la vibrazione
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setShowBadge(true);
                channel.setDescription("Canale per le notifiche push di BeMyRider");
                
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created at app startup with sound: " + defaultSoundUri);
            } else {
                // Il canale esiste già, verifica la configurazione
                Log.d(TAG, "Notification channel already exists. Importance: " + existingChannel.getImportance());
                if (existingChannel.getSound() != null) {
                    Log.d(TAG, "Channel has sound configured: " + existingChannel.getSound());
                    // Verifica che il suono sia quello corretto
                    if (!existingChannel.getSound().equals(defaultSoundUri)) {
                        Log.w(TAG, "Channel sound mismatch! Expected: " + defaultSoundUri + ", Got: " + existingChannel.getSound());
                        // Elimina e ricrea il canale con il suono corretto
                        notificationManager.deleteNotificationChannel(CHANNEL_ID);
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                                .build();
                        channel.setSound(defaultSoundUri, audioAttributes);
                        channel.enableVibration(true);
                        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        channel.setShowBadge(true);
                        channel.setDescription("Canale per le notifiche push di BeMyRider");
                        notificationManager.createNotificationChannel(channel);
                        Log.d(TAG, "Notification channel recreated with correct sound");
                    }
                } else {
                    Log.w(TAG, "Channel exists but has no sound configured!");
                }
            }
        }
    }
}
