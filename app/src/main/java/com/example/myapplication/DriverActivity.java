package com.example.myapplication;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DriverActivity extends AppCompatActivity implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback {


    DBHelper DB;
    private GoogleMap mMap;
    public static final int DEFAULT_UPDATE_INTERVAL = 20;
    public static final int FAST_UPDATE_INTERVAL = 3;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private LocationCallback locationCallBack;
    public static double lat = 0;
    public static double lng = 0;
    public static String passenger_id = null;
    List<Location> locationList;
    String tok;
    Switch gps;
    boolean shutdown = false;
    ImageView refresh;
    TextView textLat,textLng;
    ImageView logout;
    Button bookinglist,tripStart,endTrip;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //locationList = new ArrayList<>();

        gps = findViewById(R.id.switch1);
        logout = findViewById(R.id.btn_logout);
        textLat = findViewById(R.id.text_lat);
        textLng = findViewById(R.id.text_long);
        bookinglist = findViewById(R.id.btn_checkbooking);
        tripStart = findViewById(R.id.btn_Trip);
        endTrip = findViewById(R.id.btn_EndTrip);

        DB = new DBHelper(this);

        // set all properties of LocationRequest
        locationRequest = LocationRequest.create();

        bookinglist.setVisibility(View.INVISIBLE);


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


        gps.setClickable(false);
        //declare method
        getInfo();
        updateGPS();
        endTrip();


        tripStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createNewTrip();

            }
        });

        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTrip();
            }
        });

        bookinglist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverActivity.this,BookingActivity.class);
                intent.putExtra("token",tok);
                startActivity(intent);

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTrip();
                Intent intent = new Intent(DriverActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });



        //GPS switch
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int id = Integer.parseInt(passenger_id);
                if(gps.isChecked()){

                     getOpenBookings();
                     broadcastDriverLocation();
                     broadcastLocation();
                     startLocationUpdates();

                     locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                }else{

                    stopLocationUpdates();
                    turnoffBroadCast();
                     mMap.clear();
                    shutdown = true;

                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

//        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getOpenBookings();
        broadcastLocation();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        getOpenBookings();
        broadcastLocation();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopLocationUpdates();
        turnoffBroadCast();
        mMap.clear();
        shutdown = true;
        //endTrip();
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverActivity.this);

        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            // You can use the API that requires the permission.
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

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
        textLat.setText(String.valueOf(location.getLatitude()));
        textLng.setText(String.valueOf(location.getLongitude()));


        String glat = textLat.getText().toString();
        String glng = textLng.getText().toString();

        lat = Double.parseDouble(glat);
        lng = Double.parseDouble(glng);

        LatLng latLng = new LatLng(lat,lng);

        Log.d("Lat:",latLng.toString());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng)
        .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_bike_scooter_24)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    //view drivers location
    public void broadcastDriverLocation()
    {
        String url = "https://tryseecall.davao.dev/api/v2/trip/open";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("LOCATE DRIVER LOCATIONS:",response);
                try {
                    mMap.clear();
                    JSONObject data = new JSONObject(response);
                    JSONArray bookings = data.getJSONArray("data");
                    for(int i = 0; i < bookings.length(); i++){
                        JSONObject booking = bookings.getJSONObject(i);

                        double latitude = booking.getDouble("lat");
                        double longitude = booking.getDouble("lang");

                        Log.d("lat",String.valueOf(latitude));
                        Log.d("lng",String.valueOf(longitude));

                        LatLng latLng = new LatLng(latitude,longitude);

                        Log.d("Lat:",latLng.toString());

                        mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_bike_scooter_24)));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!shutdown) {
                            broadcastDriverLocation();
                        }
                    }
                },15000);

            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d("error", String.valueOf(error));
                String body = null;
                try {
                    body = new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("errorzzzz","" + body);
                Gson g = new Gson();
                JsonObject jsonObject = g.fromJson(body,JsonObject.class);


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
                tok = DB.getToken();


                String auth = "Bearer " + tok;
                headers.put("Authorization", auth);
                Log.d("headers",headers.toString());


                return headers;
            }

        };
        requestQueue.add(stringRequest);
    }

    public void createNewTrip(){
        String url = "https://tryseecall.davao.dev/api/v2/trip/new";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);


                        bookinglist.setVisibility(View.VISIBLE);
                        gps.setClickable(true);



                        Toast.makeText(DriverActivity.this,"SUCCESFULLY STARTED YOUR TRIP\nENJOY PICKING UP PASSENGERS",Toast.LENGTH_SHORT).show();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!shutdown) {
                                    broadcastLocation();
                                }
                            }
                        },20000);

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


