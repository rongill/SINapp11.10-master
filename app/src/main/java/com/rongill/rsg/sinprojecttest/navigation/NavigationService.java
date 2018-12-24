package com.rongill.rsg.sinprojecttest.navigation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.MainDrowerActivity;

public class NavigationService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        // init the notification channel for the app (singleton).
        initNotificationChannel();
        //listener fot the user navigation log in db, when there is a
        navigationLogListener();
        
        
    }

    private void navigationLogListener() {
        DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                .child("user-navigation-log").child(FirebaseAuth.getInstance().getUid());
        userNavigationLogRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Intent intent = new Intent(getBaseContext(), MainDrowerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("DYNAMIC_NAV", dataSnapshot.child("sender-uid").getValue().toString());
                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), "SIN")
                        .setContentTitle("Dynamic Navigation")
                        .setContentText(dataSnapshot.child("sender-username").getValue().toString() + " wants to meet!")
                        .setSmallIcon(R.drawable.sinicon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                notificationManagerCompat.notify(0, notificationBuilder.build());
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
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("SIN",
                "SIN_NOTIFICATIONS",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("NAVIGATION_NOTIFICATIONS");
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
