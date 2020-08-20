package com.example.saveyourmoney;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private static final String TOTAL_EXPENDITURE = "TOTAL_EXPENDITURE";
    private static final String OBJ_PATH = "GOAL_INFORMATION";
    private static final String ACHIEVED_PATH="IS_ACHIEVED";


    //SharedPreferences
    private SharedPreferences sharedPreferences;

    private final static String SHARED_PREFS = "SHARED_PREFERENCES";
    private static final String DIALOG_SHOWED = "EXCEED_THE_GOAL";


    private TextView curDate;
    private TextView curTime;
    private TextView curObj;

    private EditText totalSpent;
    private EditText spendReason;

    private FloatingActionButton fabUpload;

    private ProgressBar mProgressBar;

    private FrameLayout mFrameLayout;

    //Firebase Firestore
    private FirebaseFirestore root = FirebaseFirestore.getInstance();
    private DocumentReference priorityRef;
    private CollectionReference recordRef;

    private String userKey;
    private String curGoal;
    private String curExpenditure;

    private Boolean isClickable;

    private Toast mToast;

    private int priorityValue;
    private int base;

    private boolean isExceed;
    private boolean showedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        userKey = intent.getStringExtra(USER_KEY);

        mFrameLayout = findViewById(R.id.bg_foreground);
        mFrameLayout.getForeground().setAlpha(0);

        isClickable = true;

        totalSpent = findViewById(R.id.et_how_much);
        spendReason = findViewById(R.id.et_what_for);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        curDate = findViewById(R.id.tv_cur_date);
        curTime = findViewById(R.id.tv_cur_time);
        curObj = findViewById(R.id.tv_goal_in_record);

        isExceed = false;
        showedDialog = false;

        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        fabUpload = findViewById(R.id.fb_upload);

        loadData();

        totalSpent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String curSpend = totalSpent.getText().toString();
                int toAdd = 0;

                if (curSpend.trim().length() > 0) {
                    toAdd = Integer.parseInt(curSpend.replace(",", ""));
                }

                curSpend = String.format("%,d", base + toAdd);
                checkIfExceed(curSpend, curGoal);
                curObj.setText(curSpend + " / " + curGoal);
            }
        });

        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String moneyYouSpend = totalSpent.getText().toString().trim();
                final String reasonYouSpend = spendReason.getText().toString().trim();

                if (moneyYouSpend.length() > 0 && reasonYouSpend.length() > 0) {
                    if (!isExceed || showedDialog) {
                        if (isClickable) {
                            upload(moneyYouSpend, reasonYouSpend);
                        } else {
                            if (mToast != null) mToast.cancel();
                            mToast = Toast.makeText(RecordActivity.this, "Currently Uploading!", Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
                        builder.setTitle("You're spending too much money!");
                        builder.setMessage("You'll lose your crown.");
                        builder.setPositiveButton("I Understand.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                upload(moneyYouSpend,reasonYouSpend);


                                DocumentReference achievedRef = root.collection(userKey).document(ACHIEVED_PATH);
                                achievedRef.set(new Achieved(false))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                                editor.putBoolean(DIALOG_SHOWED,true);
                                                editor.apply();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                } else {
                    Toast.makeText(RecordActivity.this, "You haven't filled necessary parts!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setCurDateAndTime();
    }

    private void loadData() {
        showedDialog = sharedPreferences.getBoolean(DIALOG_SHOWED,false);

        DocumentReference goalRef = root.collection(userKey).document(OBJ_PATH);

        goalRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Objective curObjective = documentSnapshot.toObject(Objective.class);
                        curGoal = curObjective.getGoal();
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                loadTotal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTotal() {
        DocumentReference totalRef = root.collection(userKey).document(TOTAL_EXPENDITURE);

        totalRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        TotalExpenditure curTotal = documentSnapshot.toObject(TotalExpenditure.class);
                        base = curTotal.getTotal();
                        curExpenditure = String.format("%,d", curTotal.getTotal());
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                updateInfo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo() {
        String curState = curExpenditure + " / " + curGoal;
        checkIfExceed(curExpenditure, curGoal);
        curObj.setText(curState);
    }

    private void checkIfExceed(String spend, String goal) {
        int youSpend = Integer.parseInt(spend.replace(",", ""));
        int yourGoal = Integer.parseInt(goal.replace(",", ""));
        if (youSpend > yourGoal) {
            curObj.setTextColor(getResources().getColor(R.color.red));
            isExceed = true;
        } else {
            curObj.setTextColor(getResources().getColor(R.color.black));
            isExceed = false;
        }
    }

    private void setCurDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        String curDateInString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        curDate.setText(curDateInString);

        String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        curTime.setText(new StringBuilder().append(hour).append(" : ").append(minute).toString());
    }

    private void upload(final String moneyYouSpend, final String reasonYouSpend){
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

                        Expenditure expenditure = new Expenditure(curDate.getText().toString().trim(), reasonYouSpend,
                                Integer.parseInt(moneyYouSpend), priorityValue);

                        recordRef = root.collection(userKey).document(RECORD_PATH).collection(RECORD_LIST);

                        recordRef.add(expenditure)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(RecordActivity.this, "Recorded!", Toast.LENGTH_SHORT).show();

                                        Log.d(TAG, "onSuccess (final): " + priorityValue);

                                        Priority newPriority = new Priority(priorityValue + 1);
                                        priorityRef.set(newPriority)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        Intent intent = new Intent();
                                        intent.putExtra(CUR_SPENT, Integer.parseInt(moneyYouSpend));
                                        setResult(RESULT_OK, intent);
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
    }

}
