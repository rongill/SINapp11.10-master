package com.rongill.rsg.sinprojecttest.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.navigation.Point;

public class LocationInfoActivity extends Activity {

    private static final int STATIC_NAV_RESULT_CODE = 200;

    private String locationName, structureName;
    private Location thisLocation;
    private Button startNavBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info_page);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7),(int)(height*.65));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;


        //set the text to the location name transferred from the intent
        TextView locationNameTV = (TextView) findViewById(R.id.location_name);
        TextView structureTV = (TextView) findViewById(R.id.location_structure_textView);

        locationName = getIntent().getStringExtra("LOCATION_NAME");
        structureName = getIntent().getStringExtra("STRUCTURE");
        locationNameTV.setText(locationName);
        structureTV.setText("Structure: " + structureName);

        setLocation();
        setRoundedImage();



        startNavBtn = (Button) findViewById(R.id.start_navigation_button);
        startNavBtn.setVisibility(View.INVISIBLE);
        startNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("navigation-status");
                userStatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("navigating")){
                            Toast.makeText(getBaseContext(), "Cannot navigate, you are currently navigating.", Toast.LENGTH_LONG).show();
                        } else {
                            Intent navIntent = new Intent();
                            navIntent.putExtra("LOCATION", thisLocation);
                            setResult(STATIC_NAV_RESULT_CODE, navIntent);
                            finish();
                            userStatusRef.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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
                .child("structures").child(structureName).child("locations");
        Query query = locationRef.orderByChild("name").equalTo(locationName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    thisLocation.setBeaconName(ds.child("beacon").getValue().toString());
                    thisLocation.setName(ds.getValue(Location.class).getName());
                    thisLocation.setCategory(ds.getValue(Location.class).getCategory());
                    thisLocation.setFloor(ds.getValue(Location.class).getFloor());
                    thisLocation.setStructure(ds.getValue(Location.class).getStructure());

                    Point p = new Point();
                    p.setX(Integer.parseInt(ds.child("x").getValue().toString()));
                    p.setY(Integer.parseInt(ds.child("y").getValue().toString()));

                    thisLocation.setCoordinates(p);
                }

                TextView floorTv = (TextView)findViewById(R.id.location_floor_textView);
                floorTv.setText("Floor: " + thisLocation.getFloor());
                startNavBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
