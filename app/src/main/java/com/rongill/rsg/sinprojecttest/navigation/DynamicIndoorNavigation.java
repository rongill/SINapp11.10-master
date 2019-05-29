package com.rongill.rsg.sinprojecttest.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.SinMainActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

public class DynamicIndoorNavigation {

    private final String TAG = "DynamicNavService";
    private Location destination;
    private Compass compass;
    private User currentUser;
    private boolean navigationStarted = false;
    private String logKey;
    private RequestMessage navRequestMessage;
    private StaticIndoorNavigation staticIndoorNavigation;
    private Context context;

    public DynamicIndoorNavigation(Context context, User currentUser, RequestMessage navRequestMessage, Compass compass, String logKey){
        this.currentUser = currentUser;
        this.compass = compass;
        this.logKey = logKey;
        this.navRequestMessage = navRequestMessage;
        this.context = context;

        utilizeSharingToken(navRequestMessage.getSenderUid());
        initDestination(navRequestMessage.getSenderUid());
    }

    public void initDestination(String friendUserId) {

        destination = new Location();

        DatabaseReference destinationUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friendUserId);

        destinationUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //remote user data can change only the status & beacon.
                //if remote user is disconnected, stop navigation service and log in server.
                //if beacon is changed, init the beacon data to this destination.
                if(dataSnapshot.child("status").toString().equals("disconnected")) {
                    compass.getUserLocationTv().setText("remote user has disconnected from server");
                    staticIndoorNavigation.stopNavigation("friend DC, mid session");
                    Log.i(TAG, "Dynamic navigation stopped-remote user has disconnected");
                } else { //TODO maybe change the listener to the nav log, for that will need to update the beacon in the nav log.
                    destination.setName(dataSnapshot.child("username").getValue().toString());
                    destination.setCategory("friend");
                    destination.setBeaconName(dataSnapshot.child("beacon").getValue().toString());
                    readBeaconData(destination.getBeaconName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //read the beacon data to set in the destination var, afterwards, start the navigation UC.
    private void readBeaconData(String beaconName) {
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");
        Query beaconQuery = beaconRef.orderByChild("name").equalTo(beaconName);
        beaconQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    destination.setCoordinates(new Point(Integer.parseInt(ds.child("x").getValue().toString()),
                            Integer.parseInt(ds.child("y").getValue().toString())));
                    destination.setStructure(ds.child("structure").getValue().toString());
                    destination.setFloor(ds.child("floor").getValue().toString());

                    //if this is the first time read, start the navigation, otherwise only update the destination as above.
                    if(!navigationStarted){
                        navigationStarted = true;
                        staticIndoorNavigation = new StaticIndoorNavigation(context, currentUser, destination, compass, logKey);
                        staticIndoorNavigation.startNavigation();
                    }

                    else
                        staticIndoorNavigation.setDestination(destination);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void utilizeSharingToken(final String friendUid){

        DatabaseReference userNavLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log")
                .child(currentUser.getUserId())
                .child(logKey)
                .child("live-sharing-token");
        userNavLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String friendNavToken = dataSnapshot.getValue().toString();
                    sharingStatusListener(friendUid, friendNavToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sharingStatusListener(String friendUid, String friendNavToken){

        DatabaseReference friendSharingLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log")
                .child(friendUid)
                .child(friendNavToken)
                .child("status");
        friendSharingLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    if(dataSnapshot.getValue().toString().equals("sharing-stopped")){
                    //TODO stop the dyn nav and update DB
                    staticIndoorNavigation.stopNavigation("stopped by friend.");
                    DatabaseReference userNavLogstatusRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-navigation-log").child(currentUser.getUserId()).child(logKey)
                    .child("status");
                    userNavLogstatusRef.setValue("stopped sharing").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            makeNotification("Dynamic navigation stopped, friend stopped sharing location.");
                        }
                    });
                    //update user status to navigating, will prevent multiple navigation sessions.
                    DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(currentUser.getUserId()).child("status");
                    userStatusRef.setValue("connected");

                } else if(dataSnapshot.getValue().toString().equals("time-elapsed")){
                        staticIndoorNavigation.stopNavigation("navigation time elapsed.");
                        DatabaseReference userNavLogstatusRef = FirebaseDatabase.getInstance().getReference()
                                .child("users-navigation-log").child(currentUser.getUserId()).child(logKey)
                                .child("status");
                        userNavLogstatusRef.setValue("sharing time elapsed").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                makeNotification("Dynamic navigation stopped, navigation time elapsed.");
                            }
                        });
                        //update user status to navigating, will prevent multiple navigation sessions.
                        DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(currentUser.getUserId()).child("status");
                        userStatusRef.setValue("connected");
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeNotification(String message){
        Intent intent = new Intent(context, SinMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,"SIN")
                .setContentTitle("SIN - Dynamic Navigation")
                .setContentText(message)
                .setSmallIcon(R.drawable.sinicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        //TODO not canceling the noti. when pressed

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(3, notificationBuilder.build());
    }
}
