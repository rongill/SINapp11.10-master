package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.le.BluetoothLeScanner;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

public class StaticIndoorNavigation extends IndoorNavigation {

    private boolean hasArrived = false;
    private MyBleScanner staticNavBleScanner;

    public StaticIndoorNavigation(MyBleScanner myBleScanner, User currentUser, Location destination, BluetoothLeScanner scanner, Compass compass){
        super(currentUser,destination, scanner, compass);
        this.staticNavBleScanner = myBleScanner;

    }

    @Override
    public void startNavigation() {
        super.startNavigation();

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
                    currentUser.setCurrentBeacon(staticNavBleScanner.getClosestBeacon());
                    if(currentUser.getCurrentBeacon().getName().equals(destination.getBeaconName())){
                        hasArrived = true;
                        staticNavBleScanner.initLeScan(false);
                        compass.compassImage.setRotation(90);
                        //TODO user has arrived to destination, create a message/notification.
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
}