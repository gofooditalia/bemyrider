package com.app.bemyrider.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.bemyrider.R;


/**
 * Author：Hardik Talaviya
 * Date：  2019.08.3 2:30 PM
 * Email： hardik.talaviya@ncrypted.com
 * Describe:
 */

public class PermissionManager {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS_SERVICE_CODE = 700;
    private static final int PERMS_REQUEST_CODE = 123;
    public static String SET_PREFERRED_APPLICATIONS = "android.permission.SET_PREFERRED_APPLICATIONS";
    public static String SET_PROCESS_LIMIT = "android.permission.SET_PROCESS_LIMIT";

    //region PUBLIC_DECLARATIONS
    public static String SET_TIME = "android.permission.SET_TIME";
    public static String SET_TIME_ZONE = "android.permission.SET_TIME_ZONE";
    public static String SET_WALLPAPER = "android.permission.SET_WALLPAPER";
    public static String SET_WALLPAPER_HINTS = "android.permission.SET_WALLPAPER_HINTS";
    public static String SIGNAL_PERSISTENT_PROCESSES = "android.permission.SIGNAL_PERSISTENT_PROCESSES";
    public static String STATUS_BAR = "android.permission.STATUS_BAR";
    public static String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    public static String TRANSMIT_IR = "android.permission.TRANSMIT_IR";
    public static String UNINSTALL_SHORTCUT = "com.android.launcher.permission.UNINSTALL_SHORTCUT";
    public static String UPDATE_DEVICE_STATS = "android.permission.UPDATE_DEVICE_STATS";
    public static String USE_FINGERPRINT = "android.permission.USE_FINGERPRINT";
    public static String USE_SIP = "android.permission.USE_SIP";
    public static String VIBRATE = "android.permission.VIBRATE";
    public static String WAKE_LOCK = "android.permission.WAKE_LOCK";
    public static String WRITE_APN_SETTINGS = "android.permission.WRITE_APN_SETTINGS";
    public static String READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    public static String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public static String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static String WRITE_GSERVICES = "android.permission.WRITE_GSERVICES";
    public static String WRITE_SECURE_SETTINGS = "android.permission.WRITE_SECURE_SETTINGS";
    public static String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    public static String WRITE_SYNC_SETTINGS = "android.permission.WRITE_SYNC_SETTINGS";
    public static String WRITE_VOICEMAIL = "com.android.voicemail.permission.WRITE_VOICEMAIL";
    public static String CAMERA = "android.permission.CAMERA";
    public static String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    public static String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public static String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    public static String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static String CALL_PHONE = "android.permission.CALL_PHONE";
    public static String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    public static String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    public static String BODY_SENSORS = "android.permission.BODY_SENSORS";
    public static String SEND_SMS = "android.permission.SEND_SMS";
    public static String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public static String READ_SMS = "android.permission.READ_SMS";
    public static String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    public static String RECEIVE_MMS = "android.permission.RECEIVE_MMS";
    public static int SET_PREFERRED_APPLICATIONS_SERVICE_CODE = 701;
    public static int SET_PROCESS_LIMIT_SERVICE_CODE = 702;
    public static int SET_TIME_SERVICE_CODE = 703;
    public static int SET_TIME_ZONE_SERVICE_CODE = 704;
    public static int SET_WALLPAPER_SERVICE_CODE = 705;
    public static int SET_WALLPAPER_HINTS_SERVICE_CODE = 706;
    public static int SIGNAL_PERSISTENT_PROCESSES_SERVICE_CODE = 707;
    public static int STATUS_BAR_SERVICE_CODE = 708;
    public static int SYSTEM_ALERT_WINDOW_SERVICE_CODE = 709;
    public static int TRANSMIT_IR_SERVICE_CODE = 710;
    public static int UNINSTALL_SHORTCUT_SERVICE_CODE = 711;
    public static int UPDATE_DEVICE_STATS_SERVICE_CODE = 712;
    public static int USE_FINGERPRINT_SERVICE_CODE = 713;
    public static int USE_SIP_SERVICE_CODE = 714;
    public static int VIBRATE_SERVICE_CODE = 715;
    public static int WAKE_LOCK_SERVICE_CODE = 716;
    public static int WRITE_APN_SETTINGS_SERVICE_CODE = 717;
    public static int READ_CALENDAR_SERVICE_CODE = 718;
    public static int WRITE_CALENDAR_SERVICE_CODE = 719;
    public static int WRITE_CALL_LOG_SERVICE_CODE = 720;
    public static int WRITE_CONTACTS_SERVICE_CODE = 721;
    public static int READ_EXTERNAL_STORAGE_SERVICE_CODE = 722;
    public static int WRITE_EXTERNAL_STORAGE_SERVICE_CODE = 723;
    public static int WRITE_GSERVICES_SERVICE_CODE = 724;
    public static int WRITE_SECURE_SETTINGS_SERVICE_CODE = 725;
    public static int WRITE_SETTINGS_SERVICE_CODE = 726;
    public static int WRITE_SYNC_SETTINGS_SERVICE_CODE = 727;
    public static int WRITE_VOICEMAIL_SERVICE_CODE = 728;
    public static int CAMERA_SERVICE_CODE = 729;
    public static int READ_CONTACTS_SERVICE_CODE = 730;
    public static int GET_ACCOUNTS_SERVICE_CODE = 731;
    public static int ACCESS_FINE_LOCATION_SERVICE_CODE = 732;
    public static int ACCESS_COARSE_LOCATION_SERVICE_CODE = 733;
    public static int RECORD_AUDIO_SERVICE_CODE = 734;
    public static int READ_PHONE_STATE_SERVICE_CODE = 735;
    public static int CALL_PHONE_SERVICE_CODE = 736;
    public static int READ_CALL_LOG_SERVICE_CODE = 737;
    public static int ADD_VOICEMAIL_SERVICE_CODE = 738;
    public static int PROCESS_OUTGOING_CALLS_SERVICE_CODE = 739;
    public static int BODY_SENSORS_SERVICE_CODE = 740;
    public static int SEND_SMS_SERVICE_CODE = 741;
    public static int RECEIVE_SMS_SERVICE_CODE = 742;
    public static int READ_SMS_SERVICE_CODE = 743;
    public static int RECEIVE_WAP_PUSH_SERVICE_CODE = 744;
    public static int RECEIVE_MMS_SERVICE_CODE = 745;
    Context context;
    private String continueCode = "";
    public PermissionManager(Context context) {
        this.context = context;
    }

