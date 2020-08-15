package com.example.saveyourmoney;

import com.google.firebase.firestore.PropertyName;

public class Expenditure {
    private String date;
    private String whatFor;
    private int howMuch;
    private int priority;

    public Expenditure(){
        //No - args constructor Required
    }

    public Expenditure(String curDate, String whatFor, int howMuch, int priority) {
        this.date = curDate;
        this.whatFor = whatFor;
        this.howMuch = howMuch;
        this.priority = priority;
    }

    @PropertyName("date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @PropertyName("whatFor")
    public String getWhatFor() {
        return whatFor;
    }

    public void setWhatFor(String whatFor) {
        this.whatFor = whatFor;
    }

    @PropertyName("howMuch")
    public int getHowMuch() {
        return howMuch;
    }

    public void setHowMuch(int howMuch) {
        this.howMuch = howMuch;
    }

    @PropertyName("priority")
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}


