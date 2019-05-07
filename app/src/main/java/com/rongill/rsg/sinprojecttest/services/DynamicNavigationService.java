package com.rongill.rsg.sinprojecttest.services;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.Compass;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.navigation.MyBleScanner;
import com.rongill.rsg.sinprojecttest.navigation.Point;
import com.rongill.rsg.sinprojecttest.navigation.StaticIndoorNavigation;

public class DynamicNavigationService extends Service {

    private final String TAG = "DynamicNavService";
    private Location destination;
    private Compass compass;
    private User currentUser;
    private StaticIndoorNavigation staticIndoorNavigation;
    private boolean navigationStarted = false;
    private String navigationLogPushKey;
    private MyBleScanner myBleScanner;

    //TODO need to stop the navigation if this or remote users are disconnected, at both ends.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // get the current user, message and the compass for the navigation.
        currentUser = (User) intent.getSerializableExtra("CURRENT_USER");
        RequestMessage navigationRequestMessage = (RequestMessage) intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");
        compass = (Compass)intent.getSerializableExtra("COMPASS");
        navigationLogPushKey = intent.getStringExtra("NAVIGATION_LOG_KEY");


        initDestination(navigationRequestMessage.getSenderUid());


        return super.onStartCommand(intent, flags, startId);

    }

    //set destination with a UID
    private void initDestination(String friendUserId) {

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
                    staticIndoorNavigation.stopNavigation();
                    stopSelf();
                    Log.i(TAG, "Dynamic navigation stopped-remote user has disconnected");
                } else { //TODO bug found, this will accrue every time the data changes at the user table (beacon or connection status)
                    destination.setName(dataSnapshot.child("username").getValue().toString());
                    destination.setCategory("friend");
                    //TODO need to make sure that the below is readable from DB
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
                        staticIndoorNavigation = new StaticIndoorNavigation(getBaseContext(), currentUser, destination, compass, navigationLogPushKey);
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
