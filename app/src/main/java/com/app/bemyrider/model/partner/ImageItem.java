package com.app.bemyrider.model.partner;

import android.net.Uri;

import java.io.File;

public class ImageItem {
    //    private Bitmap image;
//    private String imagePath;
    private Uri imageStream;
    private File imageFile;
    private String imageName;

    public ImageItem(Uri imageStream, File imageFile, String imageName) {
        this.imageStream = imageStream;
        this.imageFile = imageFile;
        this.imageName = imageName;
    }

    public ImageItem(Uri imageStream) {
        this.imageStream = imageStream;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Uri getImageStream() {
        return imageStream;
    }

    public void setImageStream(Uri imageStream) {
        this.imageStream = imageStream;
    }

}