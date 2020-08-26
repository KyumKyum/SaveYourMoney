package com.example.saveyourmoney;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Log
    private static final String TAG = "System";

    //User Key Data
    private String userKey;

    //Intent Extra Data Keys
    private static final String USER_NAME = "CURRENT_USER_NAME";
    private static final String USER_EMAIL = "CURRENT_USER_EMAIL";
    private static final String USER_PHOTO = "CURRENT_USER_PHOTO";

    //Request Codes
    private static final int RECORD_EXPENDITURE = 101;
    private static final int SET_OBJ = 102;
    private static final int SET_NEW_OBJ = 103;

    //Retrieve Data
    private static final String USER_KEY = "USER_KEY_VALUE";
    private static final String CUR_SPENT = "CURRENTLY_SPENT";
    private static final String DIALOG_SHOWED = "EXCEED_THE_GOAL";

    //Database Paths
    private static final String PRIORITY_PATH = "RECORD_PRIORITY";
    private static final String OBJ_PATH ="GOAL_INFORMATION";
    private static final String RECORD_PATH = "USER_RECORDS";
    private static final String RECORD_LIST = "RECORDS";
    private static final String TOTAL_EXPENDITURE = "TOTAL_EXPENDITURE";
    private static final String ACHIEVED_PATH="IS_ACHIEVED";
    private static final String CROWN_PATH="CROWN_HERE";

    //Shared Preferences Keys
    private final static String SHARED_PREFS = "SHARED_PREFERENCES";

    //Firebase Firestore
    private FirebaseFirestore root;

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
    private FloatingActionButton fbSetObj;
    private FloatingActionButton fbNewObj;
    private FloatingActionButton fbMoreInformation;

    //Animation
    private Animation popUp;
    private Animation popOut;
    private Animation buttonRotation;
    private Animation buttonReturnRotation;

    //TextViews
    private TextView tvDocUses;
    private TextView tvSetObj;
    private TextView tvNewObj;
    private TextView goal;
    private TextView dueDate;
    private TextView crownNum;

    //UI
    private Toolbar toolbar;

    //Boolean
    private boolean isOpen;
    private boolean isAchieved;

    //RecyclerView & Adpater
    private RecyclerView mRecyclerView;
    private FirestoreRecyclerAdapter<Expenditure,ExpenditureHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userKey = sharedPreferences.getString(USER_KEY, null);

        curUserName = findViewById(R.id.tv_user_name);
        curUserEmail = findViewById(R.id.tv_user_email);
        curUserPhoto = findViewById(R.id.img_user_profile);
        curDate = findViewById(R.id.tv_cur_date);

        tvDocUses = findViewById(R.id.tv_write_new_doc);
        tvSetObj = findViewById(R.id.tv_set_obj);
        tvNewObj = findViewById(R.id.tv_set_new_obj);

        goal = findViewById(R.id.tv_objective);
        dueDate = findViewById(R.id.tv_due_date);
        crownNum = findViewById(R.id.tv_crown_number);

        fbMoreOption = findViewById(R.id.fb_more_option);
        fbNewDoc = findViewById(R.id.fb_write_new_doc);
        fbSetObj = findViewById(R.id.fb_set_obj);
        fbNewObj = findViewById(R.id.fb_set_new_obj);
        fbMoreInformation = findViewById(R.id.fb_more_information);

        fbMoreOption.setOnClickListener(this);
        fbNewDoc.setOnClickListener(this);
        fbSetObj.setOnClickListener(this);
        fbNewObj.setOnClickListener(this);
        fbMoreInformation.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.rv_usages);

        popUp = AnimationUtils.loadAnimation(this, R.anim.popup);
        popOut = AnimationUtils.loadAnimation(this, R.anim.popout);
        buttonRotation = AnimationUtils.loadAnimation(this,R.anim.button_rotation);
        buttonReturnRotation = AnimationUtils.loadAnimation(this,R.anim.button_rotation_return);

        isOpen = false;
        isAchieved = false;


        Intent intent = getIntent();
        updateUserInfo(intent);

        setDatabase();

        DocumentReference documentReference = root.collection(userKey).document(OBJ_PATH);
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            Log.d(TAG, "Before check goal");
                            checkAchieveOrNot();
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        loadInformation();
        updateCurDate();
        updateRecyclerView();

    }

    //Database
    private void setDatabase(){
        root = FirebaseFirestore.getInstance();
    }


    //onClick
    @Override
    public void onClick(View v) {
        int cid = v.getId();
        switch (cid) {
            case R.id.fb_more_option:
                if (!isOpen) {
                    popUp();
                } else {
                    popOut();
                }

                rotation(isOpen);

                isOpen = !isOpen;

                break;

            case R.id.fb_write_new_doc:
                DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);

                docRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    Intent recordIntent = new Intent(MainActivity.this, RecordActivity.class);
                                    recordIntent.putExtra(USER_KEY,userKey);
                                    startActivityForResult(recordIntent, RECORD_EXPENDITURE);

                                }else {
                                    Toast.makeText(MainActivity.this, "목표를 먼저 설정해야해요!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                break;

            case R.id.fb_set_obj:
                Intent setObjIntent = new Intent(this, ObjectiveActivity.class);
                setObjIntent.putExtra(USER_KEY,userKey);
                startActivityForResult(setObjIntent, SET_OBJ);
                break;

            case R.id.fb_set_new_obj:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("어? 새로운 목표를 세우실건가요?");
                builder.setMessage("이전 기록은 다 지워지고 이전 목표에 대한 왕관은 못 얻으실거에용...");
                builder.setPositiveButton("네!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent setObjIntent = new Intent(MainActivity.this, ObjectiveActivity.class);
                        setObjIntent.putExtra(USER_KEY,userKey);
                        startActivityForResult(setObjIntent, SET_NEW_OBJ);
                    }
                });
                builder.setNegativeButton("아니요!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            case R.id.fb_more_information:
                Intent instIntent = new Intent(this, InstructionActivity.class);
                startActivity(instIntent);
        }
    }

    //onActivityResult

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SET_OBJ && resultCode == RESULT_OK){
            resetButton();
            setGoal();
            resetPriority();

            editor.putBoolean(DIALOG_SHOWED,false);
            editor.apply();

            Toast.makeText(this, "목표가 설정되었어요! 잘해보자구요~ :)", Toast.LENGTH_SHORT).show();
        } else if(requestCode == RECORD_EXPENDITURE && resultCode == RESULT_OK){
            resetButton();

            final int returnValue = data.getIntExtra(CUR_SPENT, 0);

            DocumentReference docRef = root.collection(userKey).document(TOTAL_EXPENDITURE);
            docRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            TotalExpenditure curExpenditure = documentSnapshot.toObject(TotalExpenditure.class);
                            int temp = curExpenditure.getTotal();
                            temp += returnValue;
                            curExpenditure.setTotal(temp);
                            DocumentReference docRef = root.collection(userKey).document(TOTAL_EXPENDITURE);
                            docRef.set(curExpenditure)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            String youSpent = String.format("%,d",temp);
                            updateCurObj(youSpent);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            checkIfExceed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else if(requestCode == SET_NEW_OBJ && resultCode == RESULT_OK){
            resetButton();
            setGoal();
            resetPriority();
            resetRecords();

            editor.putBoolean(DIALOG_SHOWED,false);
            editor.apply();
            Toast.makeText(this, "새로운 목표가 설정되었어요! 잘해보자구요~ :)", Toast.LENGTH_SHORT).show();
        }
    }


    //Toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int cid = item.getItemId();

        switch (cid) {
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("로그아웃하기");
                builder.setMessage("로그아웃하실 건가요?");
                builder.setPositiveButton("네!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthUI.getInstance()
                                .signOut(MainActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(MainActivity.this, "ㅠㅠ또 돌아와야해요!", Toast.LENGTH_SHORT).show();
                                        Intent goLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                                        startActivity(goLoginIntent);
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
                builder.setNegativeButton("아니요!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.btn_finish:
                Intent goLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(goLoginIntent);
                finish();
                break;
        }

        return true;
    }

    //Animations
    private void popUp() {


        DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);

        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!documentSnapshot.exists()){
                            tvSetObj.startAnimation(popUp);
                            fbSetObj.startAnimation(popUp);
                            tvSetObj.setVisibility(View.VISIBLE);
                            fbSetObj.setVisibility(View.VISIBLE);
                        } else {
                            tvNewObj.startAnimation(popUp);
                            fbNewObj.startAnimation(popUp);
                            tvNewObj.setVisibility(View.VISIBLE);
                            fbNewObj.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        fbNewDoc.startAnimation(popUp);
                        tvDocUses.startAnimation(popUp);
                        fbNewDoc.setVisibility(View.VISIBLE);
                        tvDocUses.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void popOut() {
        DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);

        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!documentSnapshot.exists()){
                            tvSetObj.startAnimation(popOut);
                            fbSetObj.startAnimation(popOut);
                            tvSetObj.setVisibility(View.INVISIBLE);
                            fbSetObj.setVisibility(View.INVISIBLE);
                        } else {
                            fbNewObj.startAnimation(popOut);
                            tvNewObj.startAnimation(popOut);
                            tvNewObj.setVisibility(View.INVISIBLE);
                            fbNewObj.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        fbNewDoc.startAnimation(popOut);
                        tvDocUses.startAnimation(popOut);

                        fbNewDoc.setVisibility(View.INVISIBLE);
                        tvDocUses.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rotation(boolean isOpen){
        if(!isOpen){
            fbMoreOption.startAnimation(buttonRotation);
        }else{
            fbMoreOption.startAnimation(buttonReturnRotation);
        }
    }

    //Set


    private void setGoal(){
        DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "onSuccess: " + documentSnapshot.getId() + " => " +documentSnapshot.getData());
                        Objective curObj = documentSnapshot.toObject(Objective.class);

                        TotalExpenditure totalExpenditure = new TotalExpenditure(0);

                        DocumentReference docRef = root.collection(userKey).document(TOTAL_EXPENDITURE);

                        docRef.set(totalExpenditure)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        initializeCurObj(curObj.getGoal(),curObj.getDueDate());
                    }

                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        goal.setTextColor(getResources().getColor(R.color.black));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    //Check
    private void checkIfExceed(){
        String curData = goal.getText().toString();
        String[] classified = curData.split(" / ");
        int totalExpenditure = Integer.parseInt(classified[0].replace(",",""));
        int goalExpenditure = Integer.parseInt(classified[1].replace(",",""));

        Log.d(TAG, "totalExpenditure: " + totalExpenditure + " goalExpenditure: " + goalExpenditure);

        if(totalExpenditure > goalExpenditure){
            goal.setTextColor(getResources().getColor(R.color.red));
            editor.putBoolean(DIALOG_SHOWED,true);
            editor.apply();
        } else {
            goal.setTextColor(getResources().getColor(R.color.black));
            editor.putBoolean(DIALOG_SHOWED,false);
            editor.apply();
        }

    }

    private void checkAchieveOrNot(){
        Log.d(TAG, "checkGoal Start: ");

        final Calendar curDate = Calendar.getInstance();
        curDate.getTime();

        final Calendar resetDate = Calendar.getInstance();

        DocumentReference objRef = root.collection(userKey).document(OBJ_PATH);
        objRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Log.d(TAG, "checkGoal onSuccess: ");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

                        Objective setObj = documentSnapshot.toObject(Objective.class);
                        String extracted = setObj.getResetDate();
                        try {
                            Date date = sdf.parse(extracted);
                            resetDate.setTime(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "checkGoal onComplete ");
                Log.d(TAG, "Check Difference: " + curDate.compareTo(resetDate));
                if(curDate.compareTo(resetDate)>=0){
                    DocumentReference achievedRef = root.collection(userKey).document(ACHIEVED_PATH);

                    achievedRef.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        Achieved newAchieved = documentSnapshot.toObject(Achieved.class);
                                        isAchieved = newAchieved.getAchieved();
                                    } else {
                                        Toast.makeText(MainActivity.this, "NULL OBJ", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "NULL OBJECT");
                                    }
                                }
                            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(isAchieved){

                                DocumentReference crownRef = root.collection(userKey).document(CROWN_PATH);
                                crownRef.get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if(!documentSnapshot.exists()){ //Newly assigning
                                                    Crown crown = new Crown(1);
                                                    DocumentReference docRef = root.collection(userKey).document(CROWN_PATH);
                                                    docRef.set(crown)
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }else{
                                                    Crown retrievedData = documentSnapshot.toObject(Crown.class);
                                                    int curCrown = retrievedData.getCrownNum() + 1;
                                                    retrievedData.setCrownNum(curCrown);

                                                    DocumentReference docRef = root.collection(userKey).document(CROWN_PATH);
                                                    docRef.set(curCrown)
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                loadCrownNum();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("우왕! 축하해요!!! >_<");
                                builder.setMessage("목표를 지켰으니 선물로 왕관을 드리겠습니다! :)");

                                builder.setPositiveButton("고마워요!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else { //Spend more money than the goal
                                goal.setTextColor(getResources().getColor(R.color.black));

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("헐...목표를 못 지켰네요...ㅠㅠ");
                                builder.setMessage("우리 담엔 잘해봐요ㅠㅠ");
                                builder.setPositiveButton("넹...", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                            resetAll();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Reset

    private void resetButton(){
        isOpen = false;
        fbNewObj.setVisibility(View.INVISIBLE);
        fbSetObj.setVisibility(View.INVISIBLE);
        fbNewDoc.setVisibility(View.INVISIBLE);

        tvNewObj.setVisibility(View.INVISIBLE);
        tvSetObj.setVisibility(View.INVISIBLE);
        tvDocUses.setVisibility(View.INVISIBLE);

        fbMoreOption.startAnimation(buttonReturnRotation);
    }

    private void resetPriority(){
        DocumentReference docRef = root.collection(userKey).document(PRIORITY_PATH);
        Priority priority = new Priority(0);
        docRef.set(priority)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    private void resetRecords(){
        CollectionReference colRef = root.collection(userKey)
                .document(RECORD_PATH).collection(RECORD_LIST);
        colRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();

                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                        for(DocumentSnapshot snapshot: snapshots){
                            batch.delete(snapshot.getReference());
                        }

                        batch.commit()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetAll(){
        Log.d(TAG, "resetAll ");
        resetRecords();
        deleteGoal();
        deletePriority();
        deleteTotalExpenditure();
        deleteIsAchieved();

        loadInformation();
    }

    //Delete
    private void deleteGoal(){
        Log.d(TAG, "resetOthers");
        DocumentReference objRef = root.collection(userKey).document(OBJ_PATH);
        objRef.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePriority(){
        DocumentReference priorityRef = root.collection(userKey).document(PRIORITY_PATH);

        priorityRef.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteTotalExpenditure(){
        DocumentReference totalRef = root.collection(userKey).document(TOTAL_EXPENDITURE);

        totalRef.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteIsAchieved(){
        DocumentReference achievedRef = root.collection(userKey).document(ACHIEVED_PATH);
        achievedRef.delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Load
    private void loadInformation(){
        DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!documentSnapshot.exists()){
                            initializeCurObj("UNDEFINED","UNDEFINED");
                            updateCurObj("UNDEFINED");
                        } else {
                            Objective curObjective = documentSnapshot.toObject(Objective.class);
                            String curObj = curObjective.getGoal();
                            String curDueDate = curObjective.getDueDate();
                            initializeCurObj(curObj,curDueDate);

                            DocumentReference totalRef = root.collection(userKey).document(TOTAL_EXPENDITURE);
                            totalRef.get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists()){
                                                TotalExpenditure curExpenditure = documentSnapshot.toObject(TotalExpenditure.class);
                                                String youSpent = String.format("%,d",curExpenditure.getTotal());
                                                updateCurObj(youSpent);
                                            }
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            checkIfExceed();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        loadCrownNum();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCrownNum(){
        DocumentReference docRef = root.collection(userKey).document(CROWN_PATH);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){ // Crown exists
                            Crown crown = documentSnapshot.toObject(Crown.class);
                            crownNum.setText(" * " + crown.getCrownNum());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Update
    private void updateUserInfo(Intent intent) {
        userKey = intent.getStringExtra(USER_KEY);
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

    private void updateCurDate() {
        Calendar calendar = Calendar.getInstance();
        String curTime = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        curDate.setText(curTime);
    }

    private void initializeCurObj(String goalExpenditure, String due){
        goal.setText("0 / " + goalExpenditure);
        dueDate.setText("due " + due);
    }

    private void updateCurObj(String yourExpenditure){
        String curState = goal.getText().toString();
        String[] classified = curState.split(" / ");
        classified[0] = yourExpenditure;
        goal.setText(classified[0] + " / " + classified[1]);
    }

    private void updateRecyclerView(){
        CollectionReference recordReferences = root.collection(userKey)
                .document(RECORD_PATH).collection(RECORD_LIST);

        Query query = recordReferences.orderBy("priority", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Expenditure> options = new FirestoreRecyclerOptions.Builder<Expenditure>()
                .setQuery(query,Expenditure.class)
                .build();

        mAdapter = new FirestoreRecyclerAdapter<Expenditure, ExpenditureHolder>(options) {
            @NonNull
            @Override
            public ExpenditureHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
                return new ExpenditureHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExpenditureHolder holder, int position, @NonNull Expenditure model) {
                holder.setRecordDate(model.getDate());
                holder.setRecordReason(model.getWhatFor());
                holder.setRecordExpenditure(String.format("%,d",model.getHowMuch()));
            }
        };

        mAdapter.notifyDataSetChanged();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }


    //Holder Class
    private class ExpenditureHolder extends RecyclerView.ViewHolder{

        private TextView recordDate;
        private TextView recordReason;
        private TextView recordExpenditure;

        public ExpenditureHolder(@NonNull View itemView) {
            super(itemView);

            recordDate = itemView.findViewById(R.id.tv_used_date);
            recordReason = itemView.findViewById(R.id.tv_used_for);
            recordExpenditure = itemView.findViewById(R.id.tv_used_amount);
        }

        public void setRecordDate(String curDate){
            recordDate.setText(curDate);
        }

        public void setRecordReason(String curReason){
            recordReason.setText(curReason);
        }

        public void setRecordExpenditure(String curExpenditure){
            recordExpenditure.setText(curExpenditure);
        }
    }

    //Activity Life Cycle
    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAdapter!=null){
            mAdapter.stopListening();
        }
    }

}

