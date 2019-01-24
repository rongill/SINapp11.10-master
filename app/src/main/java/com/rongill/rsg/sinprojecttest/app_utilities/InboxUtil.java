package com.rongill.rsg.sinprojecttest.app_utilities;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;


public class InboxUtil {

    //TODO can remove this class now, maybe add below methods to inbox.

    public InboxUtil(){}

    public RequestMessage setRequestMessage(String receiverUid, String userId, String username, String requestType ){
        return new RequestMessage(receiverUid,userId,username,requestType,"pending");
    }

    public void sendRequest(RequestMessage message){
        DatabaseReference receiverInboxRef = FirebaseDatabase.getInstance().getReference().
                child("users-inbox").child(message.getReceiverUid());

        receiverInboxRef.push().setValue(message);
    }

}
