package com.example.saveyourmoney;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final String TAG = "System";
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";
    private GoogleSignInClient mGoogleSignInClient;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        createRequest();

        Button signInButton = findViewById(R.id.btn_google_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void createRequest(){

        //Get Google Accounts
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN && resultCode == RESULT_OK){
            //Result Returned from intent
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                //Successful Sign in : Authenticate with Firebase.
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e){
                //Sign in Failed
                Log.d(TAG, e.getMessage());
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        //Check whether the user have already signed in.
        super.onStart();

        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser!=null){
            updateUI(mUser);
        }
    }

    //Firebase Authentication With Google
    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            updateUI(mUser);
                        }else{
                            Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Login Failed on Complete Listener");
                        }
                    }
                });
    }


    //Login
    private void updateUI(FirebaseUser user) {
        String curUserName = user.getDisplayName();
        String curUserEmail = user.getEmail();
        Uri curUserPhotoUrl = user.getPhotoUrl();

        Intent goMainActivityIntent = new Intent(this,MainActivity.class);
        //goMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        goMainActivityIntent.putExtra(USER_NAME,curUserName);
        goMainActivityIntent.putExtra(USER_EMAIL,curUserEmail);
        goMainActivityIntent.putExtra(USER_PHOTO,curUserPhotoUrl.toString());

        startActivity(goMainActivityIntent);
    }
}