package com.rongill.rsg.sinprojecttest;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendProfileActivity extends AppCompatActivity {

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
        currentUser = new User();

        Intent intent = getIntent();
        currentUser = (User)intent.getSerializableExtra("CURRENT_USER");

        setFriendInfoFromDb();

        //give the friend image a rounded border/
        setRoundedImage();


    }
    public void actionPressed(View v) {
        switch (v.getId()) {
            case R.id.poke_btn:
                sendPokeRequest();
                Toast.makeText(this, "poke sent to " + friend.getUsername(), Toast.LENGTH_SHORT).show();

                break;
            case R.id.meetBtn:
                Toast.makeText(this, "meet request sent to friend", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sendLocatonBtn:
                Toast.makeText(this, "Location sent to friend", Toast.LENGTH_SHORT).show();
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

    private void setFriendInfoFromDb(){
        Intent intent = getIntent();
        friend = new User();
        friend.setUserId(intent.getStringExtra("FRIEND_UID"));
        DatabaseReference friendUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friend.getUserId());
        friendUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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


    private void sendPokeRequest(){
        RequestMessage message = new RequestMessage(friend.getUserId(), currentUser.getUserId(),currentUser.getUsername()
                , "poke", "pending");
        DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(friend.getUserId());
        friendInboxRef.push().setValue(message);
    }

}
