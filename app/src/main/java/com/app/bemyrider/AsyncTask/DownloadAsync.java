package com.app.bemyrider.AsyncTask;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import com.app.bemyrider.utils.Log;

import androidx.annotation.RequiresApi;

import com.app.bemyrider.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadAsync extends AsyncTask<String, String, String> {
    private String FOLDER_PATH;

    private Context mContext;
    private Uri uri;
    private String fileName, ext, filePath;
    private Dialog mdialog;
    private OnDownloadListener mOnDownloadListener;

    public interface OnDownloadListener {
        void onResult(String result);
    }

    public DownloadAsync(Context mContext, Uri uri, String fileName, String ext,
                         OnDownloadListener mOnDownloadListener) {
        this.mContext = mContext;
        this.uri = uri;
        this.ext = ext;
        this.fileName = stripExtension(fileName);
        this.mOnDownloadListener = mOnDownloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mdialog = new Dialog(mContext);
        mdialog.setTitle("");
        mdialog.setCancelable(false);
        mdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mdialog.setContentView(R.layout.dialog_progress);
        mdialog.show();
    }

    @Override
    protected String doInBackground(String... URL) {
        try {
            filePath = writeStreamToFile(mContext, uri, ext);
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String file_url) {
        if (mdialog != null && mdialog.isShowing())
            mdialog.dismiss();
        mOnDownloadListener.onResult(filePath);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String writeStreamToFile(Context mContext, Uri mUri, String ext) {
        FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator
                + mContext.getString(R.string.app_name);
        InputStream inputStream = null;
        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) {
            boolean b = folder.mkdir();
            Log.e("RESULT", b + "");
            Log.d("MainActivity", ">> Let's debug why this directory isn't being created: ");
            Log.d("MainActivity", "Is it working?: " + folder.mkdirs());
            Log.d("MainActivity", "Does it exist?: " + folder.exists());
            Log.d("MainActivity", "What is the full URI?: " + folder.toURI());
            Log.d("MainActivity", "--");
            Log.d("MainActivity", "Can we write to this file?: " + folder.canWrite());
            if (!folder.canWrite()) {
                Log.d("MainActivity", ">> We can't write! Do we have WRITE_EXTERNAL_STORAGE permission?");
                if (mContext.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                    Log.d("MainActivity", ">> We don't have permission to write - please add it.");
                } else {
                    Log.d("MainActivity", "We do have permission - the problem lies elsewhere.");
                }
            }
            Log.d("MainActivity", "Are we even allowed to read this file?: " + folder.canRead());
            Log.d("MainActivity", "--");
            Log.d("MainActivity", ">> End of debugging.");

        }
        try {
            inputStream = mContext.getContentResolver().openInputStream(mUri);
//            File file = File.createTempFile(fileName, "." + ext, folder);
            File file = new File(FOLDER_PATH + "/" + fileName + "." + ext);

            if (!file.exists()) {
                boolean b = file.createNewFile();
                Log.e("RESULT", b + "");
            }

            try (OutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String stripExtension(final String s) {
        return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
    }
}
