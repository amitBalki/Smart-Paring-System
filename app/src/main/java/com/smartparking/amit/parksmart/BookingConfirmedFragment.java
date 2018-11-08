package com.smartparking.amit.parksmart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class BookingConfirmedFragment extends Fragment implements OnMapReadyCallback {

    private BottomSheetBehavior sheetBehavior;
    private LinearLayout bottom_sheet;
    private Button Navigate,Cancel;
    private ImageView iv;
    Bitmap bitmap;
    private String ParkingName,SlotName;
    public final static int QRcodeWidth = 700 ;
    GoogleMap mGoogleMap;
    View view;
    MapView mMapView;
    public BookingConfirmedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking_confirmed, container, false);
        bottom_sheet = view.findViewById(R.id.booking_confirmed);
        ParkingName = this.getArguments().getString("ParkingName");
        Navigate = view.findViewById(R.id.navigate);
        Cancel = view.findViewById(R.id.cancel);
        iv = view.findViewById(R.id.iv);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String qrString = (userId+"$"+ FirebaseDatabase.getInstance().getReference("Parkings").child("Booked").child(userId).child("OTP").toString());
                if(qrString.length() == 0){

                }else {
                    try {
                        bitmap = TextToImageEncode(qrString);
                        handler.sendEmptyMessage(0);
                        // String path = saveImage(bitmap);  //give read write permission
                        //  Toast.makeText(MainActivity.this, "QRCode saved to -> "+path, Toast.LENGTH_SHORT).show();
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            }
        }).start();
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Parkings")
                            .child(ParkingName)
                            .child("Slots")
                            .child("Booked");
                        final DatabaseReference userData = data.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Log.d(TAG, "run: "+userData.toString());
                        userData.child("Slot").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String SlotNumber;
                                SlotName = dataSnapshot.getValue().toString();
                                if(SlotName!=null){
                                    Log.d(TAG, "run: "+SlotName);
                                    SlotNumber = SlotName.substring(4);     //Starting Index of Slot Number Eg. Slot2
                                    userData.removeValue();
                                    data.getParent().child("Available")
                                            .child(SlotNumber).setValue(SlotName);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                }).start();
                Intent intent = new Intent(getActivity(),MapNavActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getActivity().finish();
                Toast.makeText(getActivity(),"Booking Cancelled", Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });

        view.findViewById(R.id.proceedpayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PaymentActivity.class));
            }
        });

        Navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        return view;
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                iv.setImageBitmap(bitmap);
            }
        }
    };
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ////////////////////Navigation////////////////////////////////////////////////////
        mMapView = view.findViewById(R.id.navigate_map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
        ////////////////////BottomSheet////////////////////////////////////////////////////
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    private Bitmap TextToImageEncode(String Value) throws WriterException  {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 700, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Bundle bundle = this.getArguments();
        ArrayList<Marker> markers = new ArrayList<Marker>();    //Arraylist to store source and destination marker
        Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()   //Source marker
                .position(new LatLng(bundle.getDouble("CurrentLat"), bundle.getDouble("CurrentLong")))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        markers.add(mMarker);                                   //Adding source marker to Arraylist
        mMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(bundle.getDouble("DestinationLat"),bundle.getDouble("DestinationLong")))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));  //Destination Marker
        markers.add(mMarker);                                    //Adding Destination marker to Arraylist
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 45; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
        String url = generateURL(bundle);
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);
    }
    private String requestDirection(String reqUrl) throws IOException, IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    private String generateURL(Bundle bundle) {
        String url= null;
        if (bundle != null) {
            String origin = "origin="+bundle.getDouble("CurrentLat")+","+bundle.getDouble("CurrentLong");
            String dest = "destination="+bundle.getDouble("DestinationLat")+","+bundle.getDouble("DestinationLong");
            String sensor = "sensor=flase";
            String mode = "mode=driving";
            String param = origin + "&"+dest+"&"+sensor+"&"+mode;
            url = "https://maps.googleapis.com/maps/api/directions/" +"json"+"?"+param;


        }
        return url;
    }
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }
    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.BLACK);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                mGoogleMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getActivity(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
