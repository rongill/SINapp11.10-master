package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.le.BluetoothLeScanner;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

public class DynamicIndoorNavigation extends IndoorNavigation {

    private final String TAG = "DynamicIndoorNavigation";
    private MyBleScanner dynamicBleScanner;
    private boolean hasArrived = false;

    public DynamicIndoorNavigation(MyBleScanner myBleScanner, User currentUser, BluetoothLeScanner scanner, Compass compass) {
        super(currentUser, new Location(), scanner, compass);
        this.dynamicBleScanner = myBleScanner;
    }

    @Override
    public void startNavigation() {
        super.startNavigation();
        //init the bleScanner with the scanner from Main activity.
        dynamicBleScanner.setScanner(scanner);

        //start the LE scan, using the scanner from Main activity and the MyBleScanner scan callbacks.
        dynamicBleScanner.initLeScan(true);
        //this thread is executing a loop with a delay, to checks every 100 millis if the destination was initiated, if so will start the navigation.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasArrived){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    currentUser.setCurrentBeacon(dynamicBleScanner.getNearestBeacon());
                    if(currentUser.getCurrentBeacon().getName().equals(destination.getBeaconName())){
                        hasArrived = true;
                        dynamicBleScanner.initLeScan(false);
                        compass.compassImage.setRotation(90);
                        //TODO user has arrived to destination, create a message/notification and log in server.
                    } else {
                        //calc the direction based on current user location angle to the destination coordinates.
                        //set the float value to the directionAzimuth var in indoor navigation abstract class.
                        calcDirectionToDestination(currentUser.getCurrentBeacon().getCoordinates(), destination.getCoordinates());

                        //calc the distance to destination based on user current beacon
                        calcDistanceToDestination();

                        //rotate the image of the compass according to the directionAzimuth in degrees, calculated in the above method.
                        compass.compassImage.setRotation(90+directionAzimuth);

                        String distanceToDestination = "distance to " + destination.getName() + ":" + String.valueOf(distance);
                        compass.getUserLocationTv().setText(distanceToDestination);
                    }
                }
            }
        }).start();
    }

    public void stopNavigation(String navigationLogKey){
        dynamicBleScanner.initLeScan(false);
        compass.compassImage.setRotation(90);
        DatabaseReference userNavigationLog = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId()).child(navigationLogKey)
                .child("status");
        userNavigationLog.setValue("stopped").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(TAG, "Dynamic navigation stopped");
            }
        });
    }
}
