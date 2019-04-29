package com.rongill.rsg.sinprojecttest.app_utilities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.activities.MainDrowerActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;

import java.io.Serializable;

public class UserUtil implements Serializable {

    private final String TAG = "UserUtil";
    private FirebaseUser mFirebaseUser;
    private User currentUser;


    public  UserUtil(){
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        currentUser = new User();
        Log.i(TAG, "user init started.");
        setCurrentUser();
    }

    public User getCurrentUser(){
        return this.currentUser;
    }

    private void setCurrentUser(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser.setUserId(dataSnapshot.getKey());
                currentUser.setUsername(dataSnapshot.getValue(User.class).getUsername());
                currentUser.setUserType(dataSnapshot.getValue(User.class).getUserType());
                currentUser.setStatus(dataSnapshot.getValue(User.class).getStatus());

                Log.i(TAG, "user friends list creator started.");
                updateFriendList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //set the current users friends UID list
    private void updateFriendList(){
        Log.i(TAG, "fetching friend UID list from Database");
        DatabaseReference currentUserFriendsDb = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(currentUser.getUserId());

        currentUserFriendsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    currentUser.addFriend(ds.getValue().toString());
                }
                MainDrowerActivity.userIsSet = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //checks if friend is already in the friend list (before sending a friend request from MainActivity)
    public boolean checkIfFriendExist(String userId){
        for(String friendUid : currentUser.getFriends()){
            if(friendUid.equals(userId)) return true;
            Log.w(TAG, friendUid + "already in users friend list, would not add.");
        }
        return false;
    }

    //Saves the date from MainActivity onCreate.
    public void saveUserLoginDate (MyCalendar myCalendar){
        Log.i(TAG, "login time saved in DB");
        DatabaseReference userLoginDateRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("recent-login");
        userLoginDateRef.setValue(myCalendar);
    }


}
