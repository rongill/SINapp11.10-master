package com.rongill.rsg.sinprojecttest.activities;

import android.opengl.ETC1;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;

import java.util.HashMap;

public class AddModifyBeaconActivity extends AppCompatActivity {

    private EditText beaconNameEt, beaconFloorEt, beaconXEt, beaconYEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_modify_beacon);

        //Define the window smaller.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getWindow().setLayout((int)(metrics.widthPixels*.8), (int)(metrics.heightPixels*.6));

        beaconNameEt = (EditText)findViewById(R.id.beacon_name_ET);
        beaconFloorEt = (EditText)findViewById(R.id.beacon_floor_ET);
        beaconXEt = (EditText)findViewById(R.id.beacon_x_axis);
        beaconYEt = (EditText)findViewById(R.id.beacon_y_axis);

        Button submitBeaconSettingsBtn = (Button) findViewById(R.id.submit_beacon_settings_btn);

        //if the intent is to modify a beacon, set the text in the ET to modify before submit
        if(getIntent().getSerializableExtra("BEACON_MODIFY") != null){
            MyBeacon beaconModifier = (MyBeacon)getIntent().getSerializableExtra("BEACON_MODIFY");
            beaconNameEt.setText(beaconModifier.getName());
            beaconFloorEt.setText(beaconModifier.getFloor());
            beaconXEt.setText(String.valueOf(beaconModifier.getCoordinates().getX()));
            beaconYEt.setText(String.valueOf(beaconModifier.getCoordinates().getY()));
        }

        //Set on click submit if all fields are filled.
        submitBeaconSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitReady()){
                    if(getIntent().getSerializableExtra("BEACON_MODIFY") != null){
                        final MyBeacon beaconModifier = (MyBeacon)getIntent().getSerializableExtra("BEACON_MODIFY");
                        Query modifyLocationQuery = FirebaseDatabase.getInstance().getReference()
                                .child("beacons").orderByChild("name").equalTo(beaconModifier.getName());
                        modifyLocationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    DatabaseReference beaconToModifyRef = FirebaseDatabase.getInstance().getReference()
                                            .child("beacons").child(ds.getKey());
                                    beaconToModifyRef.child("name").setValue("SIN/" + beaconNameEt.getText().toString());
                                    beaconToModifyRef.child("floor").setValue(beaconFloorEt.getText().toString());
                                    beaconToModifyRef.child("x").setValue(beaconXEt.getText().toString());
                                    beaconToModifyRef.child("y").setValue(beaconYEt.getText().toString());
                                    beaconToModifyRef.child("structure").setValue(beaconModifier.getStructure());

                                    MyCalendar myCalendar = new MyCalendar();
                                    beaconToModifyRef.child("date-modified").setValue(myCalendar);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        HashMap<String, String> postBeacon = new HashMap<>();
                        postBeacon.put("name", "SIN/" + beaconNameEt.getText().toString());
                        postBeacon.put("floor", beaconFloorEt.getText().toString());
                        postBeacon.put("x", beaconXEt.getText().toString());
                        postBeacon.put("y", beaconYEt.getText().toString());
                        postBeacon.put("structure",getIntent().getStringExtra("STRUCTURE"));


                        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                                .child("beacons");
                        String pushKey = beaconRef.push().getKey();
                        beaconRef.child(pushKey).setValue(postBeacon).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getBaseContext(), "Beacon added successfully", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getBaseContext(), "something went wrong...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        beaconRef.child(pushKey).child("date-modified").setValue(new MyCalendar());


                    }
                } else {
                    Toast.makeText(getBaseContext(), "please fill all fields", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });
    }

    private boolean submitReady(){
        return !beaconNameEt.getText().toString().isEmpty()
                && !beaconFloorEt.getText().toString().isEmpty()
                && !beaconXEt.getText().toString().isEmpty()
                && !beaconYEt.getText().toString().isEmpty();
    }
}
