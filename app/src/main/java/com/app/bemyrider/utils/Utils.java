package com.app.bemyrider.utils;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import com.app.bemyrider.helper.LogMaster;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nct58 on 20/6/17.
 */

public class Utils {

    public static final String CATEGORY_ID = "categoryId";
    public static final String PROVIDER_ID = "providerId";
    public static final String PROVIDER_SERVICE_ID = "providerServiceId"; // Added for Deep Link Redirection

    public static boolean clearAppCache(Context context) {
        try {
            boolean isClear = deleteDir(context.getCacheDir());
            LogMaster.e("ZZZ", "isCacheClear:" + isClear);
            return isClear;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * clear cache dir of picture which is taken photo from camera
     */
    public static boolean clearCameraCache(Context mContext) {
        try {
            File tmp = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            boolean isClear = deleteDir(tmp);
            LogMaster.e("ZZZ", "clearCameraCache:" + isClear);
            return isClear;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isNullOrEmpty(String str) {
        if (str != null && !str.isEmpty())
            return false;
        return true;
    }

    public static void cancelAsyncTask(AsyncTask asyncTask) {
        try {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encodeEmoji(String message) {
        try {
            return URLEncoder.encode(message,
                    "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }

    public static String decodeEmoji(String message) {
        try {
            return URLDecoder.decode(
                    message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }

    // convert internal Java String format to UTF-8
    public static String convertStringToUTF8(String s) {
        try {
            return URLEncoder.encode(s,
                    "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static String convertUTF8ToString(String s) {
        try {
            return URLDecoder.decode(
                    s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }


    public static InputFilter EMOJI_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {

            int type = Character.getType(source.charAt(index));

            if (type == Character.SURROGATE || type == Character.NON_SPACING_MARK
                    || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };


    public static String openCamera(Context mContext, ActivityResultLauncher<Uri> actResCamera) {
        File file;
        String selectedImagePath = "";
        try {
            file = createTempFileInAppPackage(mContext);
            selectedImagePath = "file:" + file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return selectedImagePath;
        }
        Uri uri;
        if (SDK_INT >= Build.VERSION_CODES.N)
            uri = FileProvider.getUriForFile(mContext, mContext.getPackageName().concat(".provider"), file);
        else
            uri = Uri.fromFile(file);

        actResCamera.launch(uri);
        return selectedImagePath;
    }

    public static void openImagesDocument(ActivityResultLauncher<Intent> actResGallery) {
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");  // 1
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);  // 2
        String[] mimeTypes = new String[]{"image/jpeg", "image/png"};  // 3
        pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        actResGallery.launch(Intent.createChooser(pictureIntent, "Select Picture"));
    }

    public static File createTempFileInAppPackage(Context mContext) throws IOException {
//        String imgFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("Dir exists");
        else
            System.out.println("Dir not exists");
        File file = File.createTempFile("img_", ".jpg", storageDir);
//        selectedImagePath = "file:" + file.getAbsolutePath();
        return file;
    }



}
