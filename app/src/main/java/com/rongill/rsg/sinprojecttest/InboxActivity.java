package com.rongill.rsg.sinprojecttest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class InboxActivity extends Activity {
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

        Intent intent = getIntent();
        inbox = (ArrayList<RequestMessage>) intent.getSerializableExtra("MASSAGE_LIST");
        inboxAdapter = new InboxAdapter(inbox,this);
        inboxListview = (ListView)findViewById(R.id.inbox_listView);
        inboxListview.setAdapter(inboxAdapter);

    }
}
