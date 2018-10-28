package com.rongill.rsg.sinprojecttest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);



        TextView friendName = (TextView)findViewById(R.id.friend_name);
        TextView connectionStatus = (TextView)findViewById(R.id.connection_status_textView);
        friendName.setText(getIntent().getStringExtra("FRIEND_NAME"));
        connectionStatus.setText(getIntent().getStringExtra("CONNECTION_STATUS"));

        //TODO get friend info from DB(User object + picture uri)
        //give the friend image a rounded border/
        setRoundedImage();


    }
    public void actionPressed(View v) {
        switch (v.getId()) {
            case R.id.sayhiBtn:
                Toast.makeText(this, "msg was sent to friend", Toast.LENGTH_SHORT).show();
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
}
