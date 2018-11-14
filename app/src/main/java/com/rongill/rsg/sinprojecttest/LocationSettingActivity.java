package com.rongill.rsg.sinprojecttest;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;



import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LocationSettingActivity extends AppCompatActivity {

    private LocationListAdapter locationListAdapter;
    private ArrayList<Location> locations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_setting);

        Toolbar toolbar = (Toolbar)findViewById(R.id.location_settings_toolbar);
        setSupportActionBar(toolbar);

        //get the location list from the intent.
        Intent intent = getIntent();
        locations = new ArrayList<>();
        locations = (ArrayList<Location>)intent.getSerializableExtra("LOCATION_LIST");

        //TODO currently if we add a location the adapter wouldn't change as there is no listener to the server, maybe change that?
        setLocationAdapter();



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
                //TODO bug found, need to init the Adapter with a clone location list, so will not modify the real list as we search.
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
                    setLocationAdapter();
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
                Intent intent = new Intent(this, AddLocationActivity.class);
                startActivity(intent);
                break;
            case R.id.beacon_settings:
                //TODO move to beacon settings page.
        }

        return super.onOptionsItemSelected(item);
    }
    //set the location listView to the location adapter with the location list
    public ArrayList<Location> setLocationAdapter(){
        ArrayList<Location> fullLocationList = new ArrayList<>();
        for(Location temp : locations){
            fullLocationList.add(new Location(temp));
        }

        ListView locationListView = (ListView)findViewById(R.id.location_settings_listView);
        locationListAdapter = new LocationListAdapter(fullLocationList,this);
        locationListView.setAdapter(locationListAdapter);
        return fullLocationList;
    }


}
