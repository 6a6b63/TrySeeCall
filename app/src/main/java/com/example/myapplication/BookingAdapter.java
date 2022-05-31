package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder>{

    private Context mctx;
    private List<Booking> bookingList;
    String tok;
    DBHelper DB;


    public BookingAdapter(Context mctx, List<Booking> bookingList) {
        this.mctx = mctx;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mctx);
        View view = inflater.inflate(R.layout.list_layout,null);



        return new BookingViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder,int position) {

        Booking booking = bookingList.get(position);

        holder.lat.setVisibility(View.INVISIBLE);
        holder.lng.setVisibility(View.INVISIBLE);
        holder.id.setVisibility(View.INVISIBLE);
        holder.id_id.setVisibility(View.INVISIBLE);


        holder.id_id.setText(String.valueOf(booking.getBooking_id()));

        holder.id.setText(String.valueOf(booking.getPassenger_id()));
        holder.info.setText("Booking #"+ booking.getBooking_id());
        holder.note.setText(String.valueOf("Number of Passengers: "+booking.getNote()));
        holder.lat.setText(String.valueOf(booking.getLatitude()));
        holder.lng.setText(String.valueOf(booking.getLongtitude()));


        String latlng = booking.getLongtitude() + " , " + booking.getLatitude();


        holder.book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Toast.makeText(mctx, latlng, Toast.LENGTH_SHORT).show();
                String url = "https://tryseecall.davao.dev/api/v2/booking/accept";

                RequestQueue queue = Volley.newRequestQueue(mctx);


                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("response",response);

                                String id = String.valueOf(booking.getBooking_id());
                                String x = String.valueOf(booking.getLatitude());
                                String y = String.valueOf(booking.getLongtitude());

                                Log.d("xxxxxx",x);
                                Log.d("yyyyyy",y);
                                Log.d("iiiddd",id);

                                Intent intent = new Intent(mctx,EntryDriverActivity.class);
                                intent.putExtra("id",id);
                                intent.putExtra("x",x);
                                intent.putExtra("y",y);
                                intent.putExtra("token",tok);
                                mctx.startActivity(intent);
                                ((BookingActivity)mctx).finish();



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


                        map.put("booking_id", holder.id_id.getText().toString());


                        return map;
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> headers = new HashMap<>();

                        DB = new DBHelper(mctx);
//                        Intent intent = mctx.getIntent();
//                        Intent intent = new getIntent();
//                        tok = intent.getStringExtra("token");
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
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int pos = holder.getBindingAdapterPosition();
                bookingList.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        holder.locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mctx,AdminActivity.class);

                String x = String.valueOf(booking.getLatitude());
                String y = String.valueOf(booking.getLongtitude());

                intent.putExtra("x",x);
                intent.putExtra("y",y);
                mctx.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView lat,lng,id,note,id_id,info;
        Button book;
        ImageView cancel,locate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            info = itemView.findViewById(R.id.tv_info);
            id_id = itemView.findViewById(R.id.tv_passenger_id_id);
            id = itemView.findViewById(R.id.tv_passenger_id);
            note = itemView.findViewById(R.id.tv_passenger_note);
            lat = itemView.findViewById(R.id.textview_latitude);
            lng = itemView.findViewById(R.id.textview_longtitude);


            book = itemView.findViewById(R.id.button_book);
            cancel = itemView.findViewById(R.id.button_cancel);
            locate = itemView.findViewById(R.id.button_locate);

        }
    }

}
