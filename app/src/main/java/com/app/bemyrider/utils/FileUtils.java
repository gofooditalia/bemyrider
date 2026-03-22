package com.app.bemyrider.utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.bemyrider.model.FileUtilPOJO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FileUtils {

    public static final String DOCUMENTS_DIR = "documents";

    /**
     * Modernized getPath for Android 11+ compatibility.
     * Copies the content of the Uri to a temporary file in the app's cache directory.
     */
    public static FileUtilPOJO getPath(Context context, Uri uri) {
        if (uri == null) return null;

        // Google Drive check
        if (isGoogleDriveDocument(uri)) {
            String fileName = getFileName(context, uri);
            String ext = getFileExention(context, uri);
            return new FileUtilPOJO(fileName + "," + ext + "," + uri, true);
        }

        String fileName = getFileName(context, uri);
        if (fileName == null) {
            fileName = "temp_file_" + System.currentTimeMillis();
        }

        File cacheDir = getDocumentCacheDir(context);
        File tempFile = new File(cacheDir, fileName);

        // Handle name collision
        if (tempFile.exists()) {
             String baseName = fileName;
             String extension = "";
             int dotIndex = fileName.lastIndexOf('.');
             if (dotIndex > 0) {
                 baseName = fileName.substring(0, dotIndex);
                 extension = fileName.substring(dotIndex);
             }
             int index = 1;
             while (tempFile.exists()) {
                 tempFile = new File(cacheDir, baseName + "(" + index + ")" + extension);
                 index++;
             }
        }

        try {
            if (copyFileFromUri(context, uri, tempFile)) {
                return new FileUtilPOJO(tempFile.getAbsolutePath(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean copyFileFromUri(Context context, Uri uri, File destFile) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isGoogleDriveDocument(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String filename = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && returnCursor.moveToFirst()) {
                    filename = returnCursor.getString(nameIndex);
                }
                returnCursor.close();
            }
        }
        if (filename == null) {
            filename = uri.getPath();
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }
        }
        return filename;
    }

    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getFileExention(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }

    public static void downloadFile(Context mContext, String url, String fileName, String extension){
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("Downloading");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDescription("Downloading File");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + extension);
        downloadManager.enqueue(request);
    }
}
