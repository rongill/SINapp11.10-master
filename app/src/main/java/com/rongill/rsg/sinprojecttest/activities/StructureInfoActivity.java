package com.rongill.rsg.sinprojecttest.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;

import java.util.ArrayList;

public class StructureInfoActivity extends AppCompatActivity {

    private ArrayList<String> shopsListview, foodListview, servicesListview;
    private ArrayAdapter<String> expandableListviewAdapter;
    private static final int NAVIGATION_REQUEST_CODE = 100;
    private static final int STATIC_NAV_RESULT_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure_info);

        //get the structure name from the intent and set the location LV of that structure.
        final String structureName = getIntent().getStringExtra("STRUCTURE_NAME");

        TextView structureNameTv = (TextView)findViewById(R.id.structure_name);
        structureNameTv.setText(structureName);
        setStructureAddress(structureName);

        //give the friend image a rounded border/
        setRoundedImage();


        final ListView expandableListview = (ListView)findViewById(R.id.expandable_listView);
        expandableListviewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        expandableListview.setAdapter(expandableListviewAdapter);
        expandableListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), LocationInfoActivity.class);
                String locationName = (String)expandableListview.getItemAtPosition(position);
                intent.putExtra("LOCATION_NAME", locationName);
                intent.putExtra("STRUCTURE", structureName);
                startActivityForResult(intent, NAVIGATION_REQUEST_CODE);

            }
        });
        // set the location name strings in categories to display on the expendable ListView.
        setFullLocationListFromDB(structureName);


    }

    private void setStructureAddress(String structureName) {
        DatabaseReference structureAddressRef = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(structureName).child("address");
        structureAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView structureAddressTv = (TextView)findViewById(R.id.structure_address_main_TV);
                structureAddressTv.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 200){
            setResult(STATIC_NAV_RESULT_CODE, data);
            finish();
        }
    }

    private void setFullLocationListFromDB(String structureName) {

        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(structureName).child("locations");
        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shopsListview = new ArrayList<>();
                foodListview = new ArrayList<>();
                servicesListview = new ArrayList<>();

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    switch (ds.child("category").getValue().toString()){
                        case "shops":
                            shopsListview.add(ds.child("name").getValue().toString());
                            break;
                        case "food":
                            foodListview.add(ds.child("name").getValue().toString());
                            break;
                        case "services":
                            servicesListview.add(ds.child("name").getValue().toString());
                            break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //onClick method for the expandable views, setting the adapter to the correct data set.
    public void expandCategoryList(View v){
        switch (v.getId()){
            case R.id.shops_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(shopsListview);
                break;
            case R.id.food_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(foodListview);
                break;
            case R.id.services_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(servicesListview);
                break;

        }
    }

    public void setRoundedImage(){
        ImageView avatarImageView = (ImageView)findViewById(R.id.structure_image);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_place_holder);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
        roundedBitmapDrawable.setCircular(true);
        avatarImageView.setImageDrawable(roundedBitmapDrawable);

    }
}
