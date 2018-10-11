package com.rongill.rsg.sinprojecttest.SignInPages;

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
import com.rongill.rsg.sinprojecttest.MainActivity;
import com.rongill.rsg.sinprojecttest.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

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
        findViewById(R.id.btnLogin).setOnClickListener(this);

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
            startActivity(new Intent(this, MainActivity.class));

        }else{
            Toast.makeText(this, "Authentiacation failed",Toast.LENGTH_LONG).show();
            emailET.setText("");
            passwordET.setText("");
        }

    }

    public void forgotPasswordRequest(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if(i == R.id.btnLogin){
            loginOrSignup(emailET.getText().toString(), passwordET.getText().toString());

        } else {
            forgotPasswordRequest();
        }
    }
}
