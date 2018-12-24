package com.rongill.rsg.sinprojecttest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.adapters.InboxAdapter;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;

import java.util.ArrayList;

public class InboxActivity extends Activity {
    DatabaseReference userInboxRef;
    private ArrayList<RequestMessage> inbox;
    private InboxAdapter inboxAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        getWindow().setLayout((int)(metrics.widthPixels*.8),(int)(metrics.heightPixels*0.6));

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        inbox = new ArrayList<>();
        userInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(mFirebaseAuth.getUid());

        inboxAdapter = new InboxAdapter(new ArrayList<RequestMessage>(),userInboxRef,this);
        ListView inboxListview = (ListView) findViewById(R.id.inbox_listView);
        inboxListview.setAdapter(inboxAdapter);

        setInbox();

    }

    private void setInbox(){
        userInboxRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);
                inbox.add(tempMessage);
                inboxAdapter.clear();
                inboxAdapter.addAll(inbox);
                inboxAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO if needed, add function here to handle changes in inbox DB
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);
                //inboxAdapter.clear();
                //inboxAdapter.addAll(inbox);
                //inboxAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
