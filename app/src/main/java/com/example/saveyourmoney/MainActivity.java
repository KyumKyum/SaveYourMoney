package com.example.saveyourmoney;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "System";
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";

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

    //Boolean
    private boolean isOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curUserName = findViewById(R.id.tv_user_name);
        curUserEmail = findViewById(R.id.tv_user_email);
        curUserPhoto = findViewById(R.id.img_user_profile);
        curDate = findViewById(R.id.tv_cur_date);

        tvDocUses = findViewById(R.id.tv_write_new_doc);
        tvSetObj = findViewById(R.id.tv_set_new_obj);

        fbMoreOption = findViewById(R.id.fb_more_option);
        fbNewDoc = findViewById(R.id.fb_write_new_doc);
        fbNewObj = findViewById(R.id.fb_set_new_obj);
        fbMoreOption.setOnClickListener(this);

        popUp = AnimationUtils.loadAnimation(this,R.anim.popup);
        popOut = AnimationUtils.loadAnimation(this,R.anim.popout);

        isOpen = false;

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
        }
    }

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

}
