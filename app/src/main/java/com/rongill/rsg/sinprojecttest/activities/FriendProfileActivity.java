package com.rongill.rsg.sinprojecttest.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendProfileActivity extends AppCompatActivity {

    private static final int DYNAMIC_NAV_RESULT_CODE = 300;
    private User friend;
    private TextView friendName;
    private TextView connectionStatus;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        friendName = (TextView)findViewById(R.id.friend_name);
        connectionStatus = (TextView)findViewById(R.id.connection_status_textView);
        //currentUser = new User();

        Intent intent = getIntent();
        currentUser = (User)intent.getSerializableExtra("CURRENT_USER");
        if (intent.getStringExtra("KEY_FROM_POKE_MESSAGE") != null){
            DatabaseReference pokeMessageRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-inbox").child(currentUser.getUserId()).child(intent.getStringExtra("KEY_FROM_POKE_MESSAGE"));
            pokeMessageRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        makeToast("poke message received from " + friend.getUsername() + ", want to navigate?");
                    }
                }
            });
        }


        setFriendInfoFromDb();

        //give the friend image a rounded border/
        setRoundedImage();


    }

    public void actionPressed(View v) {
        switch (v.getId()) {
            case R.id.poke_btn:
                sendPokeRequest();
                break;

            case R.id.meetBtn:
                sendNavigationRequest();
                break;

            case R.id.sendLocatonBtn:
                sendUserLocation();
                break;
        }
    }

    public void setRoundedImage(){
        ImageView avatarImageView = (ImageView)findViewById(R.id.avatar_imageView);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
        roundedBitmapDrawable.setCircular(true);
        avatarImageView.setImageDrawable(roundedBitmapDrawable);

    }

    //init friend obj from DB
    private void setFriendInfoFromDb(){
        Intent intent = getIntent();
        friend = new User();
        friend.setUserId(intent.getStringExtra("FRIEND_UID"));
        DatabaseReference friendUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friend.getUserId());
        friendUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friend.setUserId(dataSnapshot.getKey());
                friend.setUsername(dataSnapshot.getValue(User.class).getUsername());
                friend.setStatus(dataSnapshot.getValue(User.class).getStatus());
                friendName.setText(friend.getUsername());
                connectionStatus.setText(friend.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //send a RequestMassage to the friend with request type poke.
    private void sendPokeRequest(){
        RequestMessage message = new RequestMessage(friend.getUserId(), currentUser.getUserId(),currentUser.getUsername()
                , "poke", "pending");
        DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(friend.getUserId());
        friendInboxRef.push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeToast("poke sent to " + friend.getUsername());
                } else {
                    makeToast("something went wrong...");
                }
            }
        });
    }

    //send a RequestMassage to the friend with request type navigation.
    private void sendNavigationRequest(){
        //when a navigation in sent to the friend, close this activity and return to the main, onActivityResult in main activity should handle a return from here with the RequestMessage in the intent and activate a listener to the user navigation log if the friend started navigating.
        RequestMessage dynamicNavRequest =
                new RequestMessage(friend.getUserId(),
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        "dynamic-navigation-request",
                        "pending");

        DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(friend.getUserId());

        String pushKey = friendInboxRef.push().getKey();
        friendInboxRef.child(pushKey).setValue(dynamicNavRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeToast("meet request sent to " + friend.getUsername());
                } else {
                    makeToast("something went wrong...");
                }
            }
        });

        Intent intent = new Intent();
        intent.putExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE", dynamicNavRequest);
        intent.putExtra("FRIEND_NAME", friend.getUsername());
        intent.putExtra("NAVIGATION_LOG_KEY", pushKey);
        setResult(DYNAMIC_NAV_RESULT_CODE, intent);
        finish();
    }

    private void sendUserLocation(){
        RequestMessage dynamicNavRequest = new RequestMessage(friend.getUserId(), currentUser.getUserId(), currentUser.getUsername(), "friend-location", "pending");
        DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(friend.getUserId());
        friendInboxRef.push().setValue(dynamicNavRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeToast("your location was sent to " + friend.getUsername());
                } else {
                    makeToast("something went wrong...");
                }
            }
        });
    }

    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
