package com.rongill.rsg.sinprojecttest.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.adapters.FriendListAdapter;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.DynamicIndoorNavigation;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.app_utilities.UserUtil;
import com.rongill.rsg.sinprojecttest.navigation.Compass;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.MyBleScanner;

import com.rongill.rsg.sinprojecttest.navigation.Point;
import com.rongill.rsg.sinprojecttest.navigation.StaticIndoorNavigation;
import com.rongill.rsg.sinprojecttest.services.InboxService;
import com.rongill.rsg.sinprojecttest.services.LiveLocationService;
import com.rongill.rsg.sinprojecttest.services.StructureMessageBoxService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//TODO generate some reports on user connectivity, last changed beacons and locations etc.

public class MainDrowerActivity extends AppCompatActivity implements SensorEventListener {

    private final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_BL = 2;
    private static final int NAVIGATION_REQUEST_CODE = 100;
    private static final int STATIC_NAV_RESULT_CODE = 200;
    private static final int LIVE_LOCATION_RESULT_CODE = 300;

    //Firebase vars
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //compass anim vars
    private SensorManager mSensorManager;
    private Compass compass;

    //search suggestions vars
    private ArrayAdapter<String> suggestionsListViewAdapter;
    private ViewGroup searchSuggestionsLayout;
    private ArrayList<Location> locationList;

    //user and friends data vars
    public static boolean userIsSet = false;
    private UserUtil mUserUtil;
    private ListView friendsListView;
    private FriendListAdapter friendsListViewAdapter;

    //Beacon scanner util
    private MyBleScanner myBleScanner;
    private boolean beaconLocated = false;

