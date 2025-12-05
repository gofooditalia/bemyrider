package com.app.bemyrider.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.partner.Partner_DisputeDetail_Activity;
import com.app.bemyrider.activity.partner.Partner_ServiceRequestDetail_Tablayout_Activity;
import com.app.bemyrider.activity.partner.PartnerServiceRequestDetailsActivity;
import com.app.bemyrider.activity.user.BookedServiceDetailActivity;
import com.app.bemyrider.activity.user.MessageDetailActivity;

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.R;
import com.app.bemyrider.WebServices.WebServiceUrl;
import com.app.bemyrider.model.CommonPojo;
import com.app.bemyrider.model.EventBusMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by nct33 on 13/9/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New FCM token received: " + token);
        
        // Salva il token usando SecurePrefsUtil per coerenza con il resto dell'app
        SecurePrefsUtil securePrefs = null;
        try {
            securePrefs = SecurePrefsUtil.with(getApplicationContext());
            securePrefs.write("device_token", token);
            Log.d(TAG, "Token saved to SecurePrefsUtil");
        } catch (Exception e) {
            Log.e(TAG, "Error saving token to SecurePrefsUtil, falling back to PrefsUtil", e);
            // Fallback al vecchio sistema se SecurePrefsUtil non è disponibile
            PrefsUtil.with(getApplicationContext()).write("device_token", token);
        }
        
        super.onNewToken(token);
        
        // Invia il token al backend se l'utente è già loggato
        sendTokenToBackend(token, securePrefs);
    }
    
    /**
     * Invia il device token al backend se l'utente è loggato.
     * Usa l'endpoint edit profile con solo user_id e device_token.
     */
    private void sendTokenToBackend(String token, SecurePrefsUtil securePrefs) {
        try {
            // Usa SecurePrefsUtil se disponibile, altrimenti PrefsUtil
            String userId = null;
            if (securePrefs != null) {
                userId = securePrefs.readString("UserId");
            } else {
                userId = PrefsUtil.with(getApplicationContext()).readString("UserId");
            }
            
            // Verifica se l'utente è loggato
            if (userId == null || userId.isEmpty()) {
                Log.d(TAG, "User not logged in, skipping token update to backend");
                return;
            }
            
            Log.d(TAG, "User logged in (ID: " + userId + "), sending token to backend");
            
            // Prepara i parametri per la chiamata API
            LinkedHashMap<String, String> textParams = new LinkedHashMap<>();
            textParams.put("user_id", userId);
            textParams.put("device_token", token);
            
            // Chiama l'endpoint edit profile con solo user_id e device_token
            // Il backend dovrebbe accettare solo questi parametri per aggiornare il token
            new WebServiceCall(
                getApplicationContext(),
                WebServiceUrl.URL_EDIT_PROFILE,
                textParams,
                CommonPojo.class,
                false, // Non mostrare dialog di progresso per chiamate in background
                new WebServiceCall.OnResultListener() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            Log.d(TAG, "Device token successfully updated on backend");
                        } else {
                            Log.e(TAG, "Failed to update device token on backend: " + obj);
                        }
                    }
                    
                    @Override
                    public void onAsync(android.os.AsyncTask asyncTask) {
                        // Non necessario per la nuova implementazione
                    }
                    
                    @Override
                    public void onCancelled() {
                        Log.w(TAG, "Token update request cancelled");
                    }
                }
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending token to backend", e);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        boolean needNotification = true;
        
        // Log dettagliato per debugging
        Log.e(TAG, "========== onMessageReceived CALLED ==========");
        Log.e("WithoutBalancePojoItem", "Received");

        // Verifica se c'è un campo "notification" (che causa gestione automatica quando app è in background)
        if (remoteMessage.getNotification() != null) {
            Log.w(TAG, "WARNING: Notification has 'notification' field - Firebase may handle it automatically when app is in background");
            Log.w(TAG, "Notification title: " + remoteMessage.getNotification().getTitle());
            Log.w(TAG, "Notification body: " + remoteMessage.getNotification().getBody());
            Log.w(TAG, "IMPORTANT: If app is in background, onMessageReceived may NOT be called!");
            Log.w(TAG, "SOLUTION: Backend should send ONLY 'data' payload (without 'notification' field)");
        } else {
            Log.d(TAG, "Notification has only 'data' field - onMessageReceived will always be called");
        }
        
        // Log dello stato dell'app (foreground/background)
        Log.d(TAG, "App state: " + (isAppInForeground() ? "FOREGROUND" : "BACKGROUND"));

        Log.e("WithoutBalancePojoItem", remoteMessage.getData().get("notification_type"));
        Log.e("WithoutBalancePojoItem", "Message Data : " + remoteMessage.getData().toString());

        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("body"));
        Log.e(TAG, "Notification DATA: " + remoteMessage.getData().toString());
        Log.e(TAG, "Notification TITLE: " + remoteMessage.getData().get("title"));
        Log.e(TAG, "Notification SOUND: " + remoteMessage.getData().get("sound"));

        String type = remoteMessage.getData().get("notification_type");
        String userType = remoteMessage.getData().get("user_type");
        String notificationConstant = remoteMessage.getData().get("notification_constant");
        
        Log.d(TAG, "Notification type: " + type + ", userType: " + userType);

        PendingIntent pendingIntent = null;
        
        // Verifica che type e userType non siano null
        if (type == null || userType == null) {
            Log.e(TAG, "ERROR: type or userType is null! type=" + type + ", userType=" + userType);
            Log.e(TAG, "Cannot process notification without type and userType");
            return;
        }
        
        /*---------- Customer side message notification ----------*/
        if (type.equals("m") && userType.equals("c")) {
            EventBus.getDefault().post(new EventBusMessage("msg", remoteMessage.getData()));
            Intent intent = new Intent(this, MessageDetailActivity.class);
            intent.putExtra("to_user", remoteMessage.getData().get("provider_id"));
            intent.putExtra("master_id", remoteMessage.getData().get("service_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        /*---------- Customer side service notification ----------*/
        else if (type.equals("s") && userType.equals("c")) {
            EventBus.getDefault().post(new EventBusMessage("s", remoteMessage.getData()));
            Intent intent = new Intent(this, BookedServiceDetailActivity.class);
            intent.putExtra("serviceRequestId", remoteMessage.getData().get("service_request_id"));
            intent.putExtra("providerServiceId", remoteMessage.getData().get("provider_service_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        /*---------- Provider side message notification ----------*/
        else if (type.equals("m") && userType.equals("p")) {
            EventBus.getDefault().post(new EventBusMessage("msg", remoteMessage.getData()));
            Intent intent = new Intent(this, MessageDetailActivity.class);
            intent.putExtra("to_user", remoteMessage.getData().get("customer_id"));
            intent.putExtra("master_id", remoteMessage.getData().get("service_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        /*---------- Provider side service notification ----------*/
        else if (type != null && type.equals("s") && userType != null && userType.equals("p")) {
            Log.d(TAG, "Processing PROVIDER service notification (rider booking request)");
            if (notificationConstant != null && notificationConstant.equalsIgnoreCase("AC_NT_NOTIFY_ME_WHEN_SERVICE_COMPLETED")) {
                Intent intent = new Intent(this, Partner_ServiceRequestDetail_Tablayout_Activity.class);
                intent.putExtra("serviceRequestId", remoteMessage.getData().get("service_request_id"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                }
            } else {
                Intent intent = new Intent(this, PartnerServiceRequestDetailsActivity.class);
                intent.putExtra("serviceRequestId", remoteMessage.getData().get("service_request_id"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                }
            }
            EventBus.getDefault().post(new EventBusMessage("s", remoteMessage.getData()));
            Log.d(TAG, "EventBus message posted for provider service notification");
        }
        /*---------- Customer and Provider side dispute notification ----------*/
        else if (type.equals("d")) {
            Intent intent = new Intent(this, Partner_DisputeDetail_Activity.class);
            intent.putExtra("DisputeId", remoteMessage.getData().get("dispute_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            EventBus.getDefault().post(new EventBusMessage("s", remoteMessage.getData()));
        }
        /*---------- Provider side review notification ----------*/
        else if (type.equals("r") && userType.equals("p")) {
            Intent intent = new Intent(this, Partner_ServiceRequestDetail_Tablayout_Activity.class);
            intent.putExtra("serviceRequestId", remoteMessage.getData().get("service_request_id"));
            intent.putExtra("fromReviewNotification", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        } else if (type.equals("notifyPayment") && userType.equals("c")) {
            needNotification = false;
            EventBus.getDefault().post(new EventBusMessage("notifyPayment", remoteMessage.getData()));
        } else if (type.equals("updateCurrency")) {
            needNotification = false;
            PrefsUtil.with(getApplicationContext()).write("CurrencySign", remoteMessage.getData().get("currency_sign"));
            Log.e("CURRENCY SIGN IS : ", PrefsUtil.with(getApplicationContext()).readString("CurrencySign"));
        }
        /*-------- User DeActive Notification --------*/
        else if (type.equalsIgnoreCase("userdeactive")) {
            PrefsUtil.with(this).clearPrefs();
            Intent intent = new Intent(this, SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        if (needNotification) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Uri defaultSoundUri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.notify); //Here is FILE_NAME is the name of file that you want to play
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.notify);
            Log.e("defaultSoundUri",defaultSoundUri.toString());

            Notification notification;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The id of the channel.
                String Ch_id = "gorider_01";

                // The user-visible name of the channel.
                CharSequence name = getString(R.string.channel_name);

                // The user-visible description of the channel.
                //String description = getString(R.string.channel_description);

                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel mChannel = notificationManager.getNotificationChannel(Ch_id);
                
                // IMPORTANTE: Se il canale esiste ma non ha suono, eliminalo e ricrealo
                // Android non permette di modificare il suono di un canale esistente
                if (mChannel != null && mChannel.getSound() == null) {
                    Log.w(TAG, "Channel exists but has no sound. Deleting and recreating...");
                    notificationManager.deleteNotificationChannel(Ch_id);
                    mChannel = null;
                }
                
                if (mChannel == null) {
                    mChannel = new NotificationChannel(Ch_id, name, importance);
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build();
                    mChannel.setSound(defaultSoundUri, audioAttributes);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    mChannel.setShowBadge(true);
                    mChannel.setDescription("Canale per le notifiche push di BeMyRider");
                    notificationManager.createNotificationChannel(mChannel);
                    Log.d(TAG, "Notification channel created/recreated with sound: " + defaultSoundUri);
                } else {
                    // Verifica che il canale abbia il suono configurato
                    if (mChannel.getSound() != null) {
                        Log.d(TAG, "Notification channel exists with sound: " + mChannel.getSound());
                        // Verifica che il suono sia quello corretto
                        if (!mChannel.getSound().equals(defaultSoundUri)) {
                            Log.w(TAG, "Channel sound mismatch! Expected: " + defaultSoundUri + ", Got: " + mChannel.getSound());
                            // Elimina e ricrea il canale con il suono corretto
                            notificationManager.deleteNotificationChannel(Ch_id);
                            mChannel = new NotificationChannel(Ch_id, name, importance);
                            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                    .build();
                            mChannel.setSound(defaultSoundUri, audioAttributes);
                            mChannel.enableVibration(true);
                            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                            mChannel.setShowBadge(true);
                            mChannel.setDescription("Canale per le notifiche push di BeMyRider");
                            notificationManager.createNotificationChannel(mChannel);
                            Log.d(TAG, "Notification channel recreated with correct sound");
                        }
                    } else {
                        Log.w(TAG, "Notification channel exists but has no sound configured!");
                    }
                }

                // Create a notification and set the notification channel.
                /*.setSmallIcon(R.drawable.notify)*/
                // IMPORTANTE: Per Android O+, il suono viene gestito dal canale di notifica
                // Assicuriamoci che il canale abbia il suono configurato correttamente
                notification = new Notification.Builder(this, Ch_id)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        /*.setContentText(remoteMessage.getData().get("title"))*/
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new Notification.BigTextStyle().bigText(remoteMessage.getData().get("title")))
                        .setChannelId(Ch_id)
                        .setDefaults(Notification.DEFAULT_ALL) // Aggiunge suono, vibrazione, LED
                        .setPriority(Notification.PRIORITY_HIGH) // Priorità alta per garantire il suono
                        .build();
            } else {
                // Create a notification
                notification = new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        /*.setContentText(remoteMessage.getData().get("title"))*/
                        .setStyle(new Notification.BigTextStyle().bigText(remoteMessage.getData().get("title")))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .build();
            }
            //Generate Diff Notification
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            notificationManager.notify(m, notification);
            Log.d(TAG, "Notification displayed with ID: " + m);
            
            // Riproduci sempre il suono direttamente per garantire che venga sentito
            // Questo è importante perché anche se il canale ha il suono configurato,
            // riprodurlo direttamente garantisce che venga sempre sentito
            playNotificationSound();
            Log.d(TAG, "========== Notification processing completed ==========");
        } else {
            Log.d(TAG, "Notification not needed (needNotification = false)");
        }

    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + 
                    getApplicationContext().getPackageName() + "/" + R.raw.notify);
            
            Log.d(TAG, "Playing notification sound: " + defaultSoundUri);
            Log.d(TAG, "App state when playing sound: " + (isAppInForeground() ? "FOREGROUND" : "BACKGROUND"));
            
            // Prova prima con Ringtone (più semplice)
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
            if (r != null) {
                // Imposta il tipo di stream per le notifiche
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r.setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED) // Forza la riproduzione anche in background
                            .build());
                }
                r.play();
                Log.d(TAG, "Notification sound played successfully via Ringtone");
                
                // Verifica che il suono sia effettivamente in riproduzione
                // (Ringtone.isPlaying() non è disponibile, ma possiamo loggare)
                Log.d(TAG, "Ringtone playback initiated");
            } else {
                Log.w(TAG, "Could not play notification sound - ringtone is null");
                Log.w(TAG, "Sound URI: " + defaultSoundUri);
                Log.w(TAG, "Package name: " + getApplicationContext().getPackageName());
                Log.w(TAG, "Resource ID: " + R.raw.notify);
                
                // Fallback: usa il suono di default del sistema
                try {
                    Uri defaultRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Log.d(TAG, "Trying fallback sound: " + defaultRingtone);
                    Ringtone fallbackRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtone);
                    if (fallbackRingtone != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            fallbackRingtone.setAudioAttributes(new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                                    .build());
                        }
                        fallbackRingtone.play();
                        Log.d(TAG, "Notification sound played via fallback (system default)");
                    } else {
                        Log.e(TAG, "Fallback ringtone is also null!");
                    }
                } catch (Exception fallbackException) {
                    Log.e(TAG, "Error playing fallback notification sound", fallbackException);
                    fallbackException.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification sound", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica se l'app è in foreground
     */
    private boolean isAppInForeground() {
        try {
            android.app.ActivityManager.RunningAppProcessInfo appProcessInfo = 
                    new android.app.ActivityManager.RunningAppProcessInfo();
            android.app.ActivityManager.getMyMemoryState(appProcessInfo);
            return (appProcessInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        } catch (Exception e) {
            Log.e(TAG, "Error checking app state", e);
            return false;
        }
    }
}
