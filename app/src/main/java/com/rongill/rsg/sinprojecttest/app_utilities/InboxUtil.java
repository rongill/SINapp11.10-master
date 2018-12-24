package com.rongill.rsg.sinprojecttest.app_utilities;

import android.app.NotificationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

    //TODO add documentation
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

        userInbox.getInboxRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);
                userInbox.getMessages().add(tempMessage);
                //TODO here we can add a notification in main UI thread when new message arrives.
                if(tempMessage.getRequestType().equals("poke")){

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);
                for(RequestMessage temp : userInbox.getMessages()){
                    if(temp.getSenderUid().equals(tempMessage.getSenderUid())) {
                        userInbox.getMessages().set(userInbox.getMessages().indexOf(temp), tempMessage);
                    }
                }
                //TODO here we can add a notification when message status changes, such as request confirmed/denied etc. , Dynamic navigation confirmed status could start the sender navigation use-case, the receiver confirmed and will start navigating, the sender will get notified here and could start navigating also.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);
                userInbox.getMessages().remove(tempMessage);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
