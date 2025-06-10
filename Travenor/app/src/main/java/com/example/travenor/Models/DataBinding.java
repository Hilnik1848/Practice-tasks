package com.example.travenor.Models;

import android.content.Context;
import android.content.SharedPreferences;

public class DataBinding {
    private static SharedPreferences sharedPreferences;
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (context == null) return;
        sharedPreferences = context.getSharedPreferences("auth_data", Context.MODE_PRIVATE);
        isInitialized = true;
    }

    public static void saveBearerToken(String token) {
        if (!isInitialized || sharedPreferences == null) return;
        sharedPreferences.edit().putString("bearer_token", token).apply();
    }

    public static String getBearerToken() {
        if (!isInitialized || sharedPreferences == null) return null;
        return sharedPreferences.getString("bearer_token", null);
    }

    public static void saveUuidUser(String uuid) {
        if (!isInitialized || sharedPreferences == null) return;
        sharedPreferences.edit().putString("user_uuid", uuid).apply();
    }

    public static String getUuidUser() {
        if (!isInitialized || sharedPreferences == null) return null;
        return sharedPreferences.getString("user_uuid", null);
    }
}