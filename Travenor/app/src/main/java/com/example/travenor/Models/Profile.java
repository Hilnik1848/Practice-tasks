package com.example.travenor.Models;

import com.google.gson.annotations.SerializedName;

public class Profile {
    @SerializedName("id")
    private String id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("avatar_url")
    private String avatarUrl;

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
}