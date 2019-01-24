package com.rongill.rsg.sinprojecttest.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.R;

import java.util.ArrayList;

public class StructureInfoActivity extends AppCompatActivity {

    private ArrayList<String> shopsListview, foodListview, servicesListview, favoriteListview;
    private ArrayAdapter<String> expandableListviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure_info);

        final ListView expandableListview = (ListView)findViewById(R.id.expandable_listView);
        expandableListviewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        expandableListview.setAdapter(expandableListviewAdapter);
        expandableListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), LocationInfoPage.class);
                intent.putExtra("LOCATION_NAME", (String)expandableListview.getItemAtPosition(position));
                startActivity(intent);
                finish();
            }
        });

        //get the structure name from the intent and set the location LV of that structure.
        String structureName = getIntent().getStringExtra("STRUCTURE_NAME");
        setFullLocationListFromDB(structureName);

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
}
