package com.rongill.rsg.sinprojecttest;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.Navigation.Compass;
import com.rongill.rsg.sinprojecttest.SignInPages.CreateUserPrifileActivity;
import com.rongill.rsg.sinprojecttest.SignInPages.LoginActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MainDrowerActivity extends AppCompatActivity implements SensorEventListener{

    //Firebase params
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //compass anim params
    private SensorManager mSensorManager;
    private Compass compass;

    ArrayAdapter<String> suggestionsListViewAdapter;
    ViewGroup searchSuggestionsLayout;

    private User currentUser;
    private ListView friendsListView;
    private FriendListAdapter friendsListViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drower);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchSuggestionsLayout = (ViewGroup)findViewById(R.id.suggestion_layout);//in appbar main drower layout
        //SearchView list and adapter.
        ListView suggestionsListView = (ListView) findViewById(R.id.suggestion_layout_listview);
        suggestionsListViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        suggestionsListView.setAdapter(suggestionsListViewAdapter);

        //compass imageview
        ImageView compassImage = (ImageView) findViewById(R.id.compass_image);

        //init compass vars for nav
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass = new Compass(compassImage, mSensorManager);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionsListViewAdapter.clear();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // init firebase auth status listener.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainDrowerActivity.this, LoginActivity.class));

                }
            }
        };

// init a user with an arraylist of friends users.

        getCurrentUserFriends();


        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_page_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<String> tempList = new ArrayList<>();
                ArrayList<String> locationData = new ArrayList<>(LocationData.getLocations());
                Collections.sort(locationData);
                if(!newText.isEmpty()) {
                    for (String temp : locationData) {
                        if (temp.toLowerCase().contains(newText.toLowerCase())) {
                            tempList.add(temp);
                        }

                        suggestionsListViewAdapter.clear();
                        suggestionsListViewAdapter.addAll(tempList);
                        searchSuggestionsLayout.bringToFront();
                    }
                }else{
                    suggestionsListViewAdapter.clear();
                }


                return true;
            }
        });


        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.sign_out_menu_btn:
                signOut();
                break;
            case R.id.profile_menu_btn:
                startActivity(new Intent(this, CreateUserPrifileActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener((mAuthListener));
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.mSensorManager.unregisterListener((SensorEventListener)this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
        compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
    }

    public void signOut(){
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        compass.onSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getCurrentUserFriends() {
        // init a user with an arraylist of friends users.

        ArrayList<User> friends = new ArrayList<>();
        User friend1 = new User("dave", true, null);
        User friend2 = new User("mike", false, null);
        User friend3 = new User("sean", true, null);
        User friend4 = new User("ron", true, null);
        User friend5 = new User("keren", true, null);
        friends.add(friend1);
        friends.add(friend2);
        friends.add(friend3);
        friends.add(friend4);
        friends.add(friend5);

        friendsListView = (ListView)findViewById(R.id.friend_listView);
        currentUser = new User("moshe", true, friends);
        friendsListViewAdapter = new FriendListAdapter(friends, getApplicationContext());
        friendsListView.setAdapter(friendsListViewAdapter);
    }
}
