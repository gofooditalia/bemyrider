package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageListDataPojo {

    @SerializedName("list")
    private List<MessageListPojoItem> messageList;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<MessageListPojoItem> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageListPojoItem> messageList) {
        this.messageList = messageList;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}