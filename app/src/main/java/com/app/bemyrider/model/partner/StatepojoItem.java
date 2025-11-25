package com.app.bemyrider.model.partner;

import com.google.gson.annotations.SerializedName;

public class StatepojoItem {

    @SerializedName("stateName")
    private String stateName;

    @SerializedName("StateID")
    private String stateID;

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStateID() {
        return stateID;
    }

    public void setStateID(String stateID) {
        this.stateID = stateID;
    }

    @Override
    public String toString() {
        return stateName.toString();
    }
}