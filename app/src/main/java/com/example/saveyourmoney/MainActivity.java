package com.example.saveyourmoney;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Log
    private static final String TAG = "System";

    //Intent Extra Data Keys
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";

    //Request Codes
    private static final int RECORD_EXPENDITURE = 101;
    private static final int SET_NEW_OBJ = 102;

    //Intent Key For Retrieve Data
    private final static String TARGET_EXPENDITURE = "TARGET_EXPENDITURE";
    private final static String TARGET_DATE = "UNTIL_WHEN";

    //Shared Preferences Keys
    private final static String SHARED_PREFS = "SHARED_PREFERENCES";
    private final static String HOW_MUCH = "TARGET_EXPENDITURE";
    private final static String UNTIL_WHEN = "TARGET_DATE";

    //Shared Preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //User Profile
    private TextView curUserName;
    private TextView curUserEmail;
    private ImageView curUserPhoto;

    //Date
    private TextView curDate;

    //Floating Action Buttons
    private FloatingActionButton fbMoreOption;
    private FloatingActionButton fbNewDoc;
    private FloatingActionButton fbNewObj;

    //Animation
    private Animation popUp;
    private Animation popOut;

    //TextViews
    private TextView tvDocUses;
    private TextView tvSetObj;
    private TextView curUses;
    private TextView dueDate;

    //UI
    private Toolbar toolbar;

    //Boolean
    private boolean isOpen;

    //Strings
    private String targetExpenditure;
    private String targetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        curUserName = findViewById(R.id.tv_user_name);
        curUserEmail = findViewById(R.id.tv_user_email);
        curUserPhoto = findViewById(R.id.img_user_profile);
        curDate = findViewById(R.id.tv_cur_date);

        tvDocUses = findViewById(R.id.tv_write_new_doc);
        tvSetObj = findViewById(R.id.tv_set_new_obj);
        curUses = findViewById(R.id.tv_objective);
        dueDate = findViewById(R.id.tv_due_date);

        fbMoreOption = findViewById(R.id.fb_more_option);
        fbNewDoc = findViewById(R.id.fb_write_new_doc);
        fbNewObj = findViewById(R.id.fb_set_new_obj);
        fbMoreOption.setOnClickListener(this);
        fbNewDoc.setOnClickListener(this);
        fbNewObj.setOnClickListener(this);

        popUp = AnimationUtils.loadAnimation(this,R.anim.popup);
        popOut = AnimationUtils.loadAnimation(this,R.anim.popout);

        isOpen = false;

        updateData();

        Intent intent = getIntent();
        updateUserInfo(intent);
        updateCurDate();
    }

    private void updateUserInfo(Intent intent){
        String userName = intent.getStringExtra(USER_NAME);
        String userEmail = intent.getStringExtra(USER_EMAIL);
        Uri userPhotoUri = Uri.parse(intent.getStringExtra(USER_PHOTO));

        curUserName.setText(userName);
        curUserEmail.setText(userEmail);

        Glide.with(this)
                .load(userPhotoUri)
                .centerCrop()
                .into(curUserPhoto);
    }

    private void updateCurDate(){
        Calendar calendar = Calendar.getInstance();
        String curTime = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        curDate.setText(curTime);
    }

    @Override
    public void onClick(View v) {
        int cid = v.getId();
        switch (cid){
            case R.id.fb_more_option:
                if(!isOpen){
                    popUp();
                }else{
                    popOut();
                }

                isOpen = !isOpen;

                break;

            case R.id.fb_write_new_doc:
                Intent recordIntent = new Intent(this,RecordActivity.class);
                startActivityForResult(recordIntent,RECORD_EXPENDITURE);
                break;

            case R.id.fb_set_new_obj:
                Intent setObjIntent = new Intent(this,ObjectiveActivity.class);
                startActivityForResult(setObjIntent, SET_NEW_OBJ);
                break;
        }
    }

    //Override Methods

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SET_NEW_OBJ && resultCode == RESULT_OK){
            targetExpenditure = data.getStringExtra(TARGET_EXPENDITURE);
            targetDate = data.getStringExtra(TARGET_DATE);

            targetExpenditure = String.format("%,d",Integer.parseInt(targetExpenditure));
            Log.d(TAG, "onActivityResult: "+targetExpenditure);

            editor.putString(HOW_MUCH,targetExpenditure);
            editor.putString(UNTIL_WHEN,targetDate);

            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateData();
    }

    //Toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int cid = item.getItemId();

        switch (cid){
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("LOGOUT");
                builder.setMessage("Are you really want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthUI.getInstance()
                                .signOut(MainActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(MainActivity.this, "Logged Out!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Oops! This have not to be happened!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.btn_finish:
                finish();
                break;
        }

        return true;
    }

    //User-Defined Functions
    private void popUp(){
        fbNewDoc.startAnimation(popUp);
        fbNewObj.startAnimation(popUp);
        tvDocUses.startAnimation(popUp);
        tvSetObj.startAnimation(popUp);

        fbNewDoc.setVisibility(View.VISIBLE);
        fbNewObj.setVisibility(View.VISIBLE);
        tvDocUses.setVisibility(View.VISIBLE);
        tvSetObj.setVisibility(View.VISIBLE);
    }

    private void popOut(){
        fbNewDoc.startAnimation(popOut);
        fbNewObj.startAnimation(popOut);
        tvDocUses.startAnimation(popOut);
        tvSetObj.startAnimation(popOut);

        fbNewDoc.setVisibility(View.INVISIBLE);
        fbNewObj.setVisibility(View.INVISIBLE);
        tvDocUses.setVisibility(View.INVISIBLE);
        tvSetObj.setVisibility(View.INVISIBLE);
    }

    private void updateData(){

        targetExpenditure = sharedPreferences.getString(HOW_MUCH,"UNDEFINED");
        targetDate = sharedPreferences.getString(UNTIL_WHEN,"UNDEFINED");

        curUses.setText("0 / "+targetExpenditure);
        dueDate.setText("due " + targetDate);
    }
}
