package com.example.travenor.Models;

public class BookingRequest {
    private String user_id;
    private String check_in_date;
    private String check_out_date;
    private int guests_count;
    private int room_id;
    private int hotel_id;
    private Double summ;

    public BookingRequest(String user_id, String check_in_date, String check_out_date,
                          int guests_count, int room_id, int hotel_id) {
        this.user_id = user_id;
        this.check_in_date = check_in_date;
        this.check_out_date = check_out_date;
        this.guests_count = guests_count;
        this.room_id = room_id;
        this.hotel_id = hotel_id;
    }

    public void setSumm(Double summ) {
        this.summ = summ;
    }


    public String getUser_id() { return user_id; }
    public String getCheck_in_date() { return check_in_date; }
    public String getCheck_out_date() { return check_out_date; }
    public int getGuests_count() { return guests_count; }
    public int getRoom_id() { return room_id; }
    public int getHotel_id() { return hotel_id; }
}