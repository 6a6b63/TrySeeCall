package com.example.myapplication;

public class Location {

    private int id;
    private int passenger_id;
    private String check_in_location;

    public Location(int id, int passenger_id, String check_in_location) {
        this.id = id;
        this.passenger_id = passenger_id;
        this.check_in_location = check_in_location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(int passenger_id) {
        this.passenger_id = passenger_id;
    }

    public String getCheck_in_location() {
        return check_in_location;
    }

    public void setCheck_in_location(String check_in_location) {
        this.check_in_location = check_in_location;
    }
}
