package com.app.bemyrider.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.squareup.picasso.Transformation;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ExifTransformation implements Transformation {
    private static final String[] CONTENT_ORIENTATION = new String[] {
            MediaStore.Images.ImageColumns.ORIENTATION
    };

    final Context context;
    final Uri uri;

    public ExifTransformation(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return source;

        int exifRotation = getExifOrientation(context, uri);
        if (exifRotation != 0) {
            Matrix matrix = new Matrix();
            matrix.preRotate(exifRotation);

            Bitmap rotated =
                    Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            if (rotated != source) {
                source.recycle();
            }
            return rotated;
        }

        return source;
    }

    @Override
    public String key() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return "documentTransform()";
        return "documentExifTransform(" + uri.toString() + ")";
    }

    public static int getExifOrientation(Context context, Uri photoUri) {
        try {

            Cursor cursor = context.getContentResolver().query(photoUri, CONTENT_ORIENTATION, null, null, null);

            if (cursor != null && cursor.getCount() != 1) {
                return -1;
            }

            cursor.moveToFirst();
            return cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}