package com.rongill.rsg.sinprojecttest.signIn_pages;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText emailET, userNameET, passwordET, confirmPassword;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailET = (EditText)findViewById(R.id.emailSignup);
        userNameET = (EditText)findViewById(R.id.username);
        passwordET = (EditText)findViewById(R.id.passwordSignup);
        confirmPassword = (EditText)findViewById(R.id.confirm_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    public void signUp(View v){
        String []password = new String[2];

        if(passwordET != null && confirmPassword != null) {
            password[0] = passwordET.getText().toString();
            password[1] = confirmPassword.getText().toString();
        }
        // check that both fields are filled
        if(validateEmailAndPassword(emailET.getText().toString(), userNameET.getText().toString(), password)){
            mFirebaseAuth.createUserWithEmailAndPassword(emailET.getText().toString(), password[0])
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "createUserWithEmail:success");
                                updateUI(true);
                            }
                            else{
                                Log.d(TAG, "createUserWithEmail:failed");
                                updateUI(false);
                            }
                        }
                    });

        }else{
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show();
        }
    }

    public void updateUI(Boolean state){
        if(state && mFirebaseAuth != null){
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getUid());
            Map<String,String> newPost = new HashMap<>();
            newPost.put("username", userNameET.getText().toString());
            newPost.put("email",emailET.getText().toString());
            newPost.put("status","connected");
            mRef.setValue(newPost);

            startActivity(new Intent(this, CreateUserPrifileActivity.class));
            finish();
        }
        else{
            Toast.makeText(this, "Sign-up failed", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateEmailAndPassword(String email, String username, String[] password){
        if(email.isEmpty()||username.isEmpty()||password[0].isEmpty()||password[1].isEmpty()){
            Toast.makeText(this, "please fill all fields", Toast.LENGTH_LONG).show();
        } else if(password[0].equals(password[1]))
            return true;
        return false;
    }


}
