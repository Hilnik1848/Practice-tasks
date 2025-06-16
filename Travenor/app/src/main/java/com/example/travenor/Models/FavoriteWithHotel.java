package com.example.travenor.Models;

import com.google.gson.annotations.SerializedName;

public class FavoriteWithHotel {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("hotel_id")
    private String hotelId;

    @SerializedName("Hotels")
    private Hotel hotel;

    public String getUserId() {
        return userId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public Hotel getHotel() {
        return hotel;
    }
}