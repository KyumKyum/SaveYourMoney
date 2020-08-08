package com.example.saveyourmoney;

import java.text.DateFormat;
import java.util.Calendar;

public class Expenditure {
    private String Date;
    private String whatFor;
    private int howMuch;

    public Expenditure(String whatFor, int howMuch) {
        Date = setCurDate();
        this.whatFor = whatFor;
        this.howMuch = howMuch;
    }

    private String setCurDate(){
        Calendar calendar = Calendar.getInstance();
        return DateFormat.getDateInstance(DateFormat.LONG).format(calendar);
    }

    public String getWhatFor() {
        return whatFor;
    }

    public void setWhatFor(String whatFor) {
        this.whatFor = whatFor;
    }

    public int getHowMuch() {
        return howMuch;
    }

    public void setHowMuch(int howMuch) {
        this.howMuch = howMuch;
    }
}


