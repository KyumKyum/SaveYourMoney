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
import java.util.Calendar;
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


    //Database Paths
    private static final String PRIORITY_PATH = "RECORD_PRIORITY";
    private static final String OBJ_PATH ="GOAL_INFORMATION";
    private static final String RECORD_PATH = "USER_RECORDS";
    private static final String RECORD_LIST = "RECORDS";
    private static final String TOTAL_EXPENDITURE = "TOTAL_EXPENDITURE";


    //Shared Preferences Keys
    private final static String SHARED_PREFS = "SHARED_PREFERENCES";

    //Firebase Firestore
    private FirebaseFirestore root;

    //Shared Preferences
    private SharedPreferences sharedPreferences;

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

    //Animation
    private Animation popUp;
    private Animation popOut;

    //TextViews
    private TextView tvDocUses;
    private TextView tvSetObj;
    private TextView tvNewObj;
    private TextView goal;
    private TextView dueDate;

    //UI
    private Toolbar toolbar;

    //Boolean
    private boolean isOpen;

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

        fbMoreOption = findViewById(R.id.fb_more_option);
        fbNewDoc = findViewById(R.id.fb_write_new_doc);
        fbSetObj = findViewById(R.id.fb_set_obj);
        fbNewObj = findViewById(R.id.fb_set_new_obj);
        fbMoreOption.setOnClickListener(this);
        fbNewDoc.setOnClickListener(this);
        fbSetObj.setOnClickListener(this);
        fbNewObj.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.rv_usages);

        popUp = AnimationUtils.loadAnimation(this, R.anim.popup);
        popOut = AnimationUtils.loadAnimation(this, R.anim.popout);

        isOpen = false;


        Intent intent = getIntent();
        updateUserInfo(intent);

        setDatabase();
        loadInformation();
        updateCurDate();
        updateRecyclerView();

    }

    //Database
    private void setDatabase(){
        root = FirebaseFirestore.getInstance();
    }


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
                                    Toast.makeText(MainActivity.this, "You have to set your goal first!", Toast.LENGTH_SHORT).show();
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
                builder.setTitle("Set new goal?");
                builder.setMessage("Your progress will be erased!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent setObjIntent = new Intent(MainActivity.this, ObjectiveActivity.class);
                        setObjIntent.putExtra(USER_KEY,userKey);
                        startActivityForResult(setObjIntent, SET_NEW_OBJ);
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
            Toast.makeText(this, "Goal Set!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "New Goal Set!", Toast.LENGTH_SHORT).show();
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
    private void popUp() {
        fbNewDoc.startAnimation(popUp);
        tvDocUses.startAnimation(popUp);
        fbNewDoc.setVisibility(View.VISIBLE);
        tvDocUses.setVisibility(View.VISIBLE);

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
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fbNewDoc.startAnimation(popOut);
        tvDocUses.startAnimation(popOut);

        fbNewDoc.setVisibility(View.INVISIBLE);
        tvDocUses.setVisibility(View.INVISIBLE);


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
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    private void resetButton(){
        isOpen = false;
        fbNewObj.setVisibility(View.INVISIBLE);
        fbSetObj.setVisibility(View.INVISIBLE);
        fbNewDoc.setVisibility(View.INVISIBLE);

        tvNewObj.setVisibility(View.INVISIBLE);
        tvSetObj.setVisibility(View.INVISIBLE);
        tvDocUses.setVisibility(View.INVISIBLE);

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
                                            TotalExpenditure curExpenditure = documentSnapshot.toObject(TotalExpenditure.class);
                                            String youSpent = String.format("%,d",curExpenditure.getTotal());
                                            updateCurObj(youSpent);
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
                }).addOnFailureListener(new OnFailureListener() {
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

