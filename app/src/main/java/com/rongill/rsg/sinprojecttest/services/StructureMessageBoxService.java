package com.rongill.rsg.sinprojecttest.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.SinMainActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;

public class StructureMessageBoxService extends Service {

    private String structureName;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        structureName = intent.getStringExtra("STRUCTURE");
        DatabaseReference structureMessageRef = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(structureName).child("management-notifications");


        structureMessageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyCalendar postedDate = dataSnapshot.child("date-posted").getValue(MyCalendar.class);
                int timeDiff = postedDate.timeDiffInSeconds(new MyCalendar());
                if(timeDiff<1000){

                    Intent intent = new Intent(getBaseContext(), SinMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(),
                            "SIN")
                            .setContentTitle(structureName + ": Management message")
                            .setContentText(dataSnapshot.child("message").getValue().toString())
                            .setSmallIcon(R.drawable.sinicon)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pi)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                    notificationManagerCompat.notify(0, notificationBuilder.build());
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
