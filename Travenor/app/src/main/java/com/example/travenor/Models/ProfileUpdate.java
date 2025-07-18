package com.example.travenor.Models;

public class ProfileUpdate {
    private String full_name;
    private String avatar_url;

    public ProfileUpdate(String full_name) {
        this.full_name = full_name;
        this.avatar_url = null;
    }

    public ProfileUpdate() {
        this.full_name = null;
        this.avatar_url = null;
    }

    public ProfileUpdate(String full_name, String avatar_url) {
        this.full_name = full_name;
        this.avatar_url = avatar_url;
    }


    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getAvatar_url() {
        return avatar_url;
    }
}