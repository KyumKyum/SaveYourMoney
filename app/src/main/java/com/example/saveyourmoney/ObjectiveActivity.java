package com.example.saveyourmoney;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;

public class ObjectiveActivity extends AppCompatActivity {

    private final static String TARGET_EXPENDITURE = "TARGET_EXPENDITURE";
    private final static String TARGET_DATE = "UNTIL_WHEN";
    private static final String USER_KEY = "USER_KEY_VALUE";

    private static final String OBJ_PATH ="GOAL_INFORMATION";
    private static final String ACHIEVED_PATH="IS_ACHIEVED";

    TextView curDate;

    EditText objExpenditure;
    EditText objDate;

    Button setObj;
    
    String untilWhen;
    String resetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objective);

        curDate = findViewById(R.id.tv_cur_date);

        setObj = findViewById(R.id.btn_set_obj);
        objExpenditure = findViewById(R.id.et_new_obj);
        objDate = findViewById(R.id.et_obj_day);

        setObj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(objExpenditure.getText().toString().trim().length() > 0 && untilWhen != null){

                    final String goalExpenditure;
                    goalExpenditure = String.format("%,d",Integer.parseInt(objExpenditure.getText().toString().trim()));

                    AlertDialog.Builder builder = new AlertDialog.Builder(ObjectiveActivity.this);
                    builder.setTitle("Confirm Your Goal?");
                    builder.setMessage("You have to use only " + goalExpenditure+"(in KRW) until " + untilWhen);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Objective objective = new Objective(goalExpenditure,untilWhen,resetDate);
                            Achieved achieved = new Achieved(true);
                            setDatabase(objective,achieved);

                            Intent objIntent = new Intent();
                            setResult(RESULT_OK, objIntent);
                            finish();
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
                } else {
                    Toast.makeText(ObjectiveActivity.this, "You haven't filled necessary parts!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        objDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Calendar calendar = Calendar.getInstance();
                int amountDate = 0;
                Toast mToast = null;

                if(!objDate.getText().toString().trim().equals("")){
                    if(objDate.getText().toString().trim().length() < 5){
                        amountDate = Integer.parseInt(objDate.getText().toString());
                    } else {
                        if(mToast != null) mToast.cancel();
                        mToast = Toast.makeText(ObjectiveActivity.this, "You are joking with me!", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                    
                } else{
                    amountDate = 0;
                }

                calendar.add(Calendar.DATE,amountDate);
                String computedDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                curDate.setText(computedDate);
                untilWhen = computedDate;
                calendar.add(Calendar.DATE,1);
                resetDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
            }
        });

        setCurDateAndTime();
    }

    private void setCurDateAndTime(){
        Calendar calendar = Calendar.getInstance();
        String curDateInString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        curDate.setText(curDateInString);
    }

    private void setDatabase(Objective objective, Achieved achieved){
        Intent intent = getIntent();
        String userKey = intent.getStringExtra(USER_KEY);

        FirebaseFirestore root = FirebaseFirestore.getInstance();
        DocumentReference docRef = root.collection(userKey).document(OBJ_PATH);
        docRef.set(objective)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ObjectiveActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        DocumentReference achievedRef = root.collection(userKey).document(ACHIEVED_PATH);
        achievedRef.set(achieved)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ObjectiveActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}