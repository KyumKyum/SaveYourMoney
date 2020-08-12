package com.example.saveyourmoney;

public class Expenditure {
    private String date;
    private String whatFor;
    private int howMuch;

    public Expenditure(String curDate, String whatFor, int howMuch) {
        this.date = curDate;
        this.whatFor = whatFor;
        this.howMuch = howMuch;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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


