package com.rongill.rsg.sinprojecttest;

import java.io.Serializable;

public class RequestMessage implements Serializable {

    private String friendUid;
    private String requestType;
    private String senderUsername;
    boolean requestStatus;

    public RequestMessage(String friendUid, String requestType, String senderUsername, boolean requestStatus){
        this.friendUid = friendUid;
        this.senderUsername = senderUsername;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
    }

    public RequestMessage(){}

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public boolean isRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(boolean requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
