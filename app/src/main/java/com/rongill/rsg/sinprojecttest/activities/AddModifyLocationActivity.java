package com.rongill.rsg.sinprojecttest.activities;

import android.app.Activity;
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
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.R;

import java.util.HashMap;

public class AddModifyLocationActivity extends Activity {

    private EditText locationNameEt, locationCategoryEt, beaconEt, xAxisEt, yAxisEt;
    private Button submitBtn;

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
        submitBtn = (Button)findViewById(R.id.submit_location_btn);

        //if the intent is to modify a location, set the text in the ET to modify before submit
        if(getIntent().getSerializableExtra("LOCATION_MODIFY") != null){
            Location locationModifier = (Location)getIntent().getSerializableExtra("LOCATION_MODIFY");
            locationNameEt.setText(locationModifier.getName());
            locationCategoryEt.setText(locationModifier.getCategory());
            beaconEt.setText(locationModifier.getBeacon());
            xAxisEt.setText(String.valueOf(locationModifier.getCoordinates().getX()));
            yAxisEt.setText(String.valueOf(locationModifier.getCoordinates().getY()));
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitReady()) {
                    if((Location)getIntent().getSerializableExtra("LOCATION_MODIFY") != null){
                        final Location locationModifier = (Location)getIntent().getSerializableExtra("LOCATION_MODIFY");
                        Query query = FirebaseDatabase.getInstance().getReference()
                                .child("locations").orderByChild("name").equalTo(locationModifier.getName());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    DatabaseReference locationToModifyRef = FirebaseDatabase.getInstance().getReference()
                                            .child("locations").child(ds.getKey());

                                    locationToModifyRef.child("beacon").setValue(beaconEt.getText().toString());
                                    locationToModifyRef.child("category").setValue(locationCategoryEt.getText().toString());
                                    locationToModifyRef.child("name").setValue(locationNameEt.getText().toString());
                                    locationToModifyRef.child("x").setValue(xAxisEt.getText().toString());
                                    locationToModifyRef.child("y").setValue(yAxisEt.getText().toString());

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        HashMap<String, String> postLocation = new HashMap<>();
                        postLocation.put("beacon", beaconEt.getText().toString());
                        postLocation.put("category", locationCategoryEt.getText().toString());
                        postLocation.put("name", locationNameEt.getText().toString());
                        postLocation.put("x", xAxisEt.getText().toString());
                        postLocation.put("y", yAxisEt.getText().toString());

                        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference()
                                .child("locations");
                        locationRef.push().setValue(postLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getBaseContext(), (getIntent().getSerializableExtra("LOCATION_MODIFY")) != null ? "Location Modified successfully" : "Location Added successfully", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getBaseContext(), "something whent wrong...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                } else {
                    Toast.makeText(getBaseContext(),"please fill all fields", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });





    }

    private boolean submitReady(){
        if (!locationNameEt.getText().toString().isEmpty()
                && !locationCategoryEt.getText().toString().isEmpty()
                && !beaconEt.getText().toString().isEmpty())
            return true;
        return false;
    }
}
