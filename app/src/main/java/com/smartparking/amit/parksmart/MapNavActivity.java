package com.smartparking.amit.parksmart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapNavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ImageView ProfileImage, menuIcon;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private MapsFragment mapsFragment;
    private FragmentManager fragmentManager;
    private String currentFragment;
    Dialog myDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_nav);
        ProfileImage = findViewById(R.id.profilePicId);
        ///////////////////Hamburger_Icon_To_Toggle_Drawer/////////////////////////////////
        menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawer(R.id.nav_view);
                }
                else{
                    mDrawerLayout.openDrawer(Gravity.START);
                }
            }
        });
        //////////////////////////FIREBASE_DATABASE////////////////////////////////////////////////////////
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        try{
            mDatabase.setPersistenceEnabled(true);
        }catch (Throwable t0 ){
            Log.d("dataPersistence",""+t0.getMessage());
        }


        ////////////////////////MAP_FRAGMENT//////////////////////////////////////////////
        FusedLocationProviderClient mFusedLocationProvider =  LocationServices.getFusedLocationProviderClient(this);
        GeoDataClient mGeodataClient = Places.getGeoDataClient(this, null);
        PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        if(statusCheck()) {
            mapsFragment = new MapsFragment();
            mapsFragment.setmFusedLocationProviderClient(mFusedLocationProvider);
            mapsFragment.setmGeoDataClient(mGeodataClient);
            mapsFragment.setmPlaceDetectionClient(mPlaceDetectionClient);
            FragmentManager mManager = getSupportFragmentManager();
            mManager.beginTransaction().replace(R.id.map_container, mapsFragment).commit();
        }
        else{

        }
        //////////////////////////Navigation_Drawer_ItemSelectListener/////////////////////////////////////
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d("Handle", "handleMessage: " + msg.what);
            if (msg.what == 0) {
                Log.d("Handle", "onStart: " + user.getPhotoUrl().toString());
                try {
                    GlideApp.with(MapNavActivity.this)
                            .load(user.getPhotoUrl().toString())
                            .into(ProfileImage);
                }
                catch (Exception e){
                    Log.d("Message", user.getPhotoUrl().toString());
                }
            }
        }
    };
    //////////////////////////Permission_Check////////////////////////////////////////////////////
    public boolean statusCheck(){
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        else
            return(true);
        return false;
    }
    private void buildAlertMessageNoGps(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapNavActivity.this);
        builder.setMessage("Enable your location")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public void profilePicClick(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivityForResult(intent,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_nav, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        Log.d("whatispassed", "onResume: "+intent.getIntExtra("BookingCode",0));
        String a = intent.getStringExtra("BookingCode");
        if(a!=null){
            Log.d("Value of a", "onResume: "+a);
            String B = "Booked";
            if(a.equals(B.toString())){
                myDialog = new Dialog(this);
                myDialog.setContentView(R.layout.custombookingconfirmed);
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();
            }
        }
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.my_parkings) {
            Intent I = new Intent(MapNavActivity.this, BookingHistoryActivity.class);
            startActivity(I);
        } else if (id == R.id.help) {

        } else if (id == R.id.payment) {

        } else if (id == R.id.about_us) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.rent_your_space) {
            Intent I = new Intent(MapNavActivity.this,SpaceRentActivity.class);
            startActivity(I);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null && user.getPhotoUrl() != null) {
            handler.sendEmptyMessage(0);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Fragment f = getFragmentManager().findFragmentById(R.id.bottom_sheet);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (f != null)
        {
            if(f.equals(BookingConfirmedFragment.class)){
                Toast.makeText(MapNavActivity.this,"Do not close the app!",Toast.LENGTH_SHORT).show();
            }

        }
        else{
            super.onBackPressed();
        }
    }
}
