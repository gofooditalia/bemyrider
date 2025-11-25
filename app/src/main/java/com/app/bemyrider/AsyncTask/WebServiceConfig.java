package com.app.bemyrider.AsyncTask;

/**
 * Created by nct96 on 21/7/17.
 */
@SuppressWarnings("unused")
public class WebServiceConfig {
    public int CONNECTION_TIMEOUT_MILLISECONDS = 60000;
    public int READ_TIMEOUT_MILLISECONDS = 60000;
    public static final String PROGRESS_DIALOG_TITLE = "Loading...";
    public static final String PROGRESS_DIALOG_MESSAGE = "Please Wait While Loading";
    public static final String UNEXPECTED_ERROR = "Unexpected error has occurred";
    public static final String INTERNET_ERROR = "No Internet Connection";
    public static final String CONNECTION_TIMEOUT_ERROR = "Connection has timed out. Do you want to retry?";

    public WebServiceConfig() {
        CONNECTION_TIMEOUT_MILLISECONDS = 60000;
        READ_TIMEOUT_MILLISECONDS = 60000;
    }

    public WebServiceConfig(int CONNECTION_TIMEOUT_SECONDS, int READ_TIMEOUT_SECONDS) {
        this.CONNECTION_TIMEOUT_MILLISECONDS = CONNECTION_TIMEOUT_SECONDS;
        this.READ_TIMEOUT_MILLISECONDS = READ_TIMEOUT_SECONDS;
    }

    public WebServiceConfig(int CONNECTION_TIMEOUT_SECONDS) {
        this.CONNECTION_TIMEOUT_MILLISECONDS = CONNECTION_TIMEOUT_SECONDS;
        this.READ_TIMEOUT_MILLISECONDS = CONNECTION_TIMEOUT_SECONDS;
    }
}
