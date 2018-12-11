package com.rongill.rsg.sinprojecttest.app_utilities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

import java.io.Serializable;

public class UserUtil implements Serializable {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private User currentUser;


    public UserUtil(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        currentUser = new User();
        setCurrentUser();


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
                updateFriendList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //set the current users friend list
    private void updateFriendList(){
        DatabaseReference currentUserFriendsDb = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());

        currentUserFriendsDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    DatabaseReference friendUserRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(dataSnapshot.getValue().toString());
                    addFriend(dataSnapshot.getValue().toString(),friendUserRef);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //add new friend in the friend list of current user.
    private void addFriend(final String friendId, DatabaseReference friendUserRef){
        friendUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User tempUser = new User();
                tempUser.setUserId(friendId);
                tempUser.setUsername(dataSnapshot.getValue(User.class).getUsername());
                tempUser.setUserType(dataSnapshot.getValue(User.class).getUserType());
                currentUser.addFriend(tempUser);
                addFriendStatusListener(friendId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //set the friend status according to DB
    private void addFriendStatusListener(final String friendId){
        DatabaseReference friendStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friendId).child("status");
        friendStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentUser.setFriendStatusByUid(friendId, dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public User getCurrentUser(){
        return this.currentUser;
    }

    //checks if friend in already in the friend list (before adding)
    public boolean checkIfFriendExist(String userId){
        for(User friend : currentUser.getFriends()){
            if(friend.getUserId().equals(userId)) return true;
        }
        return false;
    }


}
