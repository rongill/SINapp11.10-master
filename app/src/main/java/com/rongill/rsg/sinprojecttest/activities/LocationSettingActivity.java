package com.rongill.rsg.sinprojecttest.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.MaintenanceUser;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.adapters.LocationListAdapter;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.util.ArrayList;

public class LocationSettingActivity extends AppCompatActivity {

    private LocationListAdapter locationListAdapter;
    private ArrayList<Location> locations;
    private MaintenanceUser maintenanceUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_setting);

        Toolbar toolbar = (Toolbar)findViewById(R.id.location_settings_toolbar);
        setSupportActionBar(toolbar);



            setMaintenanceUser();

        locations = new ArrayList<>();
        setLocationAdapter();


    }

    private void setMaintenanceUser() {
        Intent intent = getIntent();
        User tempUser = (User) intent.getSerializableExtra("USER");
        maintenanceUser = new MaintenanceUser(
                tempUser.getUserId(), tempUser.getUsername(),
                tempUser.getStatus(), tempUser.getUserType(), "");
        DatabaseReference maintenanceUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(maintenanceUser.getUserId()).child("structure-related");
        maintenanceUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                maintenanceUser.setStructure(dataSnapshot.getValue().toString());
                setLocationsList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_settings_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_location);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                 if(!newText.isEmpty()){
                    ArrayList<Location> tempLocationList = new ArrayList<>();
                    locationListAdapter.clear();
                    for(Location temp : locations){
                        //locationListAdapter.clear();
                        if(temp.getName().toLowerCase().contains(newText.toLowerCase())){
                            tempLocationList.add(new Location(temp));
                        }
                    }
                    locationListAdapter.addAll(tempLocationList);
                    locationListAdapter.notifyDataSetChanged();
                } else {
                    locationListAdapter.clear();
                    locationListAdapter.addAll(locations);
                    locationListAdapter.notifyDataSetChanged();
                }

                return true;

            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_location:
                Intent intent = new Intent(this, AddModifyLocationActivity.class);
                intent.putExtra("STRUCTURE", maintenanceUser.getStructure());
                startActivity(intent);
                break;
            case R.id.beacon_settings:
                Intent beaconSettingIntent = new Intent(this, BeaconSettingActivity.class);
                beaconSettingIntent.putExtra("STRUCTURE", maintenanceUser.getStructure());
                startActivity(beaconSettingIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setLocationsList(){
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(maintenanceUser.getStructure()).child("locations");

        locationsRef.addChildEventListener(new ChildEventListener() {

            //create the locations list, when added in DB, also will add in locations list here.
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Location tempLocation = new Location();

                tempLocation.setName(dataSnapshot.getValue(Location.class).getName());
                tempLocation.setCategory(dataSnapshot.getValue(Location.class).getCategory());
                tempLocation.setBeaconName(dataSnapshot.child("beacon").getValue().toString());
                tempLocation.setFloor(dataSnapshot.getValue(Location.class).getFloor());
                tempLocation.setDateModified(dataSnapshot.child("date-modified").getValue(MyCalendar.class));

                tempLocation.setStructure(maintenanceUser.getStructure());

                Point p = new Point();
                p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                tempLocation.setCoordinates(p);

                locations.add(tempLocation);
                locationListAdapter.clear();
                locationListAdapter.addAll(locations);
                locationListAdapter.notifyDataSetChanged();
            }

            //when child changed, build a temp location, look it up at the locations list, and replace.
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Location tempLocation = new Location();

                tempLocation.setName(dataSnapshot.getValue(Location.class).getName());
                tempLocation.setCategory(dataSnapshot.getValue(Location.class).getCategory());
                tempLocation.setBeaconName(dataSnapshot.getValue(Location.class).getBeaconName());
                tempLocation.setFloor(dataSnapshot.getValue(Location.class).getFloor());
                tempLocation.setDateModified(dataSnapshot.child("date-modified").getValue(MyCalendar.class));


                tempLocation.setStructure(maintenanceUser.getStructure());

                Point p = new Point();
                p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                tempLocation.setCoordinates(p);

                for(Location temp : locations){
                    if(temp.getName().equals(tempLocation.getName())){
                        int i = locations.indexOf(temp);
                        locations.set(i, tempLocation);
                    }
                }

                locationListAdapter.clear();
                locationListAdapter.addAll(locations);
                locationListAdapter.notifyDataSetChanged();
            }

            //when item removed in database, also remove from list.
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                String locationName = dataSnapshot.getValue(Location.class).getName();

                for(int i = 0; i<locations.size(); i++){
                    Location locationToRemove = locations.get(i);
                    if(locationToRemove.getName().equals(locationName)){
                        locations.remove(locationToRemove);
                    }
                }

                locationListAdapter.clear();
                locationListAdapter.addAll(locations);
                locationListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //set the location listView to the location adapter with the location list
    public void setLocationAdapter(){

        ListView locationListView = (ListView)findViewById(R.id.location_settings_listView);
        locationListAdapter = new LocationListAdapter(new ArrayList<Location>(),this);
        locationListView.setAdapter(locationListAdapter);

    }


}
