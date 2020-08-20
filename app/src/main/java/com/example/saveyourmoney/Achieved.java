package com.example.saveyourmoney;

public class Achieved {

    boolean isAchieved;

    public Achieved(){
        //No-args constructor required
    }

    public Achieved(boolean isAchieved){
        this.isAchieved = isAchieved;
    }

    public boolean getAchieved() {
        return isAchieved;
    }

    public void setAchieved(boolean achieved) {
        isAchieved = achieved;
    }
}