    //Navigation vars
    private StaticIndoorNavigation staticIndoorNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drower);

        // init firebase auth status listener.
        mAuth = FirebaseAuth.getInstance();

        mFirebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Log.i(TAG, "user login state changed, moved to LoginActivity");
                    startActivity(new Intent(MainDrowerActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionsListViewAdapter.clear();
            }
        });

        //set drawer layout & toggles.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        if (mAuth.getCurrentUser() == null) {
            Log.i(TAG, "user not signed in, move to LoginActivity");
            startActivity(new Intent(MainDrowerActivity.this, LoginActivity.class));
            finish();
        } else {

            mUserUtil = new UserUtil();
            mUserUtil.saveUserLoginDate(new MyCalendar());
            setBluetooth();



            setConnectionStatus(true);

            setSearchListView();

            //compass image view
            //init compass vars for nav view
            ImageView compassImage = (ImageView) findViewById(R.id.compass_image);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            compass = new Compass(compassImage, mSensorManager);

            final TextView userLocationTv = (TextView) findViewById(R.id.user_location_TV);
            final TextView structureMainTv = (TextView)findViewById(R.id.structure_name_main_tv);
            final TextView floorMainTv = (TextView)findViewById(R.id.floor_main_tv);
            final TextView beaconNameTv = (TextView)findViewById(R.id.beacon_main_tv);
            final TextView rssiTv = (TextView)findViewById(R.id.rssi_main_tv);

            //set the first scan and then init the location lists, user etc. so we can get the current user connection place.
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    myBleScanner.initLeScan(false);
                    if(userIsSet) {
                        setUserInbox();
                        setFriendAdapter();
                    }
                    //TODO debug what is the best way to retrieve the closest beacon, here we scan the list of beacon created and return the max rssi beacon
                    //if the scanner was able to scan SIN project beacons, set the TextView.
                    if (myBleScanner.getNearestBeacon().getRssi() > -150) {

                        mUserUtil.getCurrentUser().setCurrentBeacon(myBleScanner.getNearestBeaconFromList());

                        userLocationTv.setText(mUserUtil.getCurrentUser().getUsername() + "'s location:");
                        structureMainTv.setText("Structure- " + mUserUtil.getCurrentUser().getCurrentBeacon().getStructure());
                        floorMainTv.setText("Floor- " + mUserUtil.getCurrentUser().getCurrentBeacon().getFloor());
                        beaconNameTv.setText("Beacon connected- " + mUserUtil.getCurrentUser().getCurrentBeacon().getName());
                        rssiTv.setText("Rssi- " + mUserUtil.getCurrentUser().getCurrentBeacon().getRssi());




                        //if this is the first time a beacon is located in the building, will set the management inbox and location list for the building.
                        if(!beaconLocated) {
                            setLocationList();

                            //managementMessageService is for getting the distributed messages from management,
                            Intent managementMessageServiceIntent = new Intent(getBaseContext(), StructureMessageBoxService.class);
                            managementMessageServiceIntent.putExtra("STRUCTURE", mUserUtil.getCurrentUser().getCurrentBeacon().getStructure());
                            getBaseContext().startService(managementMessageServiceIntent);


                            //FAB transfers to Structure info page.
                            FloatingActionButton structureFabButton = findViewById(R.id.structure_page_fab);

                            structureFabButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mUserUtil.getCurrentUser() != null && mUserUtil.getCurrentUser().getCurrentBeacon() != null) {
                                        Intent intent = new Intent(getBaseContext(), StructureInfoActivity.class);
                                        intent.putExtra("STRUCTURE_NAME", mUserUtil.getCurrentUser().getCurrentBeacon().getStructure());
                                        startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
                                    }
                                }
                            });
                        }

                        beaconLocated = true;
                    } else {
                        String userLocationString = "couldn't find your location... TAP to rescan";
                        userLocationTv.setText(userLocationString);
                        structureMainTv.setText("Structure- N/A");
                        floorMainTv.setText("Floor- N/A");
                        beaconNameTv.setText("Beacon connected- N/A");
                        rssiTv.setText("Rssi- N/A");
                    }
                }
            };

            handler.postDelayed(r, 3000);


            //when image pressed, the scanning process starts/stops for 3 sec
            compassImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mUserUtil.getCurrentUser().setCurrentBeacon(new MyBeacon());
                    myBleScanner.getScannedDeviceList().clear();
                    myBleScanner.getNearestBeacon().setRssi(-150);
                    userLocationTv.setText("Scanning...");
                    structureMainTv.setText("Structure- Scanning...");
                    floorMainTv.setText("Floor- Scanning...");
                    beaconNameTv.setText("Beacon connected- Scanning...");
                    rssiTv.setText("Rssi- Scanning...");

                    myBleScanner.initLeScan(true);
                    handler.postDelayed(r, 3000);
                }
            });

        }

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

        //set visible to the maintenance item if user is a maintenance user.
        final MenuItem maintenanceItem = menu.findItem(R.id.maintenance_settings);
        final MenuItem managementItem = menu.findItem(R.id.management_settings);

        //set the maintenance menu item visibility if user is a maintenance user.
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mAuth.getUid()).child("user-type");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue().toString().equals("maintenance")) {
                        maintenanceItem.setVisible(true);
                    }
                    else if (dataSnapshot.getValue().toString().equals("management")){
                        managementItem.setVisible(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        // set the search item to be a searchView.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (suggestionsListViewAdapter != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (locationList != null) {
                        ArrayList<String> tempList = new ArrayList<>();
                        if (!newText.isEmpty()) {
                            for (Location temp : locationList) {
                                if (temp.getName().toLowerCase().contains(newText.toLowerCase())) {
                                    tempList.add(temp.getName());
                                }
                            }
                            searchSuggestionsLayout.bringToFront();
                            suggestionsListViewAdapter.clear();
                            suggestionsListViewAdapter.addAll(tempList);
                            suggestionsListViewAdapter.notifyDataSetChanged();

                        } else {
                            suggestionsListViewAdapter.clear();
                            suggestionsListViewAdapter.notifyDataSetChanged();
                        }
                    }

                    return true;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sign_out_menu_btn:
                signOut();
                break;
            case R.id.profile_menu_btn:
                startActivity(new Intent(this, CreateUserProfileActivity.class));
                break;
            case R.id.maintenance_settings:
                Intent maintenanceIntent = new Intent(this, LocationSettingActivity.class);
                maintenanceIntent.putExtra("USER", mUserUtil.getCurrentUser());
                startActivity(maintenanceIntent);
                break;

            case R.id.management_settings:
                Intent managementIntent = new Intent(this, ManagementActivity.class);
                managementIntent.putExtra("USER", mUserUtil.getCurrentUser());
                startActivity(managementIntent);
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener((mAuthListener));
        if (mAuth.getCurrentUser() != null) {
            setConnectionStatus(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compass != null) {
            compass.mSensorManager.unregisterListener((SensorEventListener) this);
        }
        mAuth.addAuthStateListener((mAuthListener));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compass != null) {
            compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
            compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO check why on minimize will set the status to disconnected.
        if(mAuth.getCurrentUser() != null ) setConnectionStatus(false);
        if(staticIndoorNavigation != null && !staticIndoorNavigation.hasArrived){
            staticIndoorNavigation.stopNavigation("stopped");
        } // TODO make the same condition on dynamic nav
    }

    public void signOut() {

        startActivity(new Intent(this, LoginActivity.class));
        setConnectionStatus(false);
        mAuth.signOut();
        myBleScanner.initLeScan(false);
        finish();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        compass.onSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //change the connection status in database according to the status var.
    private void setConnectionStatus(boolean status) {
        if (mAuth != null) {
            DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(mFirebaseUser.getUid()).child("status");
            if (status)
                mUserRef.setValue("connected");
            else mUserRef.setValue("disconnected");
        }
    }

    //set the Locations for the search suggestions and more.
    private void setLocationList() {
        locationList = new ArrayList<>();

        DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(mUserUtil.getCurrentUser().getCurrentBeacon().getStructure()).child("locations");

        locationReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Location tempLocation = new Location();

                tempLocation.setName(dataSnapshot.getValue(Location.class).getName());
                tempLocation.setBeaconName(dataSnapshot.getValue(Location.class).getBeaconName());
                tempLocation.setCategory(dataSnapshot.getValue(Location.class).getCategory());
                tempLocation.setFloor(dataSnapshot.getValue(Location.class).getFloor());
                tempLocation.setStructure(dataSnapshot.getValue(Location.class).getStructure());

                Point p = new Point();
                p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                tempLocation.setCoordinates(p);

                locationList.add(tempLocation);
                suggestionsListViewAdapter.notifyDataSetChanged();
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String locationName = dataSnapshot.getValue(Location.class).getName();
                for (Location temp : locationList) {
                    if (temp.getName().equals(locationName)) {
                        int index = locationList.indexOf(temp);

                        locationList.get(index).setName(dataSnapshot.getValue(Location.class).getName());
                        locationList.get(index).setBeaconName(dataSnapshot.getValue(Location.class).getBeaconName());
                        locationList.get(index).setCategory(dataSnapshot.getValue(Location.class).getCategory());
                        locationList.get(index).setStructure(dataSnapshot.getValue(Location.class).getStructure());
                        locationList.get(index).setFloor(dataSnapshot.getValue(Location.class).getFloor());

                        Point p = new Point();
                        p.setX(Integer.parseInt(dataSnapshot.child("x").getValue().toString()));
                        p.setY(Integer.parseInt(dataSnapshot.child("y").getValue().toString()));

                        locationList.get(index).setCoordinates(p);

                        suggestionsListViewAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String locationName = dataSnapshot.getValue(Location.class).getName();
                for (Location temp : locationList) {
                    if (temp.getName().equals(locationName)) {
                        locationList.remove(temp);
                        suggestionsListViewAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //setting the search bar to the locationList var retrieved from DB earlier.
    //setting on click for the search item to transfer to the location page.
    public void setSearchListView() {
        //search bar in appbar main drawer layout.
        searchSuggestionsLayout = (ViewGroup) findViewById(R.id.suggestion_layout);
        final ListView suggestionsListView = (ListView) findViewById(R.id.suggestion_listview);
        suggestionsListViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        suggestionsListView.setAdapter(suggestionsListViewAdapter);
        suggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //by clicking one of the searched location, will be transfered to the location page.
                String locationName = (String) suggestionsListView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), LocationInfoActivity.class);
                intent.putExtra("LOCATION_NAME", locationName);
                intent.putExtra("STRUCTURE", mUserUtil.getCurrentUser().getCurrentBeacon().getStructure());
                startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
                suggestionsListViewAdapter.clear();
                suggestionsListViewAdapter.notifyDataSetChanged();

            }
        });
    }

    //Builds the users inbox.
    private void setUserInbox() {
        Intent startInboxServiceIntent = new Intent(this, InboxService.class);
        startInboxServiceIntent.putExtra("CURRENT_USER", mUserUtil.getCurrentUser());
        this.startService(startInboxServiceIntent);

    }

    //getting the user friend list from the database and update the friendList ArrayList object. set to friendListViewAdapter
    private void setFriendAdapter() {
        DatabaseReference userFriendListRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());
        friendsListView = (ListView) findViewById(R.id.friend_listView);
        friendsListViewAdapter = new FriendListAdapter(new ArrayList<String>(), getApplicationContext(), userFriendListRef);
        friendsListView.setAdapter(friendsListViewAdapter);

        //Reference to the current user-friend structure.
        DatabaseReference currentUserFriendsDb = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());

        //add listener to any changes in the users friend list, any change will result in a refresh of the friend listview by adding updated user friend uid list.
        currentUserFriendsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        //on click listener to transfer to the friend profile page.
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFriendUid = (String) friendsListView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), FriendProfileActivity.class);
                intent.putExtra("FRIEND_UID", selectedFriendUid);
                intent.putExtra("CURRENT_USER", mUserUtil.getCurrentUser());
                startActivityForResult(intent, NAVIGATION_REQUEST_CODE);
            }
        });
    }

    //function too add a friend to user friend list in db by email.
    public void inviteFriend(View v) {

        // define the input text view with margin, hint etc.
        final EditText friendEmailInput = new EditText(this);
        friendEmailInput.setHint("Enter Email");
        friendEmailInput.setText("");
        friendEmailInput.setSingleLine();
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 10;
        params.rightMargin = 10;
        friendEmailInput.setLayoutParams(params);
        container.addView(friendEmailInput);

        AlertDialog.Builder addFriendAD = new AlertDialog.Builder(this);
        addFriendAD.setView(container);
        addFriendAD.setTitle("Add A Friend");

        addFriendAD.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //if the input is the current user Email, ignore.
                if(friendEmailInput.getText().toString().toLowerCase().equals(
                        FirebaseAuth.getInstance().getCurrentUser().getEmail().toLowerCase())){
                    makeToast("Please enter a friend Email and not yours!");
                } else {
                    final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference()
                            .child("users");
                    Query query = mRef.orderByChild("email").equalTo(friendEmailInput.getText().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    //If friend not exist send a request message
                                    if (!mUserUtil.checkIfFriendExist(ds.getKey())) {
                                        RequestMessage message = new RequestMessage(ds.getKey(), mUserUtil.getCurrentUser().getUserId(),
                                                mUserUtil.getCurrentUser().getUsername(), "friend request", "pending");
                                        message.sendRequest(message);
                                        makeToast("Friend request sent to " + ds.child("username").getValue().toString());
                                    } else {
                                        makeToast("Friend already in your list");
                                    }
                                }
                            } else {
                                makeToast("Friend Not Found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        addFriendAD.setNegativeButton("close", null);
        addFriendAD.show();
    }

    //init the bluetoothLE vars, check for permissions.
    private void setBluetooth() {
        //Check if device does support BT by hardware
        if (!getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            //Toast shows a message on the screen for a LENGTH_SHORT period
            Toast.makeText(this, "BLUETOOTH NOT SUPPORTED!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Check if device does support BT Low Energy by hardware. Else close the app(finish())!
        if (!getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast shows a message on the screen for a LENGTH_SHORT period
            Toast.makeText(this, "BLE NOT SUPPORTED!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBTAdapter == null) {
                Toast.makeText(this, "ERROR GETTING BLUETOOTH ADAPTER!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                //Check if BT is enabled! This method requires BT permissions in the manifest.
                if (!mBTAdapter.isEnabled()) {
                    //If it is not enabled, ask user to enable it with default BT enable dialog! BT enable response will be received in the onActivityResult method.
                    Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTintent, PERMISSION_REQUEST_COARSE_BL);
                }
            }
            //init the BLE scanner
            myBleScanner = new MyBleScanner((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));

            //start the first scan
            myBleScanner.initLeScan(true);
        }

        //check if has Location permissions
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //If Location permissions are not granted for the app, ask user for it! Request response will be received in the onRequestPermissionsResult.
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }


    }

    //check if user granted permissions to Location.
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        //Check if permission request response is from Location
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //User granted permissions. Setup the scan settings
                    Log.d("TAG", "coarse location permission granted");
                } else {
                    //User denied Location permissions. Here you could warn the user that without
                    //Location permissions the app is not able to scan for BLE devices and eventually
                    //Close the app
                    finish();
                }
            }
        }
    }

    //handle activity result codes, result from BLE permission intent and more...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if the response is from BT
        if (requestCode == PERMISSION_REQUEST_COARSE_BL) {
            // User chose not to enable Bluetooth.
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }

        //handles static/dynamic navigation result codes from LocationInfoActivity & FriendProfileActivity
        //pull the destination from the Intent, create a static navigation object, start the navigation
        switch (resultCode) {
            case STATIC_NAV_RESULT_CODE:
                Bundle extras = data.getExtras();
                if (extras != null) {

                    final Location destination = (Location) extras.getSerializable("LOCATION");

                    compass.titleTv = (TextView) findViewById(R.id.action_display_view);
                    compass.setUserLocationTv((TextView) findViewById(R.id.user_location_TV));

                    String displayString = "Navigation to " + destination.getName() + "started";
                    compass.titleTv.setText(displayString);
                    compass.compassImage.setClickable(false);

                    Map<String, String> newPost = new HashMap<>();
                    newPost.put("status", "started");
                    newPost.put("navigation-type", "static");

                    DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                            .child("users-navigation-log").child(mUserUtil.getCurrentUser().getUserId());

                    final String navigationLogKey = userNavigationLogRef.push().getKey();

                    userNavigationLogRef.child(navigationLogKey).setValue(newPost);
                    userNavigationLogRef.child(navigationLogKey).child("date-started").setValue(new MyCalendar());
                    userNavigationLogRef.child(navigationLogKey).child("destination").setValue(destination).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            staticIndoorNavigation = new StaticIndoorNavigation(getBaseContext(), mUserUtil.getCurrentUser(),destination, compass, navigationLogKey);
                            staticIndoorNavigation.startNavigation();
                            Log.i(TAG, "Static navigation started");
                        }
                    });

                    //update user status to navigating, will prevent multiple navigation sessions.
                    DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserUtil.getCurrentUser().getUserId()).child("status");
                    userStatusRef.setValue("navigating");


                }
                break;
                //coming back from the friend profile page, set the users navigation log entry
            //TODO prevent multiple dynamic nav request.
            case LIVE_LOCATION_RESULT_CODE:

                final RequestMessage message= (RequestMessage)data.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");
                final String friendInboxPushKey = data.getStringExtra("NAVIGATION_RM_KEY");
                final User friend = (User)data.getSerializableExtra("FRIEND_USER");


                final DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                        .child("users-inbox").child(friend.getUserId()).child(friendInboxPushKey);

                friendInboxRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            MyCalendar dateStarted = dataSnapshot.child("dateCreated").getValue(MyCalendar.class);
                            //if message not to old, deleted by friend or older the 15m
                            if (dateStarted.timeDiffInSeconds(new MyCalendar()) < 900) {
                                if (dataSnapshot.child("requestStatus").getValue().toString().contains("confirmed")) {

                                    //TODO check if this works, the confirmed string contains also the friend nav log key for this user to listen to any changes.
                                    String friendNavLogPushKey = dataSnapshot.child("requestStatus").getValue().toString().substring(10);

                                    Map<String, String> newPost = new HashMap<>();
                                    newPost.put("destination-uid", message.getReceiverUid());
                                    newPost.put("destination-username", ((User) (data.getSerializableExtra("FRIEND_USER"))).getUsername());
                                    newPost.put("destination-beacon", "my-live-location");
                                    newPost.put("status", "sharing");
                                    newPost.put("navigation-type", "share-live-location");

                                    final DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users-navigation-log")
                                            .child(message.getSenderUid());

                                    final String pushKey = userNavigationLogRef.push().getKey();
                                    userNavigationLogRef.child(pushKey).setValue(newPost);
                                    userNavigationLogRef.child(pushKey).child("date-started").setValue(new MyCalendar());


                                    //If friend confirmed the navigation to user live location, liveLocation service will start and the user navigation log will be created.
                                    Intent shareLiveLocationServiceIntent = new Intent(getBaseContext(), LiveLocationService.class);
                                    shareLiveLocationServiceIntent.putExtra("CURRENT_USER", mUserUtil.getCurrentUser());
                                    shareLiveLocationServiceIntent.putExtra("NAVIGATION_PUSHKEY", pushKey);
                                    shareLiveLocationServiceIntent.putExtra("FRIEND_NAV_LOG_PUSHKEY", friendNavLogPushKey);
                                    shareLiveLocationServiceIntent.putExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE", message);
                                    getBaseContext().startService(shareLiveLocationServiceIntent);

                                    //update user status to navigating, will prevent multiple navigation sessions.
                                    DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserUtil.getCurrentUser().getUserId()).child("status");
                                    userStatusRef.setValue("navigating");

                                    //TODO make a stop nav btn visible and actionable.
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                break;
        }


    }

    //this override is for the user confirmed Dynamic navigation from InboxService notification.
    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
        if(intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE") != null){
            Log.d(TAG, "Dynamic navigation started at receiver side.");
            //start the dynamic nav here with a service. create a navigation log entry to the current user
            //get the message from the inbox service, init the user nav log with the friend UID as destination.
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(2);

            RequestMessage dynamicNavRequestMessage = (RequestMessage)intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");

            Map<String,String> newPost = new HashMap<>();

            newPost.put("destination-uid", dynamicNavRequestMessage.getSenderUid());
            newPost.put("destination-username", dynamicNavRequestMessage.getSenderUsername());
            newPost.put("destination-beacon","");
            newPost.put("status", "started");
            newPost.put("navigation-type", "dynamic-to-live-location");

            DatabaseReference userNavigationLogRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-navigation-log").child(dynamicNavRequestMessage.getReceiverUid());

            String pushKey = userNavigationLogRef.push().getKey();
            userNavigationLogRef.child(pushKey).setValue(newPost);
            userNavigationLogRef.child(pushKey).child("date-started").setValue(new MyCalendar());

            //let the friend know that the user confirmed the navigation.
            DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-inbox").child(dynamicNavRequestMessage.getReceiverUid())
                    .child(intent.getStringExtra("CONFIRMED_MESSAGE_KEY"));
            userInboxRef.child("requestStatus").setValue("confirmed-" + pushKey);

            //update user status to navigating, will prevent multiple navigation sessions.
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserUtil.getCurrentUser().getUserId()).child("status");
            userStatusRef.setValue("navigating");

            //wait for friend to start scanning and updating beacon name in DB
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DynamicIndoorNavigation dynamicIndoorNavigation = new DynamicIndoorNavigation(this, mUserUtil.getCurrentUser(),dynamicNavRequestMessage, compass, pushKey);

            //TODO make a stop nav btn visible and actionable.

        }
        super.onNewIntent(intent);
    }



    private void makeToast(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }







}


