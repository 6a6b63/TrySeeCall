package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//implements OnMapReadyCallback //insert here
public class TrySeeCallActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback {

    public static final int DEFAULT_UPDATE_INTERVAL = 20;
    public static final int FAST_UPDATE_INTERVAL = 3;
    private static final int PERMISSION_FINE_LOCATION = 99;
    public static String booking_id;
    private LocationCallback locationCallBack;
  // private static final String FINE_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    DBHelper DB;
    private GoogleMap mMap;


    Button book,cancel;
    EditText note;
    TextView vtoken,tvLat,tvLong,tvbId,tvStatus;
    String tok;
    ImageView btn_gps;

    public static double lat = 0;
    public static double lng = 0;
    public static String user_id = null;
    public static String driver_id = null;
    public static String user_level;
    public static String status;
    public static int booking_status;
    public static String latx = null;
    public static String lngx = null;
    Switch sw_locUpdate, sw_gps;
    boolean shutdown = false;
    //String note = "TEST";
    String getBook;
    ImageView logout;
    TextView bk_id;



    boolean updateOn = false;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_see_call);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DB = new DBHelper(this);


        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));
        //initiate
        vtoken = findViewById(R.id.token);
        note = findViewById(R.id.edt_number);
        book = findViewById(R.id.btn_booknow);
        cancel = findViewById(R.id.btn_cancel);
        btn_gps = findViewById(R.id.btn_gps);
        logout = findViewById(R.id.btn_log);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locUpdate = findViewById(R.id.sw_locationupdates);
        tvLat = findViewById(R.id.tv_lat);
        tvLong = findViewById(R.id.tv_long);
        tvbId = findViewById(R.id.tv_bookingID);
        tvStatus = findViewById(R.id.text_status);
        bk_id = findViewById(R.id.text_bookingnumber);

        //gps button
        btn_gps.setEnabled(false);
        book.setEnabled(false);




        //token text
        vtoken.setText(tok);

        //get Text of button Book
        String x = getBook = book.getText().toString();

         latx = tvLat.getText().toString();
         lngx = tvLat.getText().toString();


        // set all properties of LocationRequest
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

        //declare method
        getInfo();




        //booking button


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("userID:", user_id);

                int id = Integer.parseInt(user_id);

                cancelBooking(id);

                Intent intent = new Intent(TrySeeCallActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LAT:",String.valueOf(lat));
                Log.d("LAT:",String.valueOf(lng));
                Log.d("userID:", user_id);
                checkLocation();


                String notes;
                int id = Integer.parseInt(user_id);
                notes = note.getText().toString();

                Log.d("NOTENOTE ", notes);



//                 if(booking_status == 0){
//                    Toast.makeText(TrySeeCallActivity.this,"BOOKING REQUEST STILL PENDING....",Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(TrySeeCallActivity.this,"BOOKING REQUEST ACCEPTED, PLEASE WAIT FOR THE DRIVER FOR YOU TO BE PICK UP.",Toast.LENGTH_SHORT).show();
//                }


                bookUp(id,notes);

                //Toast.makeText(TrySeeCallActivity.this, "SUCCESSFULLY BOOKED", Toast.LENGTH_SHORT).show();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LAT:",String.valueOf(lat));
                Log.d("LAT:",String.valueOf(lng));
                Log.d("userID:", user_id);

                int id = Integer.parseInt(user_id);

                cancelBooking(id);
                Toast.makeText(TrySeeCallActivity.this, "SUCCESSFULLY CANCELLED", Toast.LENGTH_SHORT).show();
            }
        });



        //gps button
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateGPS();

            }
        });


        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    Toast.makeText(TrySeeCallActivity.this, "Using GPS Sensor", Toast.LENGTH_SHORT).show();
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    Toast.makeText(TrySeeCallActivity.this, "Using Towers + WIFI", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sw_locUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sw_locUpdate.isChecked()) {
                    btn_gps.setEnabled(true);
                    startLocationUpdates();
                    broadcastDriverLocation();
                    book.setEnabled(true);

                }else{
                    btn_gps.setEnabled(false);
                    mMap.clear();
                    stopLocationUpdates();
                    book.setEnabled(false);

                }
            }
        });

        updateGPS();



    }//endoncreatemethod

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

    }



    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

        tvLat.setText("GPS OFF");
        tvLong.setText("GPS OFF");
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(TrySeeCallActivity.this);

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

    //get latlng
    private void getLatLong(Location location) {
            tvLat.setText(String.valueOf(location.getLatitude()));
            tvLong.setText(String.valueOf(location.getLongitude()));

            String glat = tvLat.getText().toString();
            String glng = tvLong.getText().toString();

            lat = Double.parseDouble(glat);
            lng = Double.parseDouble(glng);

        LatLng latLng = new LatLng(lat,lng);

        Log.d("Lat:",latLng.toString());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_person_pin_24)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    //checking boundaries
    public void checkLocation() {
        String x = String.valueOf(lat);
        String y = String.valueOf(lng);

        //String url = "http://192.168.1.3/api/v2/location/check/"+ x + "/" + y;
        //String url = "http://192.168.1.3/api/v2/location/check/" + lat + "/" + lng;
        String url = "https://tryseecall.davao.dev/api/v2/location/check/" + lat + "/" +lng;




        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);

                Gson g = new Gson();

                JsonObject jsonObject = g.fromJson(response, JsonObject.class);


                if (jsonObject.get("success").toString().equals("false")) {
                    Toast.makeText(TrySeeCallActivity.this, "OUT OF BOUNDS", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(TrySeeCallActivity.this, "SUCESSFULLY BOOK", Toast.LENGTH_SHORT).show();
                }

//                if (jsonObject.get("success").toString().equals("true")) {
//                    Toast.makeText(TrySeeCallActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
//                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        }) {
            protected HashMap<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();


                return map;
            }

        };
        queue.add(stringRequest);
    }


    //cancel booking
    public void cancelBooking(int id){
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url = "http://192.168.1.3/api/v2/book/cancel";
        String url ="https://tryseecall.davao.dev/api/v2/booking/cancel";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                        Log.d("id:",String.valueOf(id));

                        book.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.INVISIBLE);

                        tvStatus.setText("   STATUS");
                        bk_id.setText(" ID:");


                        mMap.clear();
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

                map.put("passenger_id", String.valueOf(id));

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




    //view all driver location
    public void broadcastDriverLocation()
    {
        String url = "https://tryseecall.davao.dev/api/v2/trip/open";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("responsesssss",response);
                try {
                    //mMap.clear();
                    JSONObject data = new JSONObject(response);
                    JSONArray bookings = data.getJSONArray("data");
                    for(int i = 0; i < bookings.length(); i++){
                        JSONObject booking = bookings.getJSONObject(i);

                        double latitude = booking.getDouble("lat");
                        double longitude = booking.getDouble("lang");
                        int id = booking.getInt("driver_id");

                        Log.d("lat",String.valueOf(latitude));
                        Log.d("lng",String.valueOf(longitude));
                        Log.d("driver_id", String.valueOf(id));

                        LatLng latLng = new LatLng(latitude,longitude);

                        Log.d("Lat:",latLng.toString());


                        mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(String.valueOf(id))
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
                            checkStatus();
                            //Log.d("PASSENGER BOOKING ID: ",String.valueOf(booking_id));
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

    //get users information
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
                        user_id = jsonData.get("id").toString().replaceAll("\"", "");
                        user_level = jsonData.get("user_level_id").toString().replaceAll("\"", "");
                        status = jsonData.get("user_level_id").toString().replaceAll("\"", "");
                        Log.d("userID:", user_id);

//                        if(status.equals("0")){
//                            Toast.makeText(TrySeeCallActivity.this,"YOU NEED TO VERIFY THIS ACCOUNT!",Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(TrySeeCallActivity.this,LoginActivity.class);
//                            startActivity(intent);
//                        }
//                        if(user_level.equals("5")){
//                            Intent intent = new Intent(TrySeeCallActivity.this,DriverActivity.class);
//                            startActivity(intent);
//                        }


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
    protected void onStop() {
        super.onStop();

        Log.d("userID:", user_id);

        int id = Integer.parseInt(user_id);

        cancelBooking(id);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("userID:", user_id);
        int id = Integer.parseInt(user_id);

        cancelBooking(id);
    }

    //insert passenger location
    public void bookUp(int id,String note){
        //String url ="http://192.168.1.3/api/v2/booking";
        String url ="https://tryseecall.davao.dev/api/v2/booking";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);


                            //mMap.clear();
                        Gson g = new Gson();
                        JsonObject jsonObject = g.fromJson(response,JsonObject.class);
                        JsonObject jsonData = g.fromJson(jsonObject.get("data"),JsonObject.class);
                        jsonObject.get("data").toString();

                        booking_id = String.valueOf(jsonData.get("id"));
                        String ids = String.valueOf(jsonData.get("id"));


                        bk_id.setText("   ID:"+ ids.replaceAll("\"", ""));

                        checkStatus();
                       // Log.d("NOTE:", String.valueOf(jsonData.get("passenger_note")));


                        checkStatus();

//                        try {
//                            JSONObject data = new JSONObject(response);
//                            JSONArray bookings = data.getJSONArray("data");
//                            for(int i = 0; i < bookings.length(); i++){
//                                JSONObject booking = bookings.getJSONObject(i);
//
//
//                                booking_id = booking.getInt("id");
//
//
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }

                        tvbId.setText(booking_id);
                        book.setVisibility(View.INVISIBLE);
                        cancel.setVisibility(View.VISIBLE);

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



                    map.put("passenger_id", String.valueOf(id));
                    map.put("note", String.valueOf(note));
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
    public void checkStatus(){
        // String url = "http://192.168.1.3/api/v2/booking/open";

        //add booking_id sa URL    /" + booking_id
        String url = "https://tryseecall.davao.dev/api/v2/booking/checkstatus"+"?booking_id="+booking_id.replaceAll("\"", "");

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("responsexxxx",response);
                Log.d("URL URL:", url);

                Log.d("--------------: ", String.valueOf(booking_id));

                int asd;

                try {
                    JSONObject data = new JSONObject(response);
                     asd = data.getInt("booking_status");

                     Log.d("STATUUUUUUUUUUUUUUUUUUUS:", String.valueOf(asd));

                     if(asd == 0){
                         //Toast.makeText(TrySeeCallActivity.this,"WAITING FOR DRIVER TO ACCEPT BOOKING REQUEST..", Toast.LENGTH_SHORT).show();
                         tvStatus.setText("       STATUS: PENDING");
                     }else if(asd == 1){
                        //Toast.makeText(TrySeeCallActivity.this,"BOOK REQUEST IS ACCEPTED PLEASE WAIT FOR THE DRIVER FOR PICKUP....", Toast.LENGTH_SHORT).show();
                        tvStatus.setText("       STATUS: ACCEPTED");

                     }else if(asd == 2){
                        tvStatus.setText("       STATUS: PICKED UP");
                    }else if(asd == 3){
                        tvStatus.setText("       STATUS: DELIVERED");
                         Log.d("userID:", user_id);
                         int id = Integer.parseInt(user_id);
                         cancelBooking(id);
                         book.setVisibility(View.VISIBLE);
                         cancel.setVisibility(View.INVISIBLE);
                    }else{
                         tvStatus.setText("       STATUS");
                     }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


//                try {
//                    JSONObject data = new JSONObject(response);
//                    JSONArray bookings = data.getJSONArray("data");
//                    for(int i = 0; i < bookings.length(); i++){
//                        JSONObject booking = bookings.getJSONObject(i);
//
//
//                        //p_booking_id = booking.getInt("status");
//
//                        Log.d("status", booking.toString());
//
//
//                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    }
//                } catch (JSONException e) {
//
//                    e.printStackTrace();
//                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!shutdown) {
                            //checkStatus();
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

                //map.put("booking_id",booking_id);

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


    public void checkBookings(){
        //String url ="http://192.168.1.3/api/v2/booking/open";


        String url = "https://tryseecall.davao.dev/api/v2/booking/checkstatus";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response",response);



//                try {
//                    JsonElement element = gson.fromJson(jsonString, JsonElement.class);
//                    JsonObject jsonObject = element.getAsJsonObject();
//                    Integer[] integerArray = Arrays.asList(jsonData).toArray(new Integer[0]);
//                    String jsonString = response;
//                    JsonElement element = gson.fromJson(jsonString, JsonElement.class);
//                    JsonObject jsonObject = element.getAsJsonObject();

                //               if(jsonObject.get("success").toString().equals("true")) {
                // JsonObject jsonData = g.fromJson(jsonObject.get("data"), JsonObject.class);
                //                       Log.d("credentials", jsonObject.get("data").toString());
                //System.out.println(jsonObject.get("data").getAsString());Log.d("response", response);
                Gson g = new Gson();
                //               JsonObject jsonObject = g.fromJson(response,JsonObject.class);
                //  Log.d("jsonobject",jsonObject.get("data").toString());
                try {
                    //mMap.clear();
                    JSONObject data = new JSONObject(response);
                    JSONArray bookings = data.getJSONArray("data");
                    for(int i = 0; i < bookings.length(); i++){
                        JSONObject booking = bookings.getJSONObject(i);

                        int checkId = booking.getInt("passenger_id");




//                        double latitude = booking.getDouble("checkinlat");
//                        double longitude = booking.getDouble("checkinlong");
//                        Log.d("latx",String.valueOf(latitude));
//                        Log.d("lngx",String.valueOf(longitude));


                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //getOpenBookings();
//                    }
//                },5000);



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
                //         String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjUxMzIyOTI2LCJuYmYiOjE2NTEzMjI5MjYsImV4cCI6MTY1MTQwOTMyNiwiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiJraWtpQGdtYWlsLmNvbSIsInBhc3N3b3JkIjoiMTIzNCJ9.uRzR6Hq14kkIVIXFFMJ0hNRuq_SuRMi7RfF6hXKZXXk";
                headers.put("Authorization", auth);
                Log.d("headers",headers.toString());


                return headers;
            }

        };
        requestQueue.add(stringRequest);
    }


    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        LatLng latLng = new LatLng(lat,lng);

//        Log.d("Lat:",latLng.toString());


//        try {
//            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.));
//            if (!success) {
//                Log.e("MapsActivityRaw", "Style parsing failed.");
//            }
//        } catch (Resources.NotFoundException e) {
//            Log.e("MapsActivityRaw", "Can't find style.", e);
//        }

//       mMap.addMarker(new MarkerOptions().position(latLng)
//       .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_person_pin_24)));
//       mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(@NonNull Marker marker) {
//
//                Log.d("driver_idxxxx:", driver_id);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(TrySeeCallActivity.this);
//                builder.setTitle("Would you like to book the Driver?");
//                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        String id = marker.getTitle();
//
//                        Log.d("driver_idxxxx:", driver_id);
//                        int driver_id = Integer.parseInt(id);
//                            Double latitude = Double.parseDouble(latx);
//                            Double longtitude = Double.parseDouble(lngx);

//                            Log.d("MOMOMOMOMO", latx);
//                            Log.d("MXXXXXXXXX", lngx);

                            //exclusive(driver_id);

//                            Intent intent = new Intent(TrySeeCallActivity.this,BookingActivity.class);
//                            startActivity(intent);
//                    }
//                });
//
//                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //do nothing
//                    }
//                });
//
//                builder.create();
//                builder.show();
//
//                return true;
//            }
//        });

    }

    public void exclusive(int id){
//        String url ="https://tryseecall.davao.dev/api/v2/booking/exclusive/"+ lat + "/" +lng;
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("response",response);
//
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("error", String.valueOf(error));
//                String body = null;
//                try {
//                    body = new String(error.networkResponse.data, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                Log.d("error","" + body);
//                Gson g = new Gson();
//                JsonObject jsonObject = g.fromJson(body,JsonObject.class);
//
//
//            }
//        }){
//            protected HashMap<String,String> getParams() throws AuthFailureError {
//                HashMap<String,String> map = new HashMap<>();
//
//
//                map.put("driver_id", String.valueOf(id));
//                map.put("lat", String.valueOf(lat));
//                map.put("lang", String.valueOf(lng));
//
//
//                return map;
//            }
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> headers = new HashMap<>();
//                // add headers <key,value>
//                tok = DB.getToken();
//
//                Log.d("headers",headers.toString());
//                Log.d("token: ", tok);
//                //String credentials = username.getText().toString()+":"+ password.getText().toString();
////                String credentials = "6a6b63@gmail.com" +":"+ "1234";
////                String auth = "Basic "
////                        + Base64.encodeToString(credentials.getBytes(),
////                        Base64.NO_WRAP);
//                String auth = "Bearer " + tok;
//                // String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3Mzc3NDE3LCJuYmYiOjE2NDczNzc0MTcsImV4cCI6MTY0NzQ2MzgxNywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiI2YTZiNjNAZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.gfKFXyK3tPs17D3DQ9oiSWBHsO14qtgDILoOJ90-qUE";
//                headers.put("Authorization", auth);
//                Log.d("headers",headers.toString());
//
//
//                return headers;
//            }
//
//        };
//        queue.add(stringRequest);

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