package com.app.bemyrider.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.bemyrider.R;
import com.app.bemyrider.activity.AccountSettingActivity;

import java.util.Date;

/**
 * Helper class per testare le notifiche push e il suono di notifica
 */
public class NotificationTestHelper {
    
    private static final String TAG = "NotificationTestHelper";
    private static final String CHANNEL_ID = "gorider_01";
    
    /**
     * Mostra una notifica di test e riproduce il suono
     * @param context Il contesto dell'applicazione (deve essere un Activity per richiedere permessi)
     */
    public static void testNotification(Context context) {
        try {
            // Verifica permesso notifiche su Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
                    if (context instanceof Activity) {
                        ActivityCompat.requestPermissions((Activity) context, 
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
                    }
                    // Riproduci comunque il suono anche senza permesso
                    playNotificationSound(context);
                    return;
                }
            }
            
            // Crea il canale di notifica se necessario (Android 8.0+)
            createNotificationChannel(context);
            
            // Verifica che il canale esista e sia abilitato
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    Log.e(TAG, "Notification channel is null after creation attempt");
                    playNotificationSound(context);
                    return;
                }
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    Log.w(TAG, "Notification channel is disabled");
                }
            }
            
            // Crea l'intent per aprire l'app quando si clicca sulla notifica
            Intent intent = new Intent(context, AccountSettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            
            // URI del suono di notifica
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + 
                    context.getPackageName() + "/" + R.raw.notify);
            
            // Crea la notifica
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Notifica di test - Push e suono funzionanti!")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Questa è una notifica di test. Se senti il suono e vedi questa notifica, tutto funziona correttamente!"))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            
            int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            notificationManager.notify(notificationId, builder.build());
            
            // Riproduci anche il suono direttamente per essere sicuri
            playNotificationSound(context);
            
            Log.d(TAG, "Test notification sent successfully with ID: " + notificationId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing test notification", e);
            // Riproduci comunque il suono in caso di errore
            playNotificationSound(context);
        }
    }
    
    /**
     * Crea il canale di notifica per Android 8.0+
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            
            // Se il canale esiste già, verifica che sia configurato correttamente
            if (existingChannel != null) {
                Log.d(TAG, "Notification channel already exists. Importance: " + existingChannel.getImportance());
                // Se l'importanza è NONE, il canale è disabilitato
                if (existingChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    Log.w(TAG, "Notification channel is disabled (IMPORTANCE_NONE)");
                }
                return;
            }
            
            // Crea il canale se non esiste
            CharSequence channelName = context.getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            
            // Configura il suono
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + 
                    context.getPackageName() + "/" + R.raw.notify);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            mChannel.setSound(defaultSoundUri, audioAttributes);
            
            // Configura la vibrazione
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(true);
            mChannel.setDescription("Canale per le notifiche push di BeMyRider");
            
            notificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "Notification channel created with importance: " + importance);
        }
    }
    
    /**
     * Riproduce il suono di notifica direttamente
     */
    public static void playNotificationSound(Context context) {
        try {
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + 
                    context.getPackageName() + "/" + R.raw.notify);
            
            Ringtone r = RingtoneManager.getRingtone(context, defaultSoundUri);
            if (r != null) {
                r.play();
                Log.d(TAG, "Notification sound played");
            } else {
                Log.w(TAG, "Could not play notification sound - ringtone is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification sound", e);
        }
    }
}

