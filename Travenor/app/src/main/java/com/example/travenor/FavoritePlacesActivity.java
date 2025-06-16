package com.example.travenor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FavoritePlacesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_favorite_places);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FavoritePlacesFragment())
                    .commit();
        }
    }
}