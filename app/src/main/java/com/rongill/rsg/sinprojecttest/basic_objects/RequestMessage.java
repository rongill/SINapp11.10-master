package com.rongill.rsg.sinprojecttest.basic_objects;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

public class RequestMessage implements Serializable {


    private String receiverUid;
    private String senderUid;
    private String senderUsername;
    private String requestType;
    private String requestStatus;
    private MyCalendar dateCreated;

    public RequestMessage(String receiverUid, String senderUid, String senderUsername, String requestType, String requestStatus){
        this.receiverUid = receiverUid;
        this.senderUid = senderUid;
        this.senderUsername = senderUsername;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
        this.dateCreated = new MyCalendar();
    }

    public MyCalendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(MyCalendar dateCreated) {
        this.dateCreated = dateCreated;
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

    public void sendRequest(RequestMessage message){
        DatabaseReference receiverInboxRef = FirebaseDatabase.getInstance().getReference().
                child("users-inbox").child(message.getReceiverUid());

        receiverInboxRef.push().setValue(message);
    }
}
