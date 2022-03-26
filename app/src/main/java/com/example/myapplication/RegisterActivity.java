package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {

    EditText mEmail,mFirstname,mLastname,mPassword,mCpassword;
    Button register;
    TextView mLogin;
    RadioGroup radioGroup;
    RadioButton radioButton;
    String Checkvalue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mEmail = findViewById(R.id.edt_email);
        mFirstname = findViewById(R.id.edt_fname);
        mLastname = findViewById(R.id.edt_lastname);
        mPassword = findViewById(R.id.edt_pass);
        mCpassword = findViewById(R.id.edt_cpass);
        register = findViewById(R.id.btn_insert);
        mLogin = findViewById(R.id.backLogin);

        radioGroup = findViewById(R.id.radiogroup);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.passenger:
                        Checkvalue = "6";
                        break;
                    case R.id.driver:
                        Checkvalue = "5";
                        break;
                }
                // do something with value
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String gEmail = mEmail.getText().toString();
                String gFirstname = mFirstname.getText().toString();
                String gLastname = mLastname.getText().toString();
                String gPassword = mPassword.getText().toString();
                String gCpassword = mCpassword.getText().toString();

                Log.d("email",gEmail);
                insertUser(gEmail,gFirstname,gLastname,gPassword,gCpassword);

            }
        });
    }
    private void insertUser(String email,String fname,String lname,String pass,String cpass){

//        String url ="https://tryseecall.davao.dev/api/v2/register";
        String url ="http://192.168.1.3/api/v2/register";

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


                if(jsonObject.get("error").equals(422)){
                    Toast.makeText(RegisterActivity.this,"Empty Fields",Toast.LENGTH_SHORT).show();
                }


            }
        }){
            protected HashMap<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();

                Log.d("email",email);
                map.put("email",email);
                map.put("firstname",fname);
                map.put("lastname",lname);
                map.put("password",pass);
                map.put("confirm_password",cpass);


                return map;
            }
        };

        queue.add(stringRequest);
    }

}