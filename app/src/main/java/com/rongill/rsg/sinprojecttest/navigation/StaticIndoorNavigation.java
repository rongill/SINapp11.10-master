package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

import java.util.HashMap;
import java.util.Map;

public class StaticIndoorNavigation extends IndoorNavigation {

    private static final String TAG = "StaticIndoorNavigation";
    public boolean hasArrived = false;
    private String pushKey = "";
    private Context context;
    private MyBleScanner staticNavBleScanner;

    public StaticIndoorNavigation(Context context, MyBleScanner myBleScanner, User currentUser, Location destination, BluetoothLeScanner scanner, Compass compass){
        super(currentUser,destination, scanner, compass);
        this.staticNavBleScanner = myBleScanner;
        this.context = context;
    }

    @Override
    public void startNavigation() {
        super.startNavigation();

        staticNavigationLog();

        //init the bleScanner with the scanner from Main activity.
        staticNavBleScanner.setScanner(scanner);

        //start the LE scan, using the scanner from Main activity and the MyBleScanner scan callbacks.
        staticNavBleScanner.initLeScan(true);

        //this thread is executing a loop on a delay of 1 sec, basically checks every 1 sec if the user arrived at destination
        //if arrived, stop scanning, post a message and end the navigation process.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasArrived){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentUser.setCurrentBeacon(staticNavBleScanner.getNearestBeacon());
                    if(currentUser.getCurrentBeacon().getName().equals(destination.getBeaconName())){
                        hasArrived = true;
                        staticNavBleScanner.initLeScan(false);
                        compass.compassImage.setRotation(90);
                        setNavigationLogStatus("arrived");
                        arrivalNotificationMessage();
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

    public void stopNavigation(){
        staticNavBleScanner.initLeScan(false);
        DatabaseReference userNavigationLogStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId()).child(pushKey)
                .child("status");

        userNavigationLogStatusRef.setValue("stopped").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.w(TAG, "Static navigation process stopped by user/System.");
            }
        });

    }

    private void staticNavigationLog() {

        Map<String, String> newPost = new HashMap<>();
        newPost.put("status", "started");
        newPost.put("navigation-type", "static");

        DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId());

        pushKey = userNavigationLogRef.push().getKey();

        userNavigationLogRef.child(pushKey).setValue(newPost);
        userNavigationLogRef.child(pushKey).child("date-started").setValue(new MyCalendar());
        userNavigationLogRef.child(pushKey).child("destination").setValue(destination).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(TAG, "static navigation log created");
            }
        });
    }

    //set the navigation status.
    private void setNavigationLogStatus(String status){
        DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId());
        userNavigationLogRef.child(pushKey).setValue(status);
    }

    //Notification message when user arrives at destination.
    private void arrivalNotificationMessage(){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,"SIN")
                .setContentTitle("Friendly Poke")
                .setContentText("You arrived at your destination! tap to close...")
                .setSmallIcon(R.drawable.sinicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(3, notificationBuilder.build());
    }
}
