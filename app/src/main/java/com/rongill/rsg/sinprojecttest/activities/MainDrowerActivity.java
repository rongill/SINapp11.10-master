package com.rongill.rsg.sinprojecttest.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.ArrayList;

public class MainDrowerActivity extends AppCompatActivity implements SensorEventListener{

    private final String TAG = "MainActivity";
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

    //friend data vars
    private UserUtil mUserUtil;

    private ListView friendsListView;
    private FriendListAdapter friendsListViewAdapter;

    //user Inbox vars
    private InboxUtil mInboxUtil;

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
                    intent.putExtra("MESSAGES", mInboxUtil.getUserInbox().getMessages());
                    startActivity(intent);
                }


            }
        });

        //SearchView list and adapter vars, set onItemClick intent to transfer to location page.
        setSearchListView();

        //compass imageview
        //init compass vars for nav view
        ImageView compassImage = (ImageView) findViewById(R.id.compass_image);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass = new Compass(compassImage, mSensorManager);

        if(mAuth.getCurrentUser()!=null){

            setConnectionStatus(true);
            mUserUtil = new UserUtil();

            DatabaseReference userFriendListRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-friends").child(mFirebaseUser.getUid());

            friendsListView = (ListView)findViewById(R.id.friend_listView);

            friendsListViewAdapter = new FriendListAdapter(new ArrayList<String>(), getApplicationContext(), userFriendListRef);
            friendsListView.setAdapter(friendsListViewAdapter);
            refreshFriendAdapter();
            setUserInbox();
            setLocationList();
        }
    }

    private void setLocationList(){
        locationList = new ArrayList<>();
        DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference()
                .child("locations");
        locationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Location tempLocation;
                locationList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    tempLocation = ds.getValue(Location.class);
                    locationList.add(tempLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        if(mAuth!=null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getUid()).child("user-type");
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
                break;
            case R.id.maintenance_settings:
                Intent intent = new Intent(this, LocationSettingActivity.class );
                intent.putExtra("LOCATION_LIST", locationList);
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

    //getting the user friend list from the database and update the friendList ArrayList object. set to friendListViewAdapter
    private void refreshFriendAdapter() {

        //Reference to the current user-friend structure.
        DatabaseReference currentUserFriendsDb = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());
        //add listener to any changes in the users friend list, any change will result in a refresh of the friend listview by adding updated user friend uid list.
        currentUserFriendsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsListViewAdapter.myAddAll(new ArrayList<String>());
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

    //Builds the users inbox.
    private void setUserInbox(){
        DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(mFirebaseUser.getUid());
        mInboxUtil = new InboxUtil(userInboxRef);


    }




}


