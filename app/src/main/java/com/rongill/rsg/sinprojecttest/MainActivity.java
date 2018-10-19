package com.rongill.rsg.sinprojecttest;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.Navigation.Compass;
import com.rongill.rsg.sinprojecttest.Navigation.Point;
import com.rongill.rsg.sinprojecttest.SignInPages.CreateUserPrifileActivity;
import com.rongill.rsg.sinprojecttest.SignInPages.LoginActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Firebase params
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //compass imageview
    private ImageView compassImage;

    //Sensor params
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth =0f;
    private float currentAzimuth =0f;
    private SensorManager mSensorManager;
    private int oriantationNew;
    private Compass compass;

    private ListView suggestionsListView;
    ArrayAdapter<String> suggestionsListViewAdapter;


    //
    private User user;

    ViewGroup listViewLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        listViewLayout = (ViewGroup)findViewById(R.id.listview_layout);

        compassImage = (ImageView)findViewById(R.id.compass_image);
        //init sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass = new Compass(compassImage, mSensorManager);

        suggestionsListView = (ListView)findViewById(R.id.suggestion_layout_listview);
        suggestionsListViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        suggestionsListView.setAdapter(suggestionsListViewAdapter);



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionsListViewAdapter.clear();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }
            }
        };



        //init current user with cridatianls
       // String userID = mAuth.getCurrentUser().getUid();
        //DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
       // user = new User("ron", userID, new Point(0,0) );
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
                        listViewLayout.bringToFront();
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

}
