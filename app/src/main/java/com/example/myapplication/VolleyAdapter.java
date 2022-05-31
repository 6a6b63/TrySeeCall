package com.example.myapplication;

public class VolleyAdapter {

    private String token;
    private int id;
    private String email;
    private double latitude;
    private double longtitude;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public VolleyAdapter(String token, int id, String email, double latitude, double longtitude) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

}
