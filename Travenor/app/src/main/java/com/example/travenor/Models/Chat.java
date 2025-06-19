package com.example.travenor.Models;

import com.google.gson.annotations.SerializedName;

public class Chat {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("manager_id")
    private int manager_id;

    @SerializedName("hotel_id")
    private int hotel_id;

    @SerializedName("updated_at")
    private String updated_at;

    @SerializedName("managers")
    private Manager manager;

    public Profile getMyProfile() {
        return myProfile;
    }

    public void setMyProfile(Profile myProfile) {
        this.myProfile = myProfile;
    }

    @SerializedName("user")
    private Profile userProfile;

    @SerializedName("profiles")
    private Profile myProfile;

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Profile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Profile userProfile) {
        this.userProfile = userProfile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public int getManager_id() { return manager_id; }
    public void setManager_id(int manager_id) { this.manager_id = manager_id; }

    public int getHotel_id() { return hotel_id; }
    public void setHotel_id(int hotel_id) { this.hotel_id = hotel_id; }
}