package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

public class StaticIndoorNavigation extends IndoorNavigation {

    //this boolean var will be the post at main UI thread
    public boolean hasArrived = false;
    private MyBleScanner staticNavBleScanner = new MyBleScanner();

    public StaticIndoorNavigation(MyBleScanner myBleScanner, User currentUser, Location destination, BluetoothLeScanner scanner, Compass compass){
        super(currentUser,destination, scanner, compass);
        this.staticNavBleScanner = myBleScanner;

    }

    @Override
    public void startNavigation() {
        super.startNavigation();

        //init the bleScanner with the scanner from Main activity.
        staticNavBleScanner.setScanner(scanner);

        //start the LE scan, using the scanner from Main activity and the scan callbacks to go to staticNavScanCallback as below.
        staticNavBleScanner.initLeScan(staticNavScanCallback, true);

        //this thread is executing a loop on a delay of 1 sec, basically checks every 1 sec if the user arrived at destination
        //if arrived, stop scanning, post a message and end the navigation process.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasArrived){
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(currentUser.getCurrentBeacon().getName().equals(destination.getBeacon())){
                        hasArrived = true;
                        staticNavBleScanner.initLeScan(staticNavScanCallback, false);
                        //TODO user has arrived to destination, create a message.

                    }

                }
            }
        }).start();

    }

    private ScanCallback staticNavScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            MyBeacon scannedBeacon = new MyBeacon();

            //only do the below (add/update beacon to list, set user beacon, calc direction/distance, rotate compass and display distance)
            //if the scan result device name contains SIN (System beacon)
            if (result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")) {
                scannedBeacon.setMACaddress(result.getDevice().getAddress());
                scannedBeacon.setName(result.getDevice().getName());
                scannedBeacon.setRssi(result.getRssi());

                //need to do the navigation calc after the reed coordinates method execute(async DB task).
                readDBeaconDataFromDB(scannedBeacon);
            }


        }
    };

    private void readDBeaconDataFromDB(final MyBeacon scannedBeacon){
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");

        Query scannedBeaconRef = beaconRef.orderByChild("name").equalTo(scannedBeacon.getName());
        scannedBeaconRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean contains = false;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Point p = new Point(Integer.valueOf
                            (ds.child("x").getValue().toString()), Integer.valueOf(ds.child("y").getValue().toString()));
                    scannedBeacon.setCoordinates(p);
                    scannedBeacon.setFloor(ds.child("floor").getValue().toString());
                }

                //if scanned device already in the list, update it.
                for (int i = 0; i < staticNavBleScanner.getScannedDeviceList().size(); i++) {
                    if (staticNavBleScanner.getScannedDeviceList().get(i).getMACaddress().contains(scannedBeacon.getMACaddress())) {
                        contains = true;
                        staticNavBleScanner.getScannedDeviceList().set(i, scannedBeacon);
                    }
                }

                //add to the list all the devices scanned that has "SIN" string in the name.
                if (!contains) {
                    scannedBeacon.setName(scannedBeacon.getName().substring(4));
                    staticNavBleScanner.getScannedDeviceList().add(scannedBeacon);

                }

                //set the user beacon to the closest one in the list, based on rssi value.
                //on the start navigation thread, if user has the same beacon as the destination, will detect and stop the scan etc..
                currentUser.setCurrentBeacon(staticNavBleScanner.findClosestBeacon());

                //if the current user beacon name not equals to destination beacon name, update the distance, direction and destination.
                if(!currentUser.getCurrentBeacon().getName().equals(destination.getBeacon())) {
                    //calc the direction based on current user location angle to the destination coordinates.
                    //set the float value to the directionAzimuth var in indoor navigation abstract class.
                    calcDirectionToDestination(currentUser.getCurrentBeacon().getCoordinates(), destination.getCoordinates());

                    //rotate the image of the compass according to the directionAzimuth in degrees, calculated in the above method.
                    compass.compassImage.setRotation(90+directionAzimuth);

                    //calc the distance to destination based on user current beacon
                    calcDistanceToDestination();
                    String distanceToDestination = "distance to " + destination.getName() + ":" + String.valueOf(distance);
                    compass.getUserLocationTv().setText(distanceToDestination);

                } else {
                    hasArrived = true;
                    staticNavBleScanner.initLeScan(staticNavScanCallback, false);
                    compass.compassImage.setRotation(90);
                    String arrivalNotification = "you arrived at you destination";
                    compass.getUserLocationTv().setText(arrivalNotification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
