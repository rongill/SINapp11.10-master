package com.rongill.rsg.sinprojecttest.basic_objects;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;

public class Inbox implements Serializable {

    private ArrayList<RequestMessage> messages = new ArrayList<>();
    private DatabaseReference inboxRef;

    public Inbox(){}

    public Inbox(ArrayList<RequestMessage> messages, DatabaseReference inboxRef){
        this.messages = messages;
        this.inboxRef = inboxRef;
    }

    public ArrayList<RequestMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<RequestMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(RequestMessage message){
        this.messages.add(message);
    }

    public DatabaseReference getInboxRef() {
        return inboxRef;
    }

    public void setInboxRef(DatabaseReference inboxRef) {
        this.inboxRef = inboxRef;
    }
}
