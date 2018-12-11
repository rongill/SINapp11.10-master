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
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.adapters.BeaconListAdapter;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.util.ArrayList;

public class BeaconSettingActivity extends AppCompatActivity {

    private BeaconListAdapter beaconListAdapter;
    private ArrayList<MyBeacon> beacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.beacon_settings_toolbar);
        setSupportActionBar(toolbar);

        beacons = new ArrayList<>();
        setBeaconAdapter();
        setBeaconList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.beacon_settings_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_beacon);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty()){
                    ArrayList<MyBeacon> tempBeaconList = new ArrayList<>();
                    beaconListAdapter.clear();
                    for(MyBeacon temp : beacons){
                        if(temp.getName().toLowerCase().contains(newText.toLowerCase())){
                            tempBeaconList.add(new MyBeacon(temp));
                        }
                    }
                    beaconListAdapter.addAll(tempBeaconList);
                    beaconListAdapter.notifyDataSetChanged();
                } else {
                    beaconListAdapter.clear();
                    beaconListAdapter.addAll(beacons);
                    beaconListAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_beacon:
                Intent intent = new Intent(this, AddModifyBeaconActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setBeaconAdapter() {
        ListView beaconListView = (ListView)findViewById(R.id.beacon_settings_listView);
        beaconListAdapter = new BeaconListAdapter(new ArrayList<MyBeacon>(), this);
        beaconListView.setAdapter(beaconListAdapter);
    }

    private void setBeaconList() {
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");
        beaconRef.addChildEventListener(new ChildEventListener() {
            //create the beacon list, when added in DB, also will add in beacons list here.
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyBeacon tempBeacon = new MyBeacon();
                tempBeacon.setName(dataSnapshot.getValue(MyBeacon.class).getName());
                tempBeacon.setFloor(dataSnapshot.getValue(MyBeacon.class).getFloor());

                Point p = new Point();
                p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                tempBeacon.setCoordinates(p);

                beacons.add(tempBeacon);
                beaconListAdapter.clear();
                beaconListAdapter.addAll(beacons);
                beaconListAdapter.notifyDataSetChanged();
            }
            //when child changed, build a temp beacon, look it up at the beacons list, and replace.
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyBeacon tempBeacon = new MyBeacon();
                tempBeacon.setName(dataSnapshot.getValue(MyBeacon.class).getName());
                tempBeacon.setFloor(dataSnapshot.getValue(MyBeacon.class).getFloor());

                Point p = new Point();
                p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                tempBeacon.setCoordinates(p);

                for (MyBeacon temp : beacons){
                    if(temp.getName().equals(tempBeacon.getName())){
                        int i = beacons.indexOf(temp);
                        beacons.set(i, tempBeacon);
                    }
                }

                beaconListAdapter.clear();
                beaconListAdapter.addAll(beacons);
                beaconListAdapter.notifyDataSetChanged();
            }

            //when item removed in database, also remove from list.
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String beaconName = dataSnapshot.getValue(MyBeacon.class).getName();

                for(MyBeacon temp : beacons){
                    if(temp.getName().equals(beaconName))
                        beacons.remove(temp);
                }

                beaconListAdapter.clear();
                beaconListAdapter.addAll(beacons);
                beaconListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
