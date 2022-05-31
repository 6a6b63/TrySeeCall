package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntryDriverActivity extends AppCompatActivity  implements OnMapReadyCallback{

    private GoogleMap mMap;
    DBHelper DB;
    public static double lat = 0;
    public static double lng = 0;
    String tok;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    public static final int DEFAULT_UPDATE_INTERVAL = 20;
    public static final int FAST_UPDATE_INTERVAL = 3;
    private static final int PERMISSION_FINE_LOCATION = 99;
    Button endTrip,pickup,dropoff;
    Polyline polyline;
    MarkerOptions place1,place2;
    TextView bookingid;
    String bkid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        bkid = intent.getStringExtra("id");
        tok = intent.getStringExtra("token");

        bookingid = findViewById(R.id.bkid);
        endTrip = findViewById(R.id.btn_endTheTrip);
        pickup = findViewById(R.id.btn_pickup);
        dropoff = findViewById(R.id.btn_drop);


        endTrip.setVisibility(View.INVISIBLE);

        bookingid.setText("PASSENGER BOOKING ID: "+bkid);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));


        locationRequest = LocationRequest.create();

        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback(){

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                getLatLong(locationResult.getLastLocation());
            }
        };


        updateGPS();

        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTrip();
                finish();
            }
        });

        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickupTrip();
            }
        });

        dropoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropoffTrip();
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

//        viewPassenger();
        startLocationUpdates();
    }

        public void pickupTrip(){

                    //Toast.makeText(mctx, latlng, Toast.LENGTH_SHORT).show();
                    String url = "https://tryseecall.davao.dev/api/v2/booking/pickup";

                    RequestQueue queue = Volley.newRequestQueue(this);


                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("response",response);


                                    pickup.setVisibility(View.INVISIBLE);
                                    dropoff.setVisibility(View.VISIBLE);


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error", String.valueOf(error));
                            String body = null;
                            try {
                                body = new String(error.networkResponse.data, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Log.d("error","" + body);
                            Gson g = new Gson();
                            JsonObject jsonObject = g.fromJson(body,JsonObject.class);


                        }
                    }){
                        protected HashMap<String,String> getParams() throws AuthFailureError {
                            HashMap<String,String> map = new HashMap<>();


                            map.put("booking_id", bkid.replaceAll("\"", ""));


                            return map;
                        }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> headers = new HashMap<>();

                          //  DB = new DBHelper(this);
//                        Intent intent = mctx.getIntent();
//                        Intent intent = new getIntent();
//                        tok = intent.getStringExtra("token");
                            // add headers <key,value>
                            //tok = DB.getToken();

                            Log.d("headers",headers.toString());
                            Log.d("token: ", tok);
                            //String credentials = username.getText().toString()+":"+ password.getText().toString();
//                String credentials = "6a6b63@gmail.com" +":"+ "1234";
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(),
//                        Base64.NO_WRAP);
                            String auth = "Bearer " + tok;
                            // String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3Mzc3NDE3LCJuYmYiOjE2NDczNzc0MTcsImV4cCI6MTY0NzQ2MzgxNywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiI2YTZiNjNAZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.gfKFXyK3tPs17D3DQ9oiSWBHsO14qtgDILoOJ90-qUE";
                            headers.put("Authorization", auth);
                            Log.d("headers",headers.toString());


                            return headers;
                        }

                    };
                    queue.add(stringRequest);
                }


        public void dropoffTrip(){
            String url = "https://tryseecall.davao.dev/api/v2/booking/delivered";

            RequestQueue queue = Volley.newRequestQueue(this);


            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("response",response);


                            //pickup.setVisibility(View.INVISIBLE);
                            dropoff.setVisibility(View.INVISIBLE);
                            endTrip.setVisibility(View.VISIBLE);


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", String.valueOf(error));
                    String body = null;
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d("error","" + body);
                    Gson g = new Gson();
                    JsonObject jsonObject = g.fromJson(body,JsonObject.class);


                }
            }){
                protected HashMap<String,String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();


                    map.put("booking_id", bkid.replaceAll("\"", ""));


                    return map;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();

                    //  DB = new DBHelper(this);
//                        Intent intent = mctx.getIntent();
//                        Intent intent = new getIntent();
//                        tok = intent.getStringExtra("token");
                    // add headers <key,value>
                    //tok = DB.getToken();

                    Log.d("headers",headers.toString());
                    Log.d("token: ", tok);
                    //String credentials = username.getText().toString()+":"+ password.getText().toString();
//                String credentials = "6a6b63@gmail.com" +":"+ "1234";
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(),
//                        Base64.NO_WRAP);
                    String auth = "Bearer " + tok;
                    // String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3Mzc3NDE3LCJuYmYiOjE2NDczNzc0MTcsImV4cCI6MTY0NzQ2MzgxNywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiI2YTZiNjNAZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.gfKFXyK3tPs17D3DQ9oiSWBHsO14qtgDILoOJ90-qUE";
                    headers.put("Authorization", auth);
                    Log.d("headers",headers.toString());


                    return headers;
                }

            };
            queue.add(stringRequest);
        }

        public void endTrip(){
            String url = "https://tryseecall.davao.dev/api/v2/trip/close";

            RequestQueue queue = Volley.newRequestQueue(this);


            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("response",response);

                            Intent intent = new Intent(EntryDriverActivity.this, DriverActivity.class);
                            startActivity(intent);

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                }
                            },20000);

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", String.valueOf(error));



                }
            }){
                protected HashMap<String,String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();



                    return map;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    // add headers <key,value>
//                    tok = DB.getToken();

                    Log.d("headers",headers.toString());
                    Log.d("token: ", tok);
                    //String credentials = username.getText().toString()+":"+ password.getText().toString();
//                String credentials = "6a6b63@gmail.com" +":"+ "1234";
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(),
//                        Base64.NO_WRAP);
                    String auth = "Bearer " + tok;
                    // String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3Mzc3NDE3LCJuYmYiOjE2NDczNzc0MTcsImV4cCI6MTY0NzQ2MzgxNywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiI2YTZiNjNAZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.gfKFXyK3tPs17D3DQ9oiSWBHsO14qtgDILoOJ90-qUE";
                    headers.put("Authorization", auth);
                    Log.d("headers",headers.toString());


                    return headers;
                }

            };
            queue.add(stringRequest);
        }


    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void updateGPS() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(EntryDriverActivity.this);

        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            // You can use the API that requires the permission.
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {

                    if(location != null){
                        getLatLong(location);
                        //currentLocation = location;
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void getLatLong(Location location) {


        String glat = String.valueOf(location.getLatitude());
        String glng = String.valueOf(location.getLongitude());

        lat = Double.parseDouble(glat);
        lng = Double.parseDouble(glng);

        LatLng latLng = new LatLng(lat,lng);


        Log.d("Lat:",latLng.toString());


        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_bike_scooter_24)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

//        LatLng latLng = new LatLng(lat,lng);

        Intent intent = getIntent();
        double xlat = Double.parseDouble(intent.getStringExtra("x"));
        double xlng = Double.parseDouble(intent.getStringExtra("y"));

        Log.d("momomomo:",String.valueOf(xlng));
        Log.d("wawawawa:",String.valueOf(xlat));


        LatLng latLng = new LatLng(xlat,xlng);


        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_person_pin_24)));

//        place2 = new MarkerOptions().position(latLng)

        //mMap.addMarker(new MarkerOptions().position(latLng));


//
//        Log.d("onmap","7.144135261241745");
//
//        Log.d("Lat:",latLng.toString());
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private BitmapDescriptor bitmapDescriptor(Context context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);


    }
}