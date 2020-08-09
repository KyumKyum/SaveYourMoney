package com.example.saveyourmoney;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.Calendar;

public class ObjectiveActivity extends AppCompatActivity {

    private final static String TARGET_EXPENDITURE = "TARGET_EXPENDITURE";
    private final static String TARGET_DATE = "UNTIL_WHEN";

    TextView curDate;

    EditText objExpenditure;
    EditText objDate;

    Button setObj;
    
    String untilWhen;

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
                Intent objIntent = new Intent();
                objIntent.putExtra(TARGET_EXPENDITURE, objExpenditure.getText().toString());
                objIntent.putExtra(TARGET_DATE,untilWhen);
                setResult(RESULT_OK, objIntent);
                finish();
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
            }
        });

        setCurDateAndTime();
    }

    private void setCurDateAndTime(){
        Calendar calendar = Calendar.getInstance();
        String curDateInString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        curDate.setText(curDateInString);
    }
}