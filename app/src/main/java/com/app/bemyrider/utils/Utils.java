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

import com.app.bemyrider.AsyncTask.WebServiceCall;
import com.app.bemyrider.helper.LogMaster;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for general operations.
 * Optimized by Gemini - 2024.
 */
public class Utils {

    public static final String CATEGORY_ID = "categoryId";
    public static final String PROVIDER_ID = "providerId";
    public static final String PROVIDER_SERVICE_ID = "providerServiceId";

    public static InputFilter EMOJI_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {
            int type = Character.getType(source.charAt(index));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };

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
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Cancella task asincroni (AsyncTask o WebServiceCall).
     */
    public static void cancelAsyncTask(Object task) {
        try {
            if (task instanceof AsyncTask) {
                AsyncTask<?, ?, ?> asyncTask = (AsyncTask<?, ?, ?>) task;
                if (!asyncTask.isCancelled()) {
                    asyncTask.cancel(true);
                }
            } else if (task instanceof WebServiceCall) {
                WebServiceCall webServiceCall = (WebServiceCall) task;
                webServiceCall.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
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
            return URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }

    public static String decodeEmoji(String message) {
        try {
            return URLDecoder.decode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }

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
        pictureIntent.setType("image/*");
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
        pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        actResGallery.launch(Intent.createChooser(pictureIntent, "Select Picture"));
    }

    public static File createTempFileInAppPackage(Context mContext) throws IOException {
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("img_", ".jpg", storageDir);
    }
}
