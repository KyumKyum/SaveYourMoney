package com.example.saveyourmoney;

import com.google.firebase.firestore.PropertyName;

public class Objective {

    String goal;
    String dueDate;

    public Objective(){
        //no-args constructor required
    }

    public Objective(String goal, String dueDate) {
        this.goal = goal;
        this.dueDate = dueDate;
    }

    @PropertyName("goal")
    public String getGoal() {
        return goal;
    }

    @PropertyName("dueDate")
    public String getDueDate() {
        return dueDate;
    }
}
