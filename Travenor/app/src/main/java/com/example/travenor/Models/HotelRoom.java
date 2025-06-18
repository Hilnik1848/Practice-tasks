package com.example.travenor.Models;

import com.google.gson.annotations.SerializedName;

public class HotelRoom {
    private int id;

    @SerializedName("room_type")
    private String room_type;

    @SerializedName("price_per_night")
    private double price_per_night;

    @SerializedName("hostel_id")
    private int hostelId;
    private Hotel Hotels;

    public Hotel getHotel() {
        return Hotels;
    }
    public String getRoom_type() {
        return room_type;
    }

    public double getPrice_per_night() {
        return price_per_night;
    }

    public int getId() {
        return id;
    }

    public int getHostelId() {
        return hostelId;
    }

}