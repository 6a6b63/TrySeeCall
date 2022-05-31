package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingActivity extends AppCompatActivity {

    ProgressBar progressBar;

    RecyclerView recyclerView;
    BookingAdapter bookingAdapter;
    List<Booking> bookingList;

    DBHelper DB;
    boolean shutdown = false;
    private GoogleMap mMap;
    String tok;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        bookingList = new ArrayList<>();

        swipeRefreshLayout = findViewById(R.id.refreshLayout);



        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        tok = intent.getStringExtra("token");


        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }




        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                    swipeRefreshLayout.setRefreshing(false);
                    checkRecycler();
                    bookingList.clear();
                    getOpenBookings();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getOpenBookings();
    }


    public void checkRecycler(){
        if(bookingList.isEmpty()){
            Toast.makeText(BookingActivity.this,"NO PASSENGER BOOKED YET\nPLEASE WAIT....",Toast.LENGTH_SHORT).show();
        }
    }

    public void getOpenBookings(){
            // String url = "http://192.168.1.3/api/v2/booking/open";
            String url = "https://tryseecall.davao.dev/api/v2/booking/open";

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response",response);

                    bookingList.clear();

                    try {
                        JSONObject data = new JSONObject(response);
                        JSONArray bookings = data.getJSONArray("data");
                        for(int i = 0; i < bookings.length(); i++){
                            JSONObject booking = bookings.getJSONObject(i);


                                int booking_id = booking.getInt("id");
                                int passenger_id = booking.getInt("passenger_id");
                                String passenger_note = booking.getString("passenger_note");
                                double latitude = booking.getDouble("checkinlat");
                                double longitude = booking.getDouble("checkinlong");


                                Log.d("passenger_Idzzzzz",String.valueOf(passenger_id));


                                Log.d("latz", String.valueOf(latitude));
                                Log.d("lngz", String.valueOf(longitude));

                                Booking booking1 = new Booking(booking_id,passenger_id,passenger_note,latitude, longitude);


                                bookingList.add(booking1);


                        }


                        bookingAdapter = new BookingAdapter(BookingActivity.this,bookingList);
                        recyclerView.setAdapter(bookingAdapter);


                    } catch (JSONException e) {

                        e.printStackTrace();
                    }

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!shutdown) {
                                bookingList.clear();
                                getOpenBookings();

                            }
                        }
                    },15000);

                }
            },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error){
                    Log.d("error", String.valueOf(error));

                    //Toast.makeText(BookingActivity.this, error.toString(),Toast.LENGTH_SHORT).show();


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
                    //tok = DB.getToken();


                    String auth = "Bearer " + tok;
                    headers.put("Authorization", auth);
                    Log.d("headers",headers.toString());


                    return headers;
                }

            };
            requestQueue.add(stringRequest);
        }
    }