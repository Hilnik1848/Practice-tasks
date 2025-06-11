package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;

public class OtpScreen extends AppCompatActivity {

    private PinView pinView;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        pinView = findViewById(R.id.pinview);
        email = getIntent().getStringExtra("email");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Ошибка: Email не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.show_otp).setOnClickListener(v -> {
            String token = pinView.getText().toString().trim();
            if (token.length() != 6) {
                Toast.makeText(this, "Введите корректный 6-значный код", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OtpScreen.this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("token", token);
            startActivity(intent);
            finish();
        });
    }
}