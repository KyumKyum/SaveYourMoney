package com.example.saveyourmoney;

public class TotalExpenditure {

    private int total;

    public TotalExpenditure(){
        //No-args constructor required
    }

    public TotalExpenditure(int curSpent){
        this.total = curSpent;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
