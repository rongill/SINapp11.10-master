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
import com.rongill.rsg.sinprojecttest.R;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText emailET, confirmEmail, passwordET, confirmPassword;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailET = (EditText)findViewById(R.id.emailSignup);
        confirmEmail = (EditText)findViewById(R.id.confirm_email);
        passwordET = (EditText)findViewById(R.id.passwordSignup);
        confirmPassword = (EditText)findViewById(R.id.confirm_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    public void signUp(View v){
        String []email = new String[2];
        String []password = new String[2];

        if(emailET!=null && passwordET != null && confirmEmail!=null && confirmPassword != null) {
            email[0] = emailET.getText().toString();
            email[1] = confirmEmail.getText().toString();
            password[0] = passwordET.getText().toString();
            password[1] = confirmPassword.getText().toString();
        }
        // check that both fields are filled
        if(validateEmailAndPassword(email, password)){
            mFirebaseAuth.createUserWithEmailAndPassword(email[0], password[0])
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
        if(state){
            startActivity(new Intent(this, CreateUserPrifileActivity.class));
            finish();
        }
        else{
            Toast.makeText(this, "Sign-up failed", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateEmailAndPassword(String[] email, String[] password){
        if(email[0].isEmpty()||email[1].isEmpty()||password[0].isEmpty()||password[1].isEmpty()){
            Toast.makeText(this, "please fill all fields", Toast.LENGTH_LONG).show();
        } else if(email[0].equals(email[1]) && password[0].equals(password[1]))
            return true;
        return false;
    }
}
