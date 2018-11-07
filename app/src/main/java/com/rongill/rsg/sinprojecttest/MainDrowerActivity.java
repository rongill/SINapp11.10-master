package com.rongill.rsg.sinprojecttest;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.rongill.rsg.sinprojecttest.navigation.Compass;
import com.rongill.rsg.sinprojecttest.signIn_pages.CreateUserPrifileActivity;
import com.rongill.rsg.sinprojecttest.signIn_pages.LoginActivity;
import com.rongill.rsg.sinprojecttest.signIn_pages.RequestMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    ArrayAdapter<String> suggestionsListViewAdapter;
    ViewGroup searchSuggestionsLayout;

    //friend data vars
    private ArrayList <User> friendList;
    private ListView friendsListView;
    private FriendListAdapter friendsListViewAdapter;

    //Inbox vars
    //TODO consider replacing the array list of request messages with a paren class.
    private ArrayList<RequestMessage> requestMessageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drower);

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

        //FAB transfers to Structure info page.
        FloatingActionButton structureFabButton = findViewById(R.id.structure_page_fab);
        structureFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StructureInfoActivity.class);
                startActivity(intent);
            }
        });

        //SearchView list and adapter vars, set onItemClick intent to transfer to location page.
        setSearchListView();

        //compass imageview
        //init compass vars for nav view
        ImageView compassImage = (ImageView) findViewById(R.id.compass_image);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass = new Compass(compassImage, mSensorManager);

        // init firebase auth status listener.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainDrowerActivity.this, LoginActivity.class));
                }else{ setConnectionStatus(true); }
            }
        };

        //inbox setup
        requestMessageList = new ArrayList<>();

        //friend data setup
        friendList = new ArrayList<>();
        friendsListView = (ListView)findViewById(R.id.friend_listView);
        friendsListViewAdapter = new FriendListAdapter(friendList, getApplicationContext());
        friendsListView.setAdapter(friendsListViewAdapter);

        //oncreate database methods, need to logged in to activate (excludes first time users or signed out users)
        if(mAuth.getCurrentUser()!=null){
            setConnectionStatus(true);
            setCurrentUserFriends();
            setUserInbox();
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
                //TODO 1.3 get the Location data from server and init to locationData ArrayList
                ArrayList<String> locationData = new ArrayList<>(LocationData.getLocations());
                Collections.sort(locationData);
                if(!newText.isEmpty()) {
                    for (String temp : locationData) {
                        if (temp.toLowerCase().contains(newText.toLowerCase())) {
                            tempList.add(temp);
                        }
                        searchSuggestionsLayout.bringToFront();
                        suggestionsListViewAdapter.clear();
                        suggestionsListViewAdapter.addAll(tempList);

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
        if(mAuth.getCurrentUser()!=null) setConnectionStatus(false);
    }

    @Override
    protected void onDestroy() {
        if(mAuth.getCurrentUser()!=null) setConnectionStatus(false);
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

    //getting the user friend list from the database and update the friendList ArrayList object. set to friendListViewAdapter
    private void setCurrentUserFriends() {

        //Reference to the current user-friend structure.
        DatabaseReference currentUserFriendsDb = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid());

        //add listener to any changes in the users friend list, any change will result in a refresh of the friend listview.
        currentUserFriendsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String friendId = ds.getKey();
                    addNewFriendByUid(friendId);

                }
                friendsListViewAdapter.clear();
                friendsListViewAdapter.addAll(friendList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //on click listener to transfer to the friend profile page.
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedFriend = (User)friendsListView.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), FriendProfileActivity.class);
                intent.putExtra("FRIEND_NAME", selectedFriend.getUsername());
                intent.putExtra("CONNECTION_STATUS", selectedFriend.getStatus());
                startActivity(intent);
            }
        });
    }

    //method that receives a friend UID and gets the object from the user structure. adds to  the friend list object, and notify the change in the adapter.
    private void addNewFriendByUid(final String friendId){

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User friendUser = new User();
                friendUser.setUserId(friendId);
                friendUser.setUsername(dataSnapshot.getValue(User.class).getUsername());
                friendUser.setStatus(dataSnapshot.getValue(User.class).getStatus());
                friendList.add(friendUser);
                friendsListViewAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

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

    //function too add a friend to user friend list in db by email.
    public void inviteFriend(View v){
       final String userId = mFirebaseUser.getUid();
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

                           if(!checkIfFriendExist(ds.getKey())) {
                               setRequestToFriend(ds.getKey(),"friendRequest");
                           } else {
                               Toast.makeText(getBaseContext(), "Friend allready in your list", Toast.LENGTH_SHORT).show();
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
    //change the connection status in database according to the status var.
    private void setConnectionStatus(final boolean status){
        if(mAuth != null) {
            final DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(mFirebaseUser.getUid()).child("status");
            if(status)
                mUserRef.setValue("connected");
            else mUserRef.setValue("disconnected");
        }
    }

    //checks if friend in already in the friend list (before adding)
    public boolean checkIfFriendExist(String userId){
        for(User friend : friendList){
            if(friend.getUserId().equals(userId)) return true;
        }
        return false;
    }

    //TODO redundent, remove the button also
    public void refreshFriendList (View v){
        setCurrentUserFriends();
    }

    //Buildes the users request inbox.
    private void setUserInbox(){
        DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference().child("users-inbox").child(mFirebaseUser.getUid());
        userInboxRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RequestMessage tempInboxItem = dataSnapshot.getValue(RequestMessage.class);
                requestMessageList.add(tempInboxItem);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //create a request in database (type: friend request, nav requset, poke ...)
    private void setRequestToFriend(final String receiverUid, String requestType){

        Map<String,String> newPost = new HashMap<>();
        newPost.put("request-type",requestType);
        newPost.put("request-status","false");

        //get ref to the new request and set the values.
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(receiverUid);
        mRef.child(mFirebaseUser.getUid()).setValue(newPost);

        //reference to the request status, and add listener, when status is true, the request is confirmed.
        DatabaseReference requestStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(receiverUid).child(mFirebaseUser.getUid()).child("request-status");
        requestStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("true")){
                    addFriendInDatabase(receiverUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //add new friend in the database with a connection status, set listener to the friend status fireld in user structure to be equal.
    // any change in status of a friend will result a friend list refresh.
    private void addFriendInDatabase(String friendUid){

        final DatabaseReference userFriendsListRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mFirebaseUser.getUid()).child(friendUid);

        Map<String,String> newPost = new HashMap<>();
        newPost.put("status","disconnected");

        userFriendsListRef.setValue(newPost)
                .addOnCompleteListener(MainDrowerActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getBaseContext(), "Friend added to your list", Toast.LENGTH_SHORT).show();
                        } else {
                            //TODO figure how to handle an event where the email not found in firebase
                        }
                    }
                });
        //set a listener to the user structure of the friend status, when changed (connected/disconnected) will update the users friend structure.
        DatabaseReference statusUpdaterRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friendUid).child("status");
        statusUpdaterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFriendsListRef.child("status").setValue(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}


