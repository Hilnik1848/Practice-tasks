package com.example.travenor.Models;

public class ScheduleItem {
    private String imageUrl;
    private String checkInDate;
    private String checkOutDate;
    private String destinationName;
    private String location;

    public ScheduleItem(String imageUrl, String checkInDate, String checkOutDate, String destinationName, String location) {
        this.imageUrl = imageUrl;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.destinationName = destinationName;
        this.location = location;
    }

    public String getImageUrl() { return imageUrl; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getDestinationName() { return destinationName; }
    public String getLocation() { return location; }
}
