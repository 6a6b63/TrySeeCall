package com.example.myapplication;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback {


    DBHelper DB;
    private GoogleMap mMap;

    public static double lat = 0;
    public static double lng = 0;
    RecyclerView recyclerView;
    LocationAdapter adapter;
    List<Location> locationList;
    Button getLocation;

    String latloc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationList = new ArrayList<>();

        recyclerView = findViewById(R.id.rv_locationlist);
        getLocation = findViewById(R.id.btn_getLatLng);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager((this)));



        getOpenBookings();

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOpenBookings();
            }
        });
    }

    public void getOpenBookings(){
        String url ="http://192.168.1.3/api/v2/booking/open";
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
                    mMap.clear();
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

                        mMap.addMarker(new MarkerOptions().position(latLng));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getOpenBookings();
                    }
                },5000);


 //                                       for(int i = 0; i < jsonArray.length(); i++){
//                                            try {
//                                                JSONObject booking = jsonArray.getJSONObject(i);
//                                                lat = booking.getDouble("checkinlat");
//                                                lng = booking.getDouble("checkinlong");

//                                                Log.d("lat",String.valueOf(lat));
//                                                Log.d("lng",String.valueOf(lng));


//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }


 //                                       }


//                                        jsonObject.get("data").toString();
//                                        lat = jsonData.get("checkinlat");
//                                        lng = jsonData.get("checkinlong");
//
//                                        Log.d("lat:", lat);
//                                        Log.d("lng:", lng);


//                    }
//
//                    JSONArray location = new JSONArray(response);
//
//                    for(int i = 0; i < location.length(); i++){
//                        JSONArray locationObject = location.getJSONArray(i);
//
//                        Location locationxxx = new Location(
//                                locationObject.getInt(0),
//                                locationObject.getInt(1),
//                                locationObject.getString(3));
//
//                        locationList.add(locationxxx);
//                    }
//                    adapter = new LocationAdapter(DriverActivity.this,locationList);
//                    recyclerView.setAdapter(adapter);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

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
//                tok = DB.getToken();

//                Log.d("headers",headers.toString());
//                Log.d("token: ", tok);
                //String credentials = username.getText().toString()+":"+ password.getText().toString();
//                String credentials = "6a6b63@gmail.com" +":"+ "1234";
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(),
//                        Base64.NO_WRAP);
                //String auth = "Bearer " + tok;
                String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3NTUwNzYzLCJuYmYiOjE2NDc1NTA3NjMsImV4cCI6MTY0NzYzNzE2MywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiJpYW1yb21pbG9uaW5AZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.cmzegGs7BtxkqDCYfZKoAP-23TNzFrLtH21biN1O1Oc";
                headers.put("Authorization", auth);
                Log.d("headers",headers.toString());


                return headers;
            }

        };
        requestQueue.add(stringRequest);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;


        LatLng latLng = new LatLng(7.144135261241745,125.65359487465777);

        Log.d("onmap","7.144135261241745");

        Log.d("Lat:",latLng.toString());


        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }

}

