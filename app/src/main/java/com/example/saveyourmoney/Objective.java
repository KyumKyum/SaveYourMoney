package com.example.saveyourmoney;

import com.google.firebase.firestore.PropertyName;

public class Objective {

    String goal;
    String dueDate;
    String resetDate;

    public Objective(){
        //no-args constructor required
    }

    public Objective(String goal, String dueDate,String resetDate) {
        this.goal = goal;
        this.dueDate = dueDate;
        this.resetDate = resetDate;
    }

    @PropertyName("goal")
    public String getGoal() {
        return goal;
    }

    @PropertyName("dueDate")
    public String getDueDate() {
        return dueDate;
    }

    @PropertyName("resetDate")
    public String getResetDate() {
        return resetDate;
    }
}
