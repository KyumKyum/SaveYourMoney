package com.example.saveyourmoney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "System";
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";

    //User Profile
    TextView curUserName;
    TextView curUserEmail;
    ImageView curUserPhoto;

    //Date
    TextView curDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curUserName = findViewById(R.id.tv_user_name);
        curUserEmail = findViewById(R.id.tv_user_email);
        curUserPhoto = findViewById(R.id.img_user_profile);
        curDate = findViewById(R.id.tv_cur_date);

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
}
