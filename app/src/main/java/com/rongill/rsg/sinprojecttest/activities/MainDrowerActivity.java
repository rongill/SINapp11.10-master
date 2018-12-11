package com.rongill.rsg.sinprojecttest.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rongill.rsg.sinprojecttest.app_utilities.InboxUtil;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.app_utilities.UserUtil;
import com.rongill.rsg.sinprojecttest.navigation.Compass;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.util.ArrayList;
import java.util.List;

public class MainDrowerActivity extends AppCompatActivity implements SensorEventListener{

    private final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_BL = 2;
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
    private UserUtil mUserUtil;
    private ListView friendsListView;
    private FriendListAdapter friendsListViewAdapter;

    //user Inbox vars
    private InboxUtil mInboxUtil;

    //bluetooth vars
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private List<MyBeacon> scannedDeivcesList;

    //TODO !!! how to stop the app when minimized!!! need this for stop scanning or deferent scanning, and for signout method.
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
                if(firebaseAuth.getCurrentUser() == null){
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


        //drawer.addDrawerListener(new DrawerLayout.DrawerListener() {});
        toggle.syncState();

        //FAB transfers to Structure info page.
        FloatingActionButton structureFabButton = findViewById(R.id.structure_page_fab);
        structureFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StructureInfoActivity.class);
                intent.putExtra("LOCATION_LIST", locationList);
                startActivity(intent);
            }
        });

        FloatingActionButton inboxFabButton = findViewById(R.id.inbox_fab);
        inboxFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mInboxUtil!=null) {
                    Intent intent = new Intent(getBaseContext(), InboxActivity.class);
                    startActivity(intent);
                }


            }
        });

        //compass imageview
        //init compass vars for nav view
        ImageView compassImage = (ImageView) findViewById(R.id.compass_image);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass = new Compass(compassImage, mSensorManager);



        if(mAuth.getCurrentUser()!=null){

            mUserUtil = new UserUtil();

            setConnectionStatus(true);
            setSearchListView();
            setFriendAdapter();
            setUserInbox();
            setLocationList();

            setBluetooth();
            scannedDeivcesList = new ArrayList<>();
            //start the scan
            initLeScan(true);


            final TextView userLocationTv = (TextView)findViewById(R.id.user_location_TV);
            userLocationTv.setText("detecting...");

            //init a handler to execute after 5sec the stopLeScan Runnable obj.
            Handler mHandler = new Handler();
            Runnable stopLeScanThread = new Runnable(){
                @Override
                public void run() {
                    initLeScan(false);

                    if(scannedDeivcesList.size()>0){
                        mUserUtil.getCurrentUser().setCurrentBeacon(findClosestBeacon());
                        userLocationTv.setText(mUserUtil.getCurrentUser().getCurrentBeacon().getName());
                    } else {
                        userLocationTv.setText("Unknown Location");
                    }

                }
            };
            mHandler.postDelayed(stopLeScanThread, 5000);
            //mHandler.removeCallbacks(leScanThread);
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

        //set the maintenance menu item visibility if user is a maintenance user.
        if(mAuth.getCurrentUser()!=null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mAuth.getUid()).child("user-type");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue().toString().equals("maintenance")){
                        maintenanceItem.setVisible(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        // set the search item to be a searchView.
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
                if(!newText.isEmpty()) {
                    for (Location temp : locationList) {
                        if (temp.getName().toLowerCase().contains(newText.toLowerCase())) {
                            tempList.add(temp.getName());
                        }
                    }
                    searchSuggestionsLayout.bringToFront();
                    suggestionsListViewAdapter.clear();
                    suggestionsListViewAdapter.addAll(tempList);
                    suggestionsListViewAdapter.notifyDataSetChanged();

                }else{
                    suggestionsListViewAdapter.clear();
                    suggestionsListViewAdapter.notifyDataSetChanged();
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
                startActivity(new Intent(this, CreateUserProfileActivity.class));
                break;
            case R.id.maintenance_settings:
                Intent intent = new Intent(this, LocationSettingActivity.class );
                startActivity(intent);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener((mAuthListener));
        if(mAuth.getCurrentUser()!=null) setConnectionStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.mSensorManager.unregisterListener((SensorEventListener)this);
        mAuth.addAuthStateListener((mAuthListener));

    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
        compass.mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public void signOut(){

        startActivity(new Intent(this, LoginActivity.class));
        setConnectionStatus(false);
        mAuth.signOut();
        finish();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        compass.onSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    //change the connection status in database according to the status var.
    //TODO figure out how to disconnect the user when app is closed.
    private void setConnectionStatus(boolean status){
        if(mAuth != null) {
            DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users")
                   .child(mFirebaseUser.getUid()).child("status");
            if(status)
                mUserRef.setValue("connected");
            else mUserRef.setValue("disconnected");
        }
    }

    //set the Locations for the search suggestions and more.
    private void setLocationList(){
        locationList = new ArrayList<>();

        DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference()
                .child("locations");

        locationReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Location tempLocation = new Location();

                tempLocation.setName(dataSnapshot.getValue(Location.class).getName());
                tempLocation.setBeacon(dataSnapshot.getValue(Location.class).getBeacon());
                tempLocation.setCategory(dataSnapshot.getValue(Location.class).getCategory());

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
                for(Location temp : locationList){
                    if(temp.getName().equals(locationName)){
                        int index = locationList.indexOf(temp);

                        locationList.get(index).setName(dataSnapshot.getValue(Location.class).getName());
                        locationList.get(index).setBeacon(dataSnapshot.getValue(Location.class).getBeacon());
                        locationList.get(index).setCategory(dataSnapshot.getValue(Location.class).getCategory());
                        locationList.get(index).getCoordinates().setX(dataSnapshot.getValue(Point.class).getX());
                        locationList.get(index).getCoordinates().setY(dataSnapshot.getValue(Point.class).getY());
                        suggestionsListViewAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String locationName = dataSnapshot.getValue(Location.class).getName();
                for(Location temp : locationList) {
                    if (temp.getName().equals(locationName)) {
                        locationList.remove(temp);
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
    public void setSearchListView(){
        //search bar in appbar main drawer layout.
        searchSuggestionsLayout = (ViewGroup)findViewById(R.id.suggestion_layout);
        final ListView suggestionsListView = (ListView) findViewById(R.id.suggestion_listview);
        suggestionsListViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        suggestionsListView.setAdapter(suggestionsListViewAdapter);
        suggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //by clicking one of the searched location, will be transfered to the location page.
                String locationName = (String)suggestionsListView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), LocationInfoPage.class);
                intent.putExtra("LOCATION_NAME", locationName);
                startActivity(intent);
            }
        });
    }

    //Builds the users inbox.
    private void setUserInbox(){
        DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(mFirebaseUser.getUid());
        mInboxUtil = new InboxUtil(userInboxRef);


    }

    //getting the user friend list from the database and update the friendList ArrayList object. set to friendListViewAdapter
    private void setFriendAdapter() {
        DatabaseReference userFriendListRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());
        friendsListView = (ListView)findViewById(R.id.friend_listView);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        //on click listener to transfer to the friend profile page.
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFriendUid = (String)friendsListView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), FriendProfileActivity.class);
                intent.putExtra("FRIEND_UID", selectedFriendUid);
                intent.putExtra("CURRENT_USER", mUserUtil.getCurrentUser());
                startActivity(intent);
            }
        });
    }

    //function too add a friend to user friend list in db by email.
    public void inviteFriend(View v){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference()
                .child("users");
        final AlertDialog.Builder addFriendAD = new AlertDialog.Builder(this);
        final EditText friendEmailInput = new EditText(this);
        friendEmailInput.setText("");
        addFriendAD.setView(friendEmailInput);
        addFriendAD.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Query query = mRef.orderByChild("email").equalTo(friendEmailInput.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){

                            if(!mUserUtil.checkIfFriendExist(ds.getKey())) {
                                RequestMessage message = mInboxUtil.setRequestMessage(ds.getKey(), mUserUtil.getCurrentUser().getUserId(),
                                        mUserUtil.getCurrentUser().getUsername(),"friend request");
                                mInboxUtil.sendRequest(message);

                            } else {
                                Toast.makeText(getBaseContext(), "Friend already in your list", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        addFriendAD.setNegativeButton("close", null);
        addFriendAD.show();
    }

    //init the bluetoothLE vars, check for permissions.
    private void setBluetooth(){
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
            if(mBTAdapter==null){
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
        }

        //check if has Location permissions
        if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //If Location permissions are not granted for the app, ask user for it! Request response will be received in the onRequestPermissionsResult.
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        //Create the scan settings
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        //Set scan latency mode. Lower latency, faster device detection/more battery and resources consumption
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //Wrap settings together and save on a settings var (declared globally).
        scanSettings = scanSettingsBuilder.build();
        //Get the BLE scanner from the BT adapter (var declared globally)
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check if the response is from BT
        if(requestCode == PERMISSION_REQUEST_COARSE_BL){
            // User chose not to enable Bluetooth.
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //start/stop the leScan.
    private void initLeScan(boolean state){
        if(state){
            scanner.startScan(null, scanSettings, mScanCallback);
        } else {
            scanner.stopScan(mScanCallback);
        }
    }

    //here all scanned devices will show on var result.
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            MyBeacon scannedBeacon = new MyBeacon();
            scannedBeacon.setMACaddress(result.getDevice().getAddress());
            scannedBeacon.setName(result.getDevice().getName());
            scannedBeacon.setRssi(result.getRssi());
            boolean contains = false;

            //if scanned device already in the list, update it.
            for (int i=0; i<scannedDeivcesList.size(); i++) {
                if (scannedDeivcesList.get(i).getMACaddress().contains(result.getDevice().getAddress())) {
                    contains = true;
                    scannedDeivcesList.set(i, scannedBeacon);
                }
            }


            //add to the list all the devices scanned that has "SIN-PROJECT" string in the name.
            if(!contains) {
                if (result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")) {
                    scannedBeacon.setName(scannedBeacon.getName().substring(4));
                    scannedDeivcesList.add(scannedBeacon);

                }
            }
        }
    };

    //find the closest beacon from the deviceList based on RSSI and return it
    private MyBeacon findClosestBeacon(){
        MyBeacon proximityMaxBeacon = scannedDeivcesList.get(0);
        for(MyBeacon temp : scannedDeivcesList){
            if(temp.getRssi() > proximityMaxBeacon.getRssi()){
                proximityMaxBeacon = temp;
            }
        }
        return proximityMaxBeacon;
    }







}