//                if(body == null) {
//                    Toast.makeText(DriverActivity.this, body, Toast.LENGTH_SHORT).show();
//                }else{
//                    endTrip();
//                }



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
                tok = DB.getToken();

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


                        bookinglist.setVisibility(View.INVISIBLE);

                        Toast.makeText(DriverActivity.this,"SUCCESFULLY ENDED YOUR TRIP\nENJOY YOUR DAY",Toast.LENGTH_SHORT).show();


                        turnoffBroadCast();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!shutdown) {

                                }
                            }
                        },20000);

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


//                if(body == null) {
//                    Toast.makeText(DriverActivity.this, body, Toast.LENGTH_SHORT).show();
//                }else {
//                    createNewTrip();
//                }

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
                tok = DB.getToken();

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

    //delete latitude longtitude
    public void turnoffBroadCast(){
        String url = "https://tryseecall.davao.dev/api/v2/location/driver";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!shutdown) {
                                  //  broadcastLocation();
                                }
                            }
                        },15000);

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

                Log.d("latxxx:", String.valueOf(lat));
                Log.d("lngxxx:", String.valueOf(lng));

                map.put("lat", String.valueOf(0));
                map.put("lang", String.valueOf(0));


                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                // add headers <key,value>
                tok = DB.getToken();

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


    //inert driver lat,lng
    public void broadcastLocation(){
        String url = "https://tryseecall.davao.dev/api/v2/location/driver";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);
                        Log.d("okay",response);


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!shutdown) {
                                    broadcastLocation();
                                }
                            }
                        },15000);

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

                Log.d("latxxx:", String.valueOf(lat));
                Log.d("lngxxx:", String.valueOf(lng));

                map.put("lat", String.valueOf(lat));
                map.put("lang", String.valueOf(lng));


                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                // add headers <key,value>
                tok = DB.getToken();

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
    ////getpassenger
    public void getOpenBookings(){
       // String url = "http://192.168.1.3/api/v2/booking/open";
        String url = "https://tryseecall.davao.dev/api/v2/booking/open";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               Log.d("response",response);


                try {
                    JSONObject data = new JSONObject(response);
                    JSONArray bookings = data.getJSONArray("data");
                    for(int i = 0; i < bookings.length(); i++){
                        JSONObject booking = bookings.getJSONObject(i);

                        double latitude = booking.getDouble("checkinlat");
                        double longitude = booking.getDouble("checkinlong");

                        Log.d("lat",String.valueOf(latitude));
                        Log.d("lng",String.valueOf(longitude));

                        LatLng latLng = new LatLng(latitude,longitude);

                        Log.d("Lat:",latLng.toString());

                        mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_person_pin_24)));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!shutdown) {
                            getOpenBookings();
                        }
                    }
                },15000);

            }
            },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
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



                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                // add headers <key,value>
                tok = DB.getToken();


                String auth = "Bearer " + tok;
                headers.put("Authorization", auth);
                Log.d("headers",headers.toString());


                return headers;
            }

        };
        requestQueue.add(stringRequest);
    }
    public void getInfo(){
        //String url = "http://192.168.1.3/api/v2/users/me";
        String url = "https://tryseecall.davao.dev/api/v2/users/me";

        DB = new DBHelper(this);

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);

                        Gson g = new Gson();
                        JsonObject jsonObject = g.fromJson(response,JsonObject.class);

                        JsonObject jsonData = g.fromJson(jsonObject.get("data"),JsonObject.class);
                        jsonObject.get("data").toString();
                        passenger_id = jsonData.get("id").toString().replaceAll("\"", "");
//                        user_level = jsonData.get("user_level_id").toString().replaceAll("\"", "");
//                        status = jsonData.get("user_level_id").toString().replaceAll("\"", "");
//                        Log.d("userID:", user_id);


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
                tok = DB.getToken();

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



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;


        LatLng latLng = new LatLng(lat,lng);

//        Log.d("onmap","7.144135261241745");
//
//        Log.d("Lat:",latLng.toString());


//        mMap.addMarker(new MarkerOptions().position(latLng)
//                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_bike_scooter_24)));

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

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }
}

