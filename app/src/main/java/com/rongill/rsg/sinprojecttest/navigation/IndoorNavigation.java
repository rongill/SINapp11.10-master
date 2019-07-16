package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.le.BluetoothLeScanner;
import android.util.Log;

import com.rongill.rsg.sinprojecttest.basic_objects.User;

import java.util.Scanner;

public abstract class IndoorNavigation {

    private  final String TAG = "IndoorNavigation";

    protected User currentUser;
    protected Location destination;
    protected Compass compass;
    protected float distance, directionAzimuth;


    public IndoorNavigation(User currentUser, Location destination, Compass compass){
        this.currentUser = currentUser;
        this.destination = destination;
        this.compass = compass;
        this.distance = 0;
        this.directionAzimuth = 0;
    }

    protected void startNavigation(){}

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public float getDirectionAzimuth() {
        return directionAzimuth;
    }

    public void setDirectionAzimuth(float directionAzimuth) {
        this.directionAzimuth = directionAzimuth;
    }

    //calculate the distance from current location to destination, use Point method "distanceToOtherPoint".
    protected void calcDistanceToDestination(){
        distance = currentUser.getCurrentBeacon().getCoordinates()
                .distanceToOtherPoint(destination.getCoordinates());
    }

    //calculate the angel from current location to destination, use Point method "distanceToOtherPoint" and base algorithm.
    protected void calcDirectionToDestination(Point currentCoordinates, Point destination){
        try{
            int alpha;
            switch (currentCoordinates.relativePosition(destination)){
                case 1:
                    alpha = (int)Math.toDegrees(Math.asin(Math.abs
                            (destination.getY() - currentCoordinates.getY())/
                            currentCoordinates.distanceToOtherPoint(destination)));
                    directionAzimuth = 90 - alpha;
                    break;
                case 2:
                    alpha = (int)Math.toDegrees(Math.asin(Math.abs
                            (destination.getX() - currentCoordinates.getX())/
                            currentCoordinates.distanceToOtherPoint(destination)));
                    directionAzimuth = 180 - alpha;
                    break;
                case 3:
                    alpha = (int)Math.toDegrees(Math.asin(Math.abs
                            (destination.getY() - currentCoordinates.getY())/
                            currentCoordinates.distanceToOtherPoint(destination)));
                    directionAzimuth = 270 + alpha;
                    break;
                case 4:
                    alpha = (int)Math.toDegrees(Math.asin(Math.abs
                            (destination.getX() - currentCoordinates.getX())/
                            currentCoordinates.distanceToOtherPoint(destination)));
                    directionAzimuth = 180 + alpha;
            }
        } catch (IllegalArgumentException e) {
            //this exception will throw only if trying to navigate to the same point, hens distance divided is 0
            Log.e(TAG, "calc direction divided by 0!");
        }
    }
}
