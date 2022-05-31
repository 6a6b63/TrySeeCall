package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    
    EditText username,password;
    Button login;
    TextView register,driver;
    ProgressBar progressBar;
    VolleyError volleyError;
    DBHelper DB;
    String tok,getUser,getPass;

   public static final String EXTRA_TEXT = "com.example.apcplication.example.EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
        driver = findViewById(R.id.btn_driver);

         getUser = username.getText().toString();
         getPass = password.getText().toString();


        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),DriverActivity.class);
                startActivity(intent);

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               loginUser();
               //finish();

////////////////////////////////////
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }////endofoncreate
    public void loginUser(){
        //String url = "http://192.168.1.3/api/v2/auth";
        String url = "https://tryseecall.davao.dev/api/v2/auth";

        DB = new DBHelper(this);

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response);
//                        Type listType = new TypeToken<List<String>>() {}.getType();

                        Gson g = new Gson();
                        String token = null;
//                        String json = g.toJson(response, listType);
                        JsonObject jsonObject = g.fromJson(response,JsonObject.class);

                        Log.d("log",jsonObject.get("success").toString());


                        if(jsonObject.get("success").toString().equals("false")){
                            Toast.makeText(LoginActivity.this,"Invalid Username/Password",Toast.LENGTH_SHORT).show();

                        }if(jsonObject.get("success").toString().equals("true")){
                            JsonObject jsonData = g.fromJson(jsonObject.get("data"),JsonObject.class);
                            Log.d("credentials",jsonObject.get("data").toString());
                            token = jsonData.get("token").toString().replaceAll("\"", "");
                            DB.savetoken(token);
                            Log.d("token",token);


                            screenRouter();
//                            Intent intent = new Intent(getApplicationContext(), TrySeeCallActivity.class);
//                            intent.putExtra(EXTRA_TEXT,token);
//                            startActivity(intent);
                        }



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

                if(jsonObject.get("success").toString().equals("false")){
                    Toast.makeText(LoginActivity.this,"Invalid Username/Password",Toast.LENGTH_SHORT).show();
                }

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

                getUser = username.getText().toString();
                getPass = password.getText().toString();

                Log.d("headers",headers.toString());
//                String credentials = username.getText().toString()+":"+ password.getText().toString();
                //String credentials = "kaka@gmail.com" +":"+ "1234";
                String credentials = getUser +":"+ getPass;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
               //String auth = "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0cnlzZWVjYWxsLmRhdmFvLmRldiIsImF1ZCI6InRyeXNlZWNhbGwuZGF2YW8uZGV2IiwiaWF0IjoxNjQ3Mzc3NDE3LCJuYmYiOjE2NDczNzc0MTcsImV4cCI6MTY0NzQ2MzgxNywiYWxncyI6IkhTMjU2IiwidXNlcm5hbWUiOiI2YTZiNjNAZ21haWwuY29tIiwicGFzc3dvcmQiOiIxMjM0In0.gfKFXyK3tPs17D3DQ9oiSWBHsO14qtgDILoOJ90-qUE";
                headers.put("Authorization", auth);
                Log.d("headers",headers.toString());



                return headers;
            }

        };
        queue.add(stringRequest);

    }

    public void checkingPermission(){
//        JsonObject data = getInfo();



    }

    public void screenRouter(){
        //String url = "http://192.168.1.3/api/v2/users/me";
        String url = "https://tryseecall.davao.dev/api/v2/users/me";

        DB = new DBHelper(this);

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    JsonObject jsonData;
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);

                        Gson g = new Gson();
                        JsonObject jsonObject = g.fromJson(response,JsonObject.class);

                        jsonData = g.fromJson(jsonObject.get("data"),JsonObject.class);

//
                        jsonObject.get("data").toString();
//                        user_id = jsonData.get("id").toString().replaceAll("\"", "");
                       String user_level = jsonData.get("user_level_id").toString().replaceAll("\"", "");
                       String status = jsonData.get("status").toString().replaceAll("\"", "");
//                        Log.d("userID:", user_id);
                          Log.d("status", status);


                        if(status.equals("0")){
                            Toast.makeText(LoginActivity.this,"YOU NEED TO VERIFY THIS ACCOUNT!",Toast.LENGTH_SHORT).show();
                        }else if(user_level.equals("5")){
                            Intent intent = new Intent(LoginActivity.this,DriverActivity.class);
                            startActivity(intent);
                        }else if(user_level.equals("6")){
                            Intent intent = new Intent(LoginActivity.this,TrySeeCallActivity.class);
                            startActivity(intent);
                        }else if(user_level.equals("1")){
                            Intent intent = new Intent(LoginActivity.this,AdminActivity.class);
                            startActivity(intent);
                        }



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
}