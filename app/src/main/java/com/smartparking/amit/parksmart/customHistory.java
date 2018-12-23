package com.smartparking.amit.parksmart;

public class customHistory {
    private String mSystemName;
    private long mBill;
    private String mDate,status;

    public customHistory(){

    }

    public customHistory(String SystemName, String MyDate, long Bill, String status){
        this.mSystemName = SystemName;
        this.mDate = MyDate;
        this.mBill = Bill;
        this.status = status;
    }

/*    public customHistory(String SystemName, String MyDate, String Status){
        this.mSystemName = SystemName;
        this.mDate = MyDate;
        this.status = Status;
    }*/
    public long getmBill() {
    return mBill;
}
    public String getmSystemName() {
        return mSystemName;
    }
    public String getmDate() {
        return mDate;
    }
    public String getstatus() {
        return status;
    }
}
