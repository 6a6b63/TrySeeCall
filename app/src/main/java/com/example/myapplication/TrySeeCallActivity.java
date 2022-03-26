package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.os.Bundle;
import android.widget.Button;
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

//implements OnMapReadyCallback //insert here
public class TrySeeCallActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback {

    public static final int DEFAULT_UPDATE_INTERVAL = 20;
    public static final int FAST_UPDATE_INTERVAL = 3;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private LocationCallback locationCallBack;
  // private static final String FINE_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    DBHelper DB;
    private GoogleMap mMap;


    Button book;
    TextView vtoken,tvLat,tvLong;
    String tok;

    public static double lat = 0;
    public static double lng = 0;
    public static String user_id = null;
    Switch sw_locUpdate, sw_gps;
    String note = "TEST";



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

        //initiate
        vtoken = findViewById(R.id.token);
        book = findViewById(R.id.btn_booknow);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locUpdate = findViewById(R.id.sw_locationupdates);
        tvLat = findViewById(R.id.tv_lat);
        tvLong = findViewById(R.id.tv_long);

        //token text
        vtoken.setText(tok);

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

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LAT:",String.valueOf(lat));
                Log.d("LAT:",String.valueOf(lng));
                Log.d("userID:", user_id);
                checkLocation();

                int id = Integer.parseInt(user_id);

                bookUp(id,note);
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
                    startLocationUpdates();
                }else{
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();

    }//endoncreatemethod

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

    private void getLatLong(Location location) {
            tvLat.setText(String.valueOf(location.getLatitude()));
            tvLong.setText(String.valueOf(location.getLongitude()));

            String glat = tvLat.getText().toString();
            String glng = tvLong.getText().toString();

            lat = Double.parseDouble(glat);
            lng = Double.parseDouble(glng);
    }

    public void checkLocation() {
        String x = String.valueOf(lat);
        String y = String.valueOf(lng);

        //String url = "http://192.168.1.3/api/v2/location/check/"+ x + "/" + y;
        String url = "http://192.168.1.3/api/v2/location/check/" + lat + "/" + lng;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);

                Gson g = new Gson();

                JsonObject jsonObject = g.fromJson(response, JsonObject.class);

                if (jsonObject.get("success").toString().equals("false")) {
                    Toast.makeText(TrySeeCallActivity.this, "OUT OF BOUNDS", Toast.LENGTH_SHORT).show();
                }
                if (jsonObject.get("success").toString().equals("true")) {
                    Toast.makeText(TrySeeCallActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                }
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

    public void getInfo(){
        String url = "http://192.168.1.3/api/v2/users/me";

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

                        Log.d("userID:", user_id);


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
    public void bookUp(int id,String note){
        String url ="http://192.168.1.3/api/v2/booking";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);
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
                  map.put("passenger_note", String.valueOf(note));
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

        Log.d("Lat:",latLng.toString());

       mMap.addMarker(new MarkerOptions().position(latLng));
       mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }
}