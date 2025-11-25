package com.app.bemyrider.model;

import com.google.gson.annotations.SerializedName;

public class MessageListDetailPojoItem {

    @SerializedName("to_user")
    private String toUser;

    @SerializedName("isRead")
    private String isRead;

    @SerializedName("message_id")
    private String messageId;

    @SerializedName("from_user")
    private String fromUser;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("msgType")
    private String msgType;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("appAttUrl")
    private String appAttUrl;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getAppAttUrl() {
        return appAttUrl;
    }

    public void setAppAttUrl(String appAttUrl) {
        this.appAttUrl = appAttUrl;
    }

    @Override
    public String toString() {
        return
                "MessageDetailPojoItem{" +
                        "to_user = '" + toUser + '\'' +
                        ",isRead = '" + isRead + '\'' +
                        ",message_id = '" + messageId + '\'' +
                        ",from_user = '" + fromUser + '\'' +
                        ",message_text = '" + messageText + '\'' +
                        ",created_date = '" + createdDate + '\'' +
                        "}";
    }
}