package com.rongill.rsg.sinprojecttest.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.basic_objects.ManagementUser;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

import java.util.HashMap;
import java.util.Map;

public class ManagementActivity extends AppCompatActivity {

    private ManagementUser managementUser;
    private TextView structureNameTV;
    private TextView structureAdressTV;
    private TextView structureMaintenancePersonalTV;
    private TextView managementNameTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setManagementUser();

        structureNameTV = (TextView)findViewById(R.id.structure_name_tv);
        structureAdressTV = (TextView)findViewById(R.id.structure_address_tv);
        structureMaintenancePersonalTV = (TextView)findViewById(R.id.structure_maintenance_personal_tv);
        managementNameTV = (TextView) findViewById(R.id.management_account_name);


        FloatingActionButton fab = findViewById(R.id.publish_message_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent messageBoxIntent = new Intent(getBaseContext(), MessageBox.class);
                //messageBoxIntent.putExtra(managementUser.getStructure(),"STRUCTURE");
                createMessageDialog();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.management_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_maintenance_user:
                addMaintenanceUser();
        }
        return super.onOptionsItemSelected(item);
    }



    //set the management user details and structure.
    private void setManagementUser() {
        Intent intent = getIntent();
        User tempUser = (User) intent.getSerializableExtra("USER");
        managementUser = new ManagementUser(
                tempUser.getUserId(), tempUser.getUsername(),
                tempUser.getStatus(), tempUser.getUserType(), "");

        DatabaseReference managementUserRef = FirebaseDatabase.getInstance().getReference()
                .child("management-users").child(managementUser.getUserId());
        managementUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                managementUser.setStructure(dataSnapshot.child("structure").getValue().toString());
                managementNameTV.setText("Account name: " + managementUser.getUsername());
                structureNameTV.setText(managementUser.getStructure());
                structureAdressTV.setText("Address: " + dataSnapshot.child("address").getValue().toString());
                structureMaintenancePersonalTV.setText("Maintenance personal: ");
                readMaintenancePersonal(managementUser.getStructure());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMaintenancePersonal(String structureName) {
        DatabaseReference structureMaintenancePersonalRef = FirebaseDatabase.getInstance().getReference()
                .child("structures").child(structureName).child("maintenance-personal");
        structureMaintenancePersonalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = (int)dataSnapshot.getChildrenCount();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    structureMaintenancePersonalTV.append(ds.child("username").getValue().toString());
                    if(counter > 1)
                        structureMaintenancePersonalTV.append(", ");
                    counter--;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //create an AlertDialog that takes the Email input and add Maintenance users to the current building.
    private void addMaintenanceUser() {

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter Email");
        emailInput.setText("");
        emailInput.setSingleLine(true);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 10;
        params.rightMargin = 10;
        emailInput.setLayoutParams(params);
        container.addView(emailInput);

        AlertDialog.Builder addMaintenanceAD = new AlertDialog.Builder(this);
        addMaintenanceAD.setView(container);
        addMaintenanceAD.setTitle("Assign Maintenance personal");
        addMaintenanceAD.setPositiveButton("add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                        .child("users");
                //Query by Email
                Query emailQuery = userRef.orderByChild("email").equalTo(emailInput.getText().toString());
                emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //If user exist
                        if(dataSnapshot.exists()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                //if already a maintenance user, ,ake toast.
                                if (ds.child("user-type").toString().equals("maintenance") || ds.child("user-type").toString().equals("management"))
                                    makeToast("Invalid user!");
                                else {
                                    //if not, open a new maintenance user input in the maintenance-users tree with the structure assigned value.
                                    DatabaseReference maintenanceUserRef = FirebaseDatabase.getInstance().getReference()
                                            .child("structures").child(managementUser.getStructure()).child("maintenance-personal");
                                    Map<String, String> newPost = new HashMap<>();
                                    newPost.put("UID", ds.getKey());
                                    newPost.put("username", ds.child("username").getValue().toString());
                                    maintenanceUserRef.push().setValue(newPost);
                                    userRef.child(ds.getKey()).child("user-type").setValue("maintenance");
                                    userRef.child(ds.getKey()).child("structure-related").setValue(managementUser.getStructure());

                                    makeToast(ds.child("username").getValue().toString() + " has been assigned to " + managementUser.getStructure() + " successfully!");
                                    makeToast("Will refresh at next entry.");
                                }
                            }
                        } else{
                            makeToast("User not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        addMaintenanceAD.setNegativeButton("close", null);
        addMaintenanceAD.show();

    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void createMessageDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter Email");
        input.setText("");
        input.setSingleLine(false);
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(4);
        input.setMaxLines(5);
        input.setGravity(Gravity.START|Gravity.TOP);
        input.setHorizontalScrollBarEnabled(false);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 10;
        params.rightMargin = 10;
        input.setLayoutParams(params);
        container.addView(input);

        final AlertDialog.Builder postMessageAD = new AlertDialog.Builder(this);
        postMessageAD.setView(container);
        postMessageAD.setTitle("Distribute message");
        postMessageAD.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().equals("")){
                    DatabaseReference structureNotificationsRef = FirebaseDatabase.getInstance().getReference()
                            .child("structures").child(managementUser.getStructure()).child("management-notifications");

                    String pushKey = structureNotificationsRef.push().getKey();
                    structureNotificationsRef.child(pushKey).child("message").setValue(input.getText().toString());
                    structureNotificationsRef.child(pushKey).child("date-posted").setValue(new MyCalendar()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                                makeToast("message posted!");
                        }
                    });

                    dialog.dismiss();
                }
            }
        });

        postMessageAD.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        postMessageAD.show();




    }

    //TODO make fab click to open message box DEBUG

}
