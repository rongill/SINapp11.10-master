package com.rongill.rsg.sinprojecttest.app_utilities;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.Inbox;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;

import java.util.ArrayList;

public class InboxUtil {

    private FirebaseUser mFirebaseUser;
    private Inbox userInbox;
    private ArrayList<RequestMessage> pendingRequests = new ArrayList<>();
    private ArrayList<RequestMessage> confirmedRequests = new ArrayList<>();
    private ArrayList<RequestMessage> deniedRequests = new ArrayList<>();

    public InboxUtil(DatabaseReference inboxRef){
        userInbox = new Inbox(new ArrayList<RequestMessage>(),inboxRef);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setInboxAndListener();
        //getActiveRequestsFromDb();

    }

    public Inbox getUserInbox() {
        return userInbox;
    }

    public void setUserInbox(Inbox userInbox) {
        this.userInbox = userInbox;
    }

    private void setInboxAndListener(){

        userInbox.getInboxRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(userInbox.getMessages()!=null)
                    userInbox.getMessages().clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    RequestMessage tempMessage = new RequestMessage();
                    tempMessage.setReceiverUid(ds.getValue(RequestMessage.class).getReceiverUid());
                    tempMessage.setSenderUid(ds.getValue(RequestMessage.class).getSenderUid());
                    tempMessage.setSenderUsername(ds.getValue(RequestMessage.class).getSenderUsername());
                    tempMessage.setRequestType(ds.getValue(RequestMessage.class).getRequestType());
                    tempMessage.setRequestStatus(ds.getValue(RequestMessage.class).getRequestStatus());
                    userInbox.addMessage(tempMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public RequestMessage setRequestMessage(String receiverUid, String userId, String username, String requestType ){
        return new RequestMessage(receiverUid,userId,username,requestType,"pending");
    }

    public void sendRequest(RequestMessage message){
        DatabaseReference receiverInboxRef = FirebaseDatabase.getInstance().getReference().
                child("users-inbox").child(message.getReceiverUid());

        receiverInboxRef.push().setValue(message);
    }

    //TODO no need for this method, for now
    /*
    private void getActiveRequestsFromDb(){
        DatabaseReference usersInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox");
        Query pendingRequestQuery = usersInboxRef.orderByChild("senderUid").equalTo(mFirebaseUser.getUid());
        pendingRequestQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getValue(RequestMessage.class).getRequestStatus().equals("pending")){
                        pendingRequests.add(ds.getValue(RequestMessage.class));
                    } else if(ds.getValue(RequestMessage.class).getRequestStatus().equals("confirmed")) {
                        confirmedRequests.add(ds.getValue(RequestMessage.class));
                    }else {
                        deniedRequests.add(ds.getValue(RequestMessage.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
