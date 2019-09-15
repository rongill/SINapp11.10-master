package com.rongill.rsg.sinprojecttest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.R;

import java.util.HashMap;

public class AddModifyLocationActivity extends Activity {

    //TODO add documentation
    private EditText locationNameEt, locationCategoryEt, beaconEt, xAxisEt, yAxisEt, floorEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getWindow().setLayout((int)(metrics.widthPixels*.8), (int)(metrics.heightPixels*.6));

        locationNameEt = (EditText)findViewById(R.id.location_name_ET);
        locationCategoryEt = (EditText)findViewById(R.id.location_category_ET);
        beaconEt = (EditText)findViewById(R.id.beacon_id_ET);
        xAxisEt = (EditText)findViewById(R.id.x_axis);
        yAxisEt = (EditText)findViewById(R.id.y_axis);
        floorEt = (EditText)findViewById(R.id.floor_ET);


        Button submitBtn = (Button) findViewById(R.id.submit_location_btn);

        //if the intent is to modify a location, set the text in the ET to modify before submit
        if(getIntent().getSerializableExtra("LOCATION_MODIFY") != null){
            Location locationModifier = (Location)getIntent().getSerializableExtra("LOCATION_MODIFY");
            locationNameEt.setText(locationModifier.getName());
            locationCategoryEt.setText(locationModifier.getCategory());
            beaconEt.setText(locationModifier.getBeaconName());
            xAxisEt.setText(String.valueOf(locationModifier.getCoordinates().getX()));
            yAxisEt.setText(String.valueOf(locationModifier.getCoordinates().getY()));
            floorEt.setText(locationModifier.getFloor());
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitReady()) {
                    if(getIntent().getSerializableExtra("LOCATION_MODIFY") != null){
                        final Location locationModifier = (Location)getIntent().getSerializableExtra("LOCATION_MODIFY");
                        Query query = FirebaseDatabase.getInstance().getReference()
                                .child("structures").child(locationModifier.getStructure()).child("locations")
                                .orderByChild("name").equalTo(locationModifier.getName());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    DatabaseReference locationToModifyRef = FirebaseDatabase.getInstance().getReference()
                                            .child("structures").child(locationModifier.getStructure()).child("locations")
                                            .child(ds.getKey());

                                    locationToModifyRef.child("beacon").setValue(beaconEt.getText().toString());
                                    locationToModifyRef.child("category").setValue(locationCategoryEt.getText().toString());
                                    locationToModifyRef.child("name").setValue(locationNameEt.getText().toString());
                                    locationToModifyRef.child("x").setValue(xAxisEt.getText().toString());
                                    locationToModifyRef.child("y").setValue(yAxisEt.getText().toString());
                                    locationToModifyRef.child("floor").setValue(floorEt.getText().toString());
                                    locationToModifyRef.child("date-modified").setValue(new MyCalendar());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {

                        Intent intent = getIntent();
                        String structureName = intent.getStringExtra("STRUCTURE");

                        HashMap<String, String> postLocation = new HashMap<>();
                        postLocation.put("beacon", beaconEt.getText().toString());
                        postLocation.put("category", locationCategoryEt.getText().toString());
                        postLocation.put("name", locationNameEt.getText().toString());
                        postLocation.put("x", xAxisEt.getText().toString());
                        postLocation.put("y", yAxisEt.getText().toString());
                        postLocation.put("floor", floorEt.getText().toString());
                        postLocation.put("structure", structureName);



                        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference()
                                .child("structures").child(structureName).child("locations");
                        String pushKey = locationRef.push().getKey();
                        locationRef.child(pushKey).setValue(postLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getBaseContext(), (getIntent().getSerializableExtra("LOCATION_MODIFY")) != null ? "Location Modified successfully" : "Location Added successfully", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getBaseContext(), "something whent wrong...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        locationRef.child(pushKey).child("date-modified").setValue(new MyCalendar());

                    }
                    finish();

                } else {
                    Toast.makeText(getBaseContext(),"please fill all fields", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private boolean submitReady(){
        return !locationNameEt.getText().toString().isEmpty()
                && !locationCategoryEt.getText().toString().isEmpty()
                && !beaconEt.getText().toString().isEmpty()
                && !xAxisEt.getText().toString().isEmpty()
                && !yAxisEt.getText().toString().isEmpty()
                && !floorEt.getText().toString().isEmpty();
    }
}
