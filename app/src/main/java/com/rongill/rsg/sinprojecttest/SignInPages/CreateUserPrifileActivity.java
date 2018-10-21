package com.rongill.rsg.sinprojecttest.SignInPages;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.MainDrowerActivity;
import com.rongill.rsg.sinprojecttest.R;

import java.util.HashMap;
import java.util.Map;

public class CreateUserPrifileActivity extends AppCompatActivity {

    private static final String TAG = "CreateUserProfileActivity";

    private EditText nameET, ageET, telephoneET;
    private Spinner genderSpinner;
    private String gender [] = {"male", "female"};
    private ArrayAdapter<String> spinnerAdapter;
    private CheckBox [] cbArray = new CheckBox[4];

    private FirebaseAuth mFirebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_prifile);

        //init editText fields
        nameET = (EditText)findViewById(R.id.profile_nameET);
        ageET = (EditText)findViewById(R.id.profile_AgeET);
        telephoneET = (EditText)findViewById(R.id.profile_telephoneET);

        //init Spinner nad adapter spinner
        genderSpinner = (Spinner)findViewById(R.id.profile_gender_spinner);
        spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,gender);
        genderSpinner.setAdapter(spinnerAdapter);

        //init CheckBoxes
        cbArray[0] = (CheckBox)findViewById(R.id.sportCB);
        cbArray[1] = (CheckBox)findViewById(R.id.clothingCB);
        cbArray[2] = (CheckBox)findViewById(R.id.gadgetsCB);
        cbArray[2] = (CheckBox)findViewById(R.id.foodCB);

        //link to current user instance
        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    public void submitToServer(View v){
        String userId;
        if(mFirebaseAuth!=null) {
            userId = mFirebaseAuth.getCurrentUser().getUid();
            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId);

            Map newPost = new HashMap();
            newPost.put("username", nameET.getText().toString());
            newPost.put("age", ageET.getText().toString());
            newPost.put("telephone", telephoneET.getText().toString());
            newPost.put("gender", genderSpinner.getSelectedItem().toString());

            // String preferences = "";

            //  for(int i=0;i<4;i++){
            //    if(cbArray[i].isSelected())
            //         preferences+=cbArray[i].getText().toString() + " ";
            //   }

            //   newPost.put("preferences", preferences);
            currentUserDb.setValue(newPost).addOnCompleteListener(CreateUserPrifileActivity.this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "User profile save: success.");
                                updateUI(true);
                            } else{
                                Log.d(TAG, "User profile save: failed.");
                                updateUI(false);
                            }

                        }
                    });
        }


    }

    private void updateUI(Boolean state){
        if(state){
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainDrowerActivity.class));
            finish();
        } else {
            Toast.makeText(this, "something whent wrong..", Toast.LENGTH_LONG).show();
        }
    }


}
