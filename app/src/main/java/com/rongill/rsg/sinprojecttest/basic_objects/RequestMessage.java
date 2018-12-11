package com.rongill.rsg.sinprojecttest.basic_objects;

import java.io.Serializable;

public class RequestMessage implements Serializable {


    private String receiverUid;
    private String senderUid;
    private String senderUsername;
    private String requestType;
    private String requestStatus;

    public RequestMessage(String receiverUid, String senderUid, String senderUsername, String requestType, String requestStatus){
        this.receiverUid = receiverUid;
        this.senderUid = senderUid;
        this.senderUsername = senderUsername;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
    }

    public RequestMessage(){}

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
