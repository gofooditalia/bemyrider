package com.app.bemyrider.model.partner;

/**
 * Created by nct121 on 11/20/2015.
 */
public class Events {
    String date;
    String id;

    public Events(String date, String id) {
        this.date = date;
        this.id = id;
    }

    public Events() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
