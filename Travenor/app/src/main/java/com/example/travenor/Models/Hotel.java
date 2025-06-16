package com.example.travenor.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Hotel implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;


    @SerializedName("description")
    private String description;

    @SerializedName("star")
    private double rating;

    @SerializedName("hotel_url")
    private String imageUrl;


    protected Hotel(Parcel in) {
        id = in.readString();
        name = in.readString();
        addres = in.readString();
        description = in.readString();
        rating = in.readDouble();
        imageUrl = in.readString();
    }
    @SerializedName("addres")
    private String addres;

    public String getAddres() {
        return addres;
    }
    public static final Creator<Hotel> CREATOR = new Creator<Hotel>() {
        @Override
        public Hotel createFromParcel(Parcel in) {
            return new Hotel(in);
        }

        @Override
        public Hotel[] newArray(int size) {
            return new Hotel[size];
        }
    };

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void setLocation(String location) { this.addres = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(addres);
        dest.writeString(description);
        dest.writeDouble(rating);
        dest.writeString(imageUrl);
    }
}