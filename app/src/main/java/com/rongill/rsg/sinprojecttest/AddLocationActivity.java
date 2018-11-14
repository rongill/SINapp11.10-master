package com.rongill.rsg.sinprojecttest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLocationActivity extends Activity {

    private EditText locationNameEt, locationCategoryEt, beaconEt;
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
        submitBtn = (Button)findViewById(R.id.submit_location_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitReady()) {
                    Location postLocation = new Location(locationNameEt.getText().toString(), locationCategoryEt.getText().toString(), beaconEt.getText().toString());
                    DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("locations");
                    locationRef.push().setValue(postLocation);
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
