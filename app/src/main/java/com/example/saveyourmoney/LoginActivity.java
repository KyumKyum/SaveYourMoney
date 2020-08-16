package com.example.saveyourmoney;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final String TAG = "System";
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";

    private final static String SHARED_PREFS = "SHARED_PREFERENCES";
    private static final String USER_KEY = "USER_KEY_VALUE";

    private FirebaseAuth mAuth;

    private Button loginButton;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.btn_google_sign_in);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    //Create Sign In Intent

    private void signIn(){
       if(FirebaseAuth.getInstance().getCurrentUser() != null){ //Auto-Login
           navigateToMainActivity(FirebaseAuth.getInstance().getCurrentUser());
       } else {
           List<AuthUI.IdpConfig> providers = Collections.singletonList(
                   new AuthUI.IdpConfig.GoogleBuilder().build()
           );

           startActivityForResult(
                   AuthUI.getInstance()
                           .createSignInIntentBuilder()
                           .setAvailableProviders(providers)
                           .build(),
                   RC_SIGN_IN);
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK){ //Successfully Signed In
                FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
                navigateToMainActivity(curUser);
            } else{
                Toast.makeText(this, "Oops! There might be an error!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "ERROR WHILE LOGIN");
                Log.d(TAG, "onActivityResult: " + response.getError().getErrorCode());
            }
        }
    }

    //Login
    private void navigateToMainActivity(FirebaseUser user) {
        String curUserName = user.getDisplayName();
        String curUserEmail = user.getEmail();
        Uri curUserPhotoUrl = user.getPhotoUrl();

        String userKey = curUserEmail.trim()+"USER_KEY";
        Log.d(TAG, "navigateToMainActivity: " + userKey);

        Intent goMainActivityIntent = new Intent(this,MainActivity.class);
        goMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        goMainActivityIntent.putExtra(USER_KEY, userKey);
        goMainActivityIntent.putExtra(USER_NAME,curUserName);
        goMainActivityIntent.putExtra(USER_EMAIL,curUserEmail);
        goMainActivityIntent.putExtra(USER_PHOTO,curUserPhotoUrl.toString());

        startActivity(goMainActivityIntent);
    }



}