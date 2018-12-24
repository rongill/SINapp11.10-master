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
    private ArrayList<User> friends = new ArrayList<>();
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
    }

    public void setFriends(ArrayList<User> friendList){
        this.friends = friendList;
    }

    public ArrayList<User> getFriends(){
        return this.friends;
    }

    public void addFriend(User friend){
        this.friends.add(friend);
    }

    public void setFriendStatusByUid(String friendUid, String newStatus){
        for(int i=0; i<friends.size();i++){
            if(friends.get(i).getUserId().equals(friendUid))
                this.friends.get(i).setStatus(newStatus);

        }

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
        DatabaseReference userBeaconRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getUid()).child("beacon");
        userBeaconRef.setValue(beaconName);
    }

    public void setUserBeaconCoordinatesFromDB(){
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");
        Query query = beaconRef.orderByChild("name").equalTo(currentBeacon.getName());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Point p = new Point();
                    p.setX(Integer.parseInt(ds.child("x").getValue().toString()));
                    p.setY(Integer.parseInt(ds.child("y").getValue().toString()));
                    currentBeacon.setCoordinates(p);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
