package com.smartparking.amit.parksmart;

import java.util.ArrayList;

public class MyLocation {
    private double Latitude,Longitude;
    private String Help;
    private ArrayList<Slots> Slots = new ArrayList<>();

    public MyLocation() {
    }

    public MyLocation(double Latitude, double Longitude, String Help, ArrayList<Slots> slots) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Help = Help;
        this.Slots = slots;
    }

    public double getLatitude() {
        return Latitude;
    }

   public String getHelp() {
        return Help;
    }

    public double getLongitude() {
        return Longitude;
    }
}
