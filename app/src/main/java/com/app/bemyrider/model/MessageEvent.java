package com.app.bemyrider.model;

/**
 * Created by nct96 on 13/12/16.
 */

public class MessageEvent {
    String type,message;

    public MessageEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
