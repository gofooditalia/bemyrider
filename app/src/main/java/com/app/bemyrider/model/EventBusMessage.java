package com.app.bemyrider.model;

import java.util.Map;

/**
 * Created by nct96 on 16/1/18.
 */

public class EventBusMessage {
    String type;
    Map<String, String> data;

    public EventBusMessage(String type, Map<String, String> remoteMessage) {
        this.type = type;
        this.data = remoteMessage;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getData() {
        return data;
    }
}
