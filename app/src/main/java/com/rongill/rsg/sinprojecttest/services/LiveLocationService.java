package com.rongill.rsg.sinprojecttest.services;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.SinMainActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.MyBleScanner;

public class LiveLocationService extends Service {
    private final String TAG = "DynamicNavService";
    private User currentUser;
    MyBleScanner myBleScanner;
    public boolean isSharing = true;

    public LiveLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUser = (User)intent.getSerializableExtra("CURRENT_USER");
        RequestMessage message = (RequestMessage)intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");
        String friendNavPushKey = intent.getStringExtra("FRIEND_NAV_LOG_PUSHKEY");
        String userNavigationPushkey = intent.getStringExtra("NAVIGATION_PUSHKEY");


        myBleScanner = new MyBleScanner((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        myBleScanner.initLeScan(scanCallback, true);

        final DatabaseReference userNavLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId()).child(userNavigationPushkey).child("status");

        DatabaseReference friendNavLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(message.getReceiverUid()).child(friendNavPushKey);

        //sharing the nav log key so that the friend user can listen to any changes in the sharing process.
        friendNavLogRef.child("live-sharing-token").setValue(userNavigationPushkey);

        //update this user navigation/sharing log according to friend user navigation process.
        friendNavLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    switch (dataSnapshot.child("status").getValue().toString()){
                        case "stopped":
                            isSharing = false;
                            myBleScanner.initLeScan(scanCallback, false);
                            userNavLogRef.setValue("stopped by friend").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    makeNotification("friend stopped the navigation, Live location sharing disabled.");

                                    DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(currentUser.getUserId()).child("status");
                                    userStatusRef.setValue("connected");

                                    LiveLocationService.this.stopSelf();
                                }
                            });

                            break;

                        case "arrived":
                            isSharing = false;
                            myBleScanner.initLeScan(scanCallback, false);
                            userNavLogRef.setValue("navigation complete!").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    makeNotification("Your friend is nearby! have a look around.");

                                    DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(currentUser.getUserId()).child("status");
                                    userStatusRef.setValue("connected");

                                    LiveLocationService.this.stopSelf();
                                }
                            });
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isSharing) {
                    myBleScanner.initLeScan(scanCallback, false);
                    userNavLogRef.setValue("time-elapsed").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            makeNotification("sharing time elapsed, SIN has stopped sharing you location.");
                            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(currentUser.getUserId()).child("status");
                            userStatusRef.setValue("connected");
                        }
                    });
                }
            }
        };

        //will stop the scan after 10min, will update the users log accordingly
        handler.postDelayed(runnable, 600000);


        return super.onStartCommand(intent, flags, startId);
    }


    private void makeNotification(String message){
        Intent intent = new Intent(getBaseContext(), SinMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(),"SIN")
                .setContentTitle("SIN - Dynamic Navigation")
                .setContentText(message)
                .setSmallIcon(R.drawable.sinicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        //TODO not canceling the noti. when pressed

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
        notificationManagerCompat.notify(3, notificationBuilder.build());

        //update user status to navigating, will prevent multiple navigation sessions.
        DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUser.getUserId()).child("navigation-status");
        userStatusRef.setValue("idle");
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")){
                MyBeacon scannedBeacon = new MyBeacon();
                scannedBeacon.setMACaddress(result.getDevice().getAddress());
                scannedBeacon.setName(result.getDevice().getName());
                scannedBeacon.setRssi(result.getRssi());

                myBleScanner.setBeaconDataFromServer(scannedBeacon);
                currentUser.setCurrentBeacon(myBleScanner.getNearestBeacon());
            }
        }
    };



    @Override
    public IBinder onBind(Intent intent) {
      //  throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
