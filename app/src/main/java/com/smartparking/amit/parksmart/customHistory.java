package com.smartparking.amit.parksmart;

public class customHistory {
    private String mSystemName;
    private long mBill;
    private String mDate;

    public customHistory(String SystemName, String MyDate, long Bill){
        this.mSystemName = SystemName;
        this.mDate = MyDate;
        this.mBill = Bill;
    }

    public long getmBill() {
        return mBill;
    }

    public String getmSystemName() {
        return mSystemName;
    }

    public String getmSlot() {
        return mDate;
    }
}
