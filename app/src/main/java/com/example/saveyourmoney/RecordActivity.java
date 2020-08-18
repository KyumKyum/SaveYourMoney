package com.example.saveyourmoney;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;

public class RecordActivity extends AppCompatActivity {

    private final static String TAG = "system";


    private static final String USER_KEY = "USER_KEY_VALUE";
    private static final String PRIORITY_PATH = "RECORD_PRIORITY";
    private static final String RECORD_PATH = "USER_RECORDS";
    private static final String RECORD_LIST = "RECORDS";
    private static final String CUR_SPENT = "CURRENTLY_SPENT";

    private TextView curDate;
    private TextView curTime;

    private EditText totalSpended;
    private EditText spendReason;

    private FloatingActionButton fabUpload;

    private ProgressBar mProgressBar;

    private FrameLayout mFrameLayout;

    //Firebase Firestore
    private FirebaseFirestore root = FirebaseFirestore.getInstance();
    private DocumentReference priorityRef;
    private CollectionReference recordRef;

    private String userKey;

    private Boolean isClickable;

    private Toast mToast;

    private int priorityValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        userKey = intent.getStringExtra(USER_KEY);

        mFrameLayout = findViewById(R.id.bg_foreground);
        mFrameLayout.getForeground().setAlpha(0);

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
                final String moneyYouSpend = totalSpended.getText().toString().trim();
                final String reasonYouSpend = spendReason.getText().toString().trim();

                if( moneyYouSpend.length() > 0 && reasonYouSpend.length() > 0){
                    if(isClickable){

                        mFrameLayout.getForeground().setAlpha(100);
                        mProgressBar.setVisibility(View.VISIBLE);

                        isClickable = false;

                        //Connecting to DB
                        priorityRef = root.collection(userKey).document(PRIORITY_PATH);
                        priorityRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Priority curPriority = documentSnapshot.toObject(Priority.class);
                                        priorityValue = curPriority.getPriority();
                                        Log.d(TAG, "onSuccess (1): " + priorityValue);

                                        Log.d(TAG, "onSuccess(2): " + priorityValue);

                                        Expenditure expenditure = new Expenditure(curDate.getText().toString().trim(),reasonYouSpend,
                                                Integer.parseInt(moneyYouSpend), priorityValue);

                                        recordRef = root.collection(userKey).document(RECORD_PATH).collection(RECORD_LIST);

                                        recordRef.add(expenditure)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Toast.makeText(RecordActivity.this, "Recorded!", Toast.LENGTH_SHORT).show();

                                                        Log.d(TAG, "onSuccess (final): " + priorityValue);

                                                        Priority newPriority = new Priority(priorityValue+1);
                                                        priorityRef.set(newPriority)
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                        Intent intent = new Intent();
                                                        intent.putExtra(CUR_SPENT,Integer.parseInt(moneyYouSpend));
                                                        setResult(RESULT_OK,intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, e.getMessage());
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
