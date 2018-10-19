package com.rongill.rsg.sinprojecttest;

import com.rongill.rsg.sinprojecttest.Navigation.Point;

public class User {

    private String username;
    private String userID;
    private Point coordinates;

    public User(String username, String userID, Point userCurrentLocation){

        this.username = username;
        this.userID = userID;
        coordinates = new Point(userCurrentLocation);

    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

    public Point getCoordiantes() {
        return coordinates;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setCoordiantes(Point coordinates) {
        this.coordinates = coordinates;
    }
}
