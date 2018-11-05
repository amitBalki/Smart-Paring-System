package com.smartparking.amit.parksmart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class BookingHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);
        FirebaseUser UserID = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference BookingRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(UserID.getUid())
                .child("MyBookings");
        if(BookingRef==null){
            //Todo No Data
        }else {
            final ListView listView = findViewById(R.id.HistList);
            final ArrayList<customHistory> bookinghistory = new ArrayList<customHistory>();
            final CustomAdapter arrayAdapter = new CustomAdapter(this, bookinghistory);
            listView.setAdapter(arrayAdapter);
            BookingRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String sysName = dataSnapshot.child("SystemName").getValue().toString();
                    String date = dataSnapshot.child("MyDate").getValue().toString();
                    long bill = (long) dataSnapshot.child("Bill").getValue();
                    customHistory value = new customHistory(sysName, date, bill);
                    //customHistory value = dataSnapshot.getValue(customHistory.class);
                    bookinghistory.add(value);
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
