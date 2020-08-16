package com.example.saveyourmoney;

import com.google.firebase.firestore.PropertyName;

public class Priority {
    int priority;

    public Priority(){
        //No-args constructor required
    }

    public Priority(int priority){
        this.priority = priority;
    }

    @PropertyName("priority")
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
