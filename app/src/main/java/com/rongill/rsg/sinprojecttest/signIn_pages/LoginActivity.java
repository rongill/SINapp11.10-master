package com.rongill.rsg.sinprojecttest.signIn_pages;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rongill.rsg.sinprojecttest.MainDrowerActivity;
import com.rongill.rsg.sinprojecttest.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";
    private FirebaseAuth mFirebaseAuth;
    private EditText emailET;
    private EditText passwordET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Edit text init-
        emailET = (EditText)findViewById(R.id.emailLogin);
        passwordET = (EditText)findViewById(R.id.passwordLogin);
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOrSignup(emailET.getText().toString(), passwordET.getText().toString());
            }
        });
        Button resetPasswordBtn = (Button)findViewById(R.id.forgotPass_btn);
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordRequest();
            }
        });

        // Firebase shared instance init-
        mFirebaseAuth = FirebaseAuth.getInstance();

    }




    public void loginOrSignup(String email, String password){

        if( !email.isEmpty() ){
            if (password.equals(""))
                Toast.makeText(this, "please enter a password", Toast.LENGTH_LONG).show();
            else{
                mFirebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    Log.d(TAG, "signInWithEmail:success");
                                    updateUI(true);

                                }else{
                                    Log.w(TAG,"signInWithEmail:failed");
                                    updateUI(false);
                                }

                            }
                        });
            }

        }else{
            startActivity(new Intent(this,SignupActivity.class));
            finish();

        }

    }

    private void updateUI(boolean state){
        if (state){
            startActivity(new Intent(this, MainDrowerActivity.class));
            finish();

        }else{
            Toast.makeText(this, "Authentiacation failed",Toast.LENGTH_LONG).show();
            emailET.setText("");
            passwordET.setText("");
        }

    }

    public void forgotPasswordRequest(){

        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setMessage("Enter your email address");
        final EditText input = new EditText(this);
        passwordResetDialog.setView(input);
        input.setText("");

        passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().isEmpty()){
                    //TODO send password reset email from Firebase SDK
                    Toast.makeText(LoginActivity.this, "An email has been send to " + input.getText().toString(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        passwordResetDialog.setNegativeButton("Close", null);
        passwordResetDialog.show();
    }
}
