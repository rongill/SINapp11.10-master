package com.rongill.rsg.sinprojecttest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LocationInfoPage extends AppCompatActivity {

    private TextView locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info_page);

        //set the text to the location name transfered from the intent
        //TODO use passed location name to get data from the DB server.
        locationName = (TextView)findViewById(R.id.location_name);
        locationName.setText(getIntent().getStringExtra("LOCATION_NAME"));

        setRoundedImage();

        //init toggle button with small animation.
        //TODO when isChecked is true, need to add to the user fav places, when false remove from fav list (in DB)
        setFavToggleButton();


    }

    public void setFavToggleButton(){

        ToggleButton buttonFavorite = (ToggleButton)findViewById(R.id.button_favorite);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);
        buttonFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   buttonView.startAnimation(scaleAnimation);
                   Toast.makeText(getBaseContext(),locationName.getText().toString()+"was added to your favorite list",Toast.LENGTH_SHORT).show();
               } else {
                   buttonView.startAnimation(scaleAnimation);
                   Toast.makeText(getBaseContext(),locationName.getText().toString()+"was removed from your favorite list",Toast.LENGTH_SHORT).show();
               }

            }
        });

    }

    public void setRoundedImage(){
        ImageView locationImage = (ImageView) findViewById(R.id.location_image);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_place_holder);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
        roundedBitmapDrawable.setCircular(true);
        locationImage.setImageDrawable(roundedBitmapDrawable);
    }
}