    //Method which checks whether or not permission is allowed
    public boolean isPermissionAllowed(String permission) {
        int permissionToCheck = ContextCompat.checkSelfPermission(context, permission);

        if (permissionToCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(23)
    public boolean checkAndRequestPermissions(String permission, int serviceCode, String continueCode) {
        int permissionToGrantValue = ContextCompat.checkSelfPermission(context, permission);
        if (permissionToGrantValue != PackageManager.PERMISSION_GRANTED) {
            String[] stringArray = new String[]{permission};
            ActivityCompat.requestPermissions((Activity) context, stringArray, serviceCode);
            this.continueCode = continueCode;
            return false;
        }
        return true;
    }


    public boolean onRequestPermissionResult(int requestCode, String permissions, int[] grantResults) {
        boolean isGranted = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(context, "permission granted", Toast.LENGTH_SHORT).show();
            isGranted = true;

        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(((Activity) context), permissions)) {
                ((Activity) context).finish();
                Toast.makeText(context, context.getResources().getString(R.string.enable_permission_from_setting), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } /*else {
                Toast.makeText(context, context.getResources().getString(R.string.cancel_permissions), Toast.LENGTH_LONG).show();
            }*/
            isGranted = false;
        }
        return isGranted;
    }

    public String currentCodePos() {
        return continueCode;
    }

}
