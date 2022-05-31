package com.example.myapplication;

public class Booking {


    private int booking_id;
    private int passenger_id;
    private String passenger_note;
    private double latitude;
    private double longtitude;



    public Booking(int booking_id, int passenger_id, String passenger_note, double latitude, double longtitude) {

        this.booking_id = booking_id;
        this.passenger_id = passenger_id;
        this.passenger_note = passenger_note;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public int getBooking_id() {
        return booking_id;
    }

    public int getPassenger_id() {
        return passenger_id;
    }

    public String getNote() {
        return passenger_note;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }
}
