package com.rongill.rsg.sinprojecttest;

import java.util.ArrayList;

public class User {

    private String userName;
    private boolean status;
    private ArrayList<User> friends;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isConnected() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    public User(String userName, boolean isConnected, ArrayList<User> friends){
        this.userName = userName;
        this.status = isConnected;
        this.friends = friends;
    }


}
