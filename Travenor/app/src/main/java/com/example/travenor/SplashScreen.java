package com.example.travenor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travenor.Models.DataBinding;

public class SplashScreen extends AppCompatActivity {
    private final String MY_SETTINGS = "prefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding.init(getApplicationContext());
        setContentView(R.layout.splash);

        new Handler().postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(SplashScreen.this);
            SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

            Intent intent;

            if (!prefs.contains("firstStart")) {
                intent = new Intent(SplashScreen.this, OnboardingActivity.class);
            } else if (sessionManager.isLoggedIn()) {
                if (sessionManager.isPinSet()) {
                    intent = new Intent(SplashScreen.this, PinCodeActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, SetPinActivity.class);
                }
            } else {
                intent = new Intent(SplashScreen.this, Login.class);
            }

            startActivity(intent);
            finish();
        }, 3000);
    }
}