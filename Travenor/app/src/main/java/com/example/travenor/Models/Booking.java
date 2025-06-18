package com.example.travenor.Models;


import com.google.gson.annotations.SerializedName;

public class Booking {
    private String id;
    private String user_id;
    private String check_in_date;
    private String check_out_date;
    private int guests_count;
    private int room_id;
    private int hotel_id;
    private Double summ;

    public Double getSumm() {
        return summ;
    }

    public void setSumm(Double summ) {
        this.summ = summ;
    }

    @SerializedName("hotel_room")
    private HotelRoom hotelRoom;
    public Hotel getHotel() {
        return hotelRoom != null ? hotelRoom.getHotel() : null;
    }
    public String getId() { return id; }
    public String getUserId() { return user_id; }
    public String getCheckInDate() { return check_in_date; }
    public String getCheckOutDate() { return check_out_date; }
    public int getGuestsCount() { return guests_count; }
    public int getRoomId() { return room_id; }
    public int getHotelId() { return hotel_id; }
    public HotelRoom getHotelRoom() { return hotelRoom; }
}