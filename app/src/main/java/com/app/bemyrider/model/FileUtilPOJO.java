package com.app.bemyrider.model;

public class FileUtilPOJO {

    String path = "";
    boolean requiredDownload = false;

    public FileUtilPOJO(String path, boolean requiredDownload){
        this.path = path;
        this.requiredDownload = requiredDownload;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean isRequiredDownload() {
        return requiredDownload;
    }

    public void setRequiredDownload(boolean requiredDownload) {
        this.requiredDownload = requiredDownload;
    }
}
