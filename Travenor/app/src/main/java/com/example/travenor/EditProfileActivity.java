package com.example.travenor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .commit();
        }
    }
}