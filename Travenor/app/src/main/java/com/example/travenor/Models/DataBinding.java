package com.example.travenor.Models;

import android.content.Context;
import android.content.SharedPreferences;

public class DataBinding {

    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("auth_data", Context.MODE_PRIVATE);
    }

    public static void saveBearerToken(String token) {
        sharedPreferences.edit().putString("bearer_token", token).apply();
    }


    public static String getBearerToken() {
        return sharedPreferences.getString("bearer_token", null);
    }


    public static void saveUuidUser(String uuid) {
        sharedPreferences.edit().putString("user_uuid", uuid).apply();
    }


    public static String getUuidUser() {
        return sharedPreferences.getString("user_uuid", null);
    }
}