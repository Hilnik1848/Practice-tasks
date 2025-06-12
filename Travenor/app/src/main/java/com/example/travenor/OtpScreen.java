package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.travenor.Models.DataBinding;

import java.io.IOException;

public class OtpScreen extends AppCompatActivity {
    private PinView pinView;
    private String email;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding.init(getApplicationContext());
        setContentView(R.layout.activity_otp);

        pinView = findViewById(R.id.pinview);
        Button showOtpButton = findViewById(R.id.show_otp);

        email = getIntent().getStringExtra("email");
        supabaseClient = new SupabaseClient();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Ошибка: Email не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showOtpButton.setOnClickListener(v -> {
            String token = pinView.getText().toString().trim();

            if (token.length() != 6) {
                Toast.makeText(this, "Введите корректный 6-значный код", Toast.LENGTH_SHORT).show();
                return;
            }

            supabaseClient.verifyOtp(email, token, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() -> {
                        String message = e.getMessage();

                        if (message.contains("OTP истёк")) {
                            Toast.makeText(OtpScreen.this, "Код истёк. Запросите новый.", Toast.LENGTH_LONG).show();
                        } else if (message.contains("Неверный токен")) {
                            Toast.makeText(OtpScreen.this, "Неверный код. Попробуйте снова.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(OtpScreen.this, "Ошибка: " + message, Toast.LENGTH_LONG).show();
                        }

                        pinView.setText("");
                    });
                }

                @Override
                public void onResponse(String accessToken) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(OtpScreen.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("token", token);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        });
    }
}