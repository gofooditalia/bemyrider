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

import com.app.bemyrider.activity.SignupActivity;
import com.app.bemyrider.activity.partner.Partner_DisputeDetail_Activity;
import com.app.bemyrider.activity.partner.Partner_ServiceRequestDetail_Tablayout_Activity;
import com.app.bemyrider.activity.partner.PartnerServiceRequestDetailsActivity;
import com.app.bemyrider.activity.user.BookedServiceDetailActivity;
import com.app.bemyrider.activity.user.MessageDetailActivity;

import com.app.bemyrider.R;
import com.app.bemyrider.model.EventBusMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * Created by nct33 on 13/9/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onNewToken(String s) {
        PrefsUtil.with(getApplicationContext()).write("device_token", s);
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        boolean needNotification = true;
        Log.e("WithoutBalancePojoItem", "Received");

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

        PendingIntent pendingIntent = null;
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
        else if (type.equals("s") && userType.equals("p")) {
            if (notificationConstant.equalsIgnoreCase("AC_NT_NOTIFY_ME_WHEN_SERVICE_COMPLETED")) {
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

                NotificationChannel mChannel = new NotificationChannel(Ch_id, name, importance);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                mChannel.setSound(defaultSoundUri, audioAttributes);
                notificationManager.createNotificationChannel(mChannel);

                // Create a notification and set the notification channel.
                /*.setSmallIcon(R.drawable.notify)*/
                notification = new Notification.Builder(this, Ch_id)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        /*.setContentText(remoteMessage.getData().get("title"))*/
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new Notification.BigTextStyle().bigText(remoteMessage.getData().get("title")))
                        .setChannelId(Ch_id)
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
            //playNotificationSound();
        }

    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri defaultSoundUri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.notify); //Here is FILE_NAME is the name of file that you want to play

            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
