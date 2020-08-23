package com.example.saveyourmoney;

public class Crown {

    int crownNum;

    public Crown(){
        //No - args constructor required
    }

    public Crown(int crown){
        this.crownNum = crown;
    }

    public int getCrownNum() {
        return crownNum;
    }

    public void setCrownNum(int crownNum) {
        this.crownNum = crownNum;
    }
}
