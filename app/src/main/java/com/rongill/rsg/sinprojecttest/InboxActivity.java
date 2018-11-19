package com.rongill.rsg.sinprojecttest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class InboxActivity extends Activity {
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<RequestMessage> inbox;
    private InboxAdapter inboxAdapter;
    private ListView inboxListview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        getWindow().setLayout((int)(metrics.widthPixels*.8),(int)(metrics.heightPixels*0.6));
        mFirebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        inbox = (ArrayList<RequestMessage>) intent.getSerializableExtra("MESSAGES");
        DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(mFirebaseAuth.getUid());

        inboxAdapter = new InboxAdapter(inbox,userInboxRef,this);
        inboxListview = (ListView)findViewById(R.id.inbox_listView);
        inboxListview.setAdapter(inboxAdapter);


    }
}
