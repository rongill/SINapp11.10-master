package com.rongill.rsg.sinprojecttest.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.navigation.Point;

public class LocationInfoPage extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

    private String locationName;
    private Location thisLocation;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info_page);

        //set the text to the location name transfered from the intent
        TextView locationNameTV = (TextView) findViewById(R.id.location_name);
        locationName = getIntent().getStringExtra("LOCATION_NAME");
        locationNameTV.setText(locationName);

        setLocation();
        setRoundedImage();

        mFirebaseAuth = FirebaseAuth.getInstance();
        setFavToggleButton();


    }
    //TODO bug to fix, remove not working, need to figure how to set the toggle btn correctly at activity start.
    //init toggle button with small animation. add/remove location to users-favorites DB.
    public void setFavToggleButton(){

        final ToggleButton buttonFavorite = (ToggleButton)findViewById(R.id.button_favorite);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);

        DatabaseReference userFavoriteRef = FirebaseDatabase.getInstance().getReference()
                .child("users-favorites").child(mFirebaseAuth.getUid());

        //init the toggle btn to on if in fav list or off if not.
        Query query = userFavoriteRef.orderByChild("location-name").equalTo(locationName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    buttonFavorite.setChecked(true);
                    isFavorite = true;
                }

                buttonFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked && !isFavorite){
                            buttonView.startAnimation(scaleAnimation);
                            Toast.makeText(getBaseContext(),locationName+"was added to your favorite list",Toast.LENGTH_SHORT).show();
                            addRemoveUserFavorites(true);
                        } else if (!isChecked) {
                            buttonView.startAnimation(scaleAnimation);
                            addRemoveUserFavorites(false);
                        }

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    //pull the location from the DB, init thisLocation.
    private void setLocation(){
        thisLocation = new Location();

        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference()
                .child("locations");
        Query query = locationRef.orderByChild("name").equalTo(locationName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    thisLocation.setBeacon(ds.getValue(Location.class).getBeacon());
                    thisLocation.setName(ds.getValue(Location.class).getName());
                    thisLocation.setCategory(ds.getValue(Location.class).getCategory());

                    Point p = new Point();
                    p.setX(Integer.parseInt(ds.child("x").getValue().toString()));
                    p.setY(Integer.parseInt(ds.child("y").getValue().toString()));

                    thisLocation.setCoordinates(p);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //called from setFavToggleButton(true/false), add/remove from users favs in DB.
    private void addRemoveUserFavorites(final boolean state){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getUid();
        final DatabaseReference usersFavoriteRef = FirebaseDatabase.getInstance().getReference()
                .child("users-favorites").child(userId);
        if(state){
            usersFavoriteRef.push().child("location-name").setValue(locationName);
        } else {
            usersFavoriteRef.child("location-name").child(locationName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getBaseContext(),locationName+"was removed from your favorite list",Toast.LENGTH_SHORT).show();

                    }
                }
            });

            /*
            final Query query = usersFavoriteRef.orderByChild("location-name").equalTo(locationName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        usersFavoriteRef.child(ds.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getBaseContext(),locationName+"was removed from your favorite list",Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });*/
        }
    }
}
