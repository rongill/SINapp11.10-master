package com.rongill.rsg.sinprojecttest.basic_objects;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {

    private String userId, status, username, userType;
    private ArrayList<String> friendsUid = new ArrayList<>();
    private MyBeacon currentBeacon;

    public User(){}

    public User(String userId, String userName, String status, String userType){
        this.userId = userId;
        this.username = userName;
        this.status = status;
        this.userType = userType;
        currentBeacon = new MyBeacon();

    }

    public MyBeacon getCurrentBeacon() {
        return currentBeacon;
    }

    public void setCurrentBeacon(MyBeacon currentBeacon) {
        this.currentBeacon = currentBeacon;
        updateUserBeaconNameInDB(currentBeacon.getName());
        this.currentBeacon.getBeaconDetailsDB();
    }

    public ArrayList<String> getFriends(){
        return this.friendsUid;
    }

    public void addFriend(String friendUid){
        this.friendsUid.add(friendUid);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserType(String userType){
        this.userType = userType;
    }

    public String getUserType(){
        return this.userType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return (Objects.equals(getUserId(), user.getUserId()) &&
                Objects.equals(getStatus(), user.getStatus()) &&
                Objects.equals(getUsername(), user.getUsername()) &&
                Objects.equals(getUserType(), user.getUserType()));
    }

    @Override
    public int hashCode() {

        return Objects.hash(getUserId(), getStatus(), username, getUserType());
    }

    private void updateUserBeaconNameInDB(String beaconName){
        if(FirebaseAuth.getInstance().getUid() != null) {
            DatabaseReference userBeaconRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getUid()).child("beacon");
            userBeaconRef.setValue(beaconName);
        }
    }
}
