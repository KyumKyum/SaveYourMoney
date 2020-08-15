package com.example.saveyourmoney;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;

public class RecordActivity extends AppCompatActivity {

    private final static String TAG = "system";

    private final static String SHARED_PREFS = "SHARED_PREFERENCES";
    private static final String USER_KEY = "USER_KEY_VALUE";
    private final static String PRIORITY = "EXPENDITURE_PRIORITY";

    private TextView curDate;
    private TextView curTime;

    private EditText totalSpended;
    private EditText spendReason;

    private FloatingActionButton fabUpload;

    private ProgressBar mProgressBar;

    private FrameLayout mFrameLayout;

    //Firebase Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String userKey;

    private Boolean isClickable;

    private Toast mToast;

    private int priority;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mFrameLayout = findViewById(R.id.bg_foreground);
        mFrameLayout.getForeground().setAlpha(0);

        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isClickable= true;

        totalSpended = findViewById(R.id.et_how_much);
        spendReason = findViewById(R.id.et_what_for);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        curDate = findViewById(R.id.tv_cur_date);
        curTime = findViewById(R.id.tv_cur_time);
        fabUpload = findViewById(R.id.fb_upload);
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String moneyYouSpend = totalSpended.getText().toString().trim();
                String reasonYouSpend = spendReason.getText().toString().trim();

                if( moneyYouSpend.length() > 0 && reasonYouSpend.length() > 0){
                    if(isClickable){

                        mFrameLayout.getForeground().setAlpha(100);
                        mProgressBar.setVisibility(View.VISIBLE);

                        isClickable = false;
                        userKey = sharedPreferences.getString(USER_KEY, null);
                        collectionReference = db.collection(userKey);

                        priority = sharedPreferences.getInt(PRIORITY,0);

                        Expenditure expenditure = new Expenditure(curDate.getText().toString().trim(),reasonYouSpend,Integer.parseInt(moneyYouSpend), ++priority);

                        collectionReference.add(expenditure)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(RecordActivity.this, "Recorded!", Toast.LENGTH_SHORT).show();

                                        editor.putInt(PRIORITY,priority);
                                        editor.apply();

                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RecordActivity.this, "Oops! This shouldn't be happened!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                        finish();
                                    }
                                });
                    } else{

                        if(mToast != null) mToast.cancel();
                        mToast = Toast.makeText(RecordActivity.this,"Currently Uploading!",Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                } else{
                    Toast.makeText(RecordActivity.this, "You haven't filled necessary parts!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setCurDateAndTime();
    }

    private void setCurDateAndTime(){
        Calendar calendar = Calendar.getInstance();
        String curDateInString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        curDate.setText(curDateInString);

        String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        curTime.setText(new StringBuilder().append(hour).append(" : ").append(minute).toString());
    }

}
