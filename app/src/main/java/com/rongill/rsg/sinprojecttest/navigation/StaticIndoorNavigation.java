package com.rongill.rsg.sinprojecttest.navigation;

import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.MainDrowerActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;


public class StaticIndoorNavigation extends IndoorNavigation {

    private static final String TAG = "StaticIndoorNavigation";
    public boolean hasArrived = false;
    private String pushKey;
    private Context context;
    private MyBleScanner staticNavBleScanner;

    public StaticIndoorNavigation(Context context, User currentUser, Location destination, Compass compass, String navigationLogKey){
        super(currentUser,destination, compass);
        this.staticNavBleScanner = new MyBleScanner((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
        this.pushKey = navigationLogKey;
        this.context = context;
    }

    @Override
    public void startNavigation() {
        super.startNavigation();

        //init the bleScanner with the scanner from Main activity.
        staticNavBleScanner.setScanner(staticNavBleScanner.getScanner());

        //start the LE scan, using the scanner from Main activity and the MyBleScanner scan callbacks.
        staticNavBleScanner.initLeScan(staticNavScanCallback, true);

    }

    public void stopNavigation(){
        staticNavBleScanner.initLeScan(staticNavScanCallback, false);
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


    //set the navigation status.
    private void setNavigationLogStatus(String status){
        DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                .child("users-navigation-log").child(currentUser.getUserId());
        userNavigationLogRef.child(pushKey).child("status").setValue(status);
    }

    //Notification message when user arrives at destination.
    private void arrivalNotificationMessage(){
        Intent intent = new Intent(context, MainDrowerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,"SIN")
                .setContentTitle("Navigation Complete!")
                .setContentText("You arrived at your destination! tap to close...")
                .setSmallIcon(R.drawable.sinicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        //TODO not canceling the noti. when pressed

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(3, notificationBuilder.build());
    }


    private ScanCallback staticNavScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")) {
                MyBeacon scannedBeacon = new MyBeacon();
                scannedBeacon.setMACaddress(result.getDevice().getAddress());
                scannedBeacon.setName(result.getDevice().getName());
                scannedBeacon.setRssi(result.getRssi());

                try {
                    staticNavBleScanner.setBeaconDataFromServer(scannedBeacon);
                    currentUser.setCurrentBeacon(staticNavBleScanner.getNearestBeacon());
                    if (currentUser.getCurrentBeacon().getName().equals(destination.getBeaconName())) {
                        hasArrived = true;
                        staticNavBleScanner.initLeScan(staticNavScanCallback, false);
                        compass.compassImage.setRotation(90);
                        setNavigationLogStatus("arrived");
                        arrivalNotificationMessage();
                        compass.titleTv.setText("click image to scan your location");
                        compass.compassImage.setClickable(true);
                        compass.getUserLocationTv().setText("");
                    } else {
                        //calc the direction based on current user location angle to the destination coordinates.
                        //set the float value to the directionAzimuth var in indoor navigation abstract class.
                        calcDirectionToDestination(currentUser.getCurrentBeacon().getCoordinates(), destination.getCoordinates());

                        //calc the distance to destination based on user current beacon
                        calcDistanceToDestination();

                        //rotate the image of the compass according to the directionAzimuth in degrees, calculated in the above method.
                        compass.compassImage.setRotation(90 + directionAzimuth);

                        String distanceToDestination = "distance to " + destination.getName() + " : " + String.valueOf((int) distance) + "m";
                        compass.getUserLocationTv().setText(distanceToDestination);
                    }


                } catch (NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    };
}