package com.example.travenor;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travenor.Models.AuthResponse;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.google.gson.Gson;

import java.io.IOException;

public class SetPinActivity extends AppCompatActivity {
    private TextView pinDisplay;
    private StringBuilder pinBuilder = new StringBuilder();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding.init(getApplicationContext());
        setContentView(R.layout.activity_set_pin);

        pinDisplay = findViewById(R.id.pinDisplay);
        sessionManager = new SessionManager(this);

        ImageButton forgotBackButton = findViewById(R.id.forgotBack);
        forgotBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(SetPinActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        setButtonListeners();
    }

    private void setButtonListeners() {
        Button[] numberButtons = {
                findViewById(R.id.btn1), findViewById(R.id.btn2), findViewById(R.id.btn3),
                findViewById(R.id.btn4), findViewById(R.id.btn5), findViewById(R.id.btn6),
                findViewById(R.id.btn7), findViewById(R.id.btn8), findViewById(R.id.btn9),
                findViewById(R.id.btn0)
        };

        for (Button button : numberButtons) {
            button.setOnClickListener(v -> {
                String digit = ((Button) v).getText().toString();
                addDigit(digit);
            });
        }

        findViewById(R.id.btnClear).setOnClickListener(v -> deleteDigit());
    }

    private void addDigit(String digit) {
        if (pinBuilder.length() < 4) {
            pinBuilder.append(digit);
            updatePinDisplay();

            if (pinBuilder.length() == 4) {
                String email = sessionManager.getEmail();
                String password = sessionManager.getPassword();

                if (email == null || password == null) {
                    Toast.makeText(this, "Ошибка: нет данных для входа", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Login.class));
                    finish();
                    return;
                }

                SupabaseClient supabaseClient = new SupabaseClient();
                LoginRequest request = new LoginRequest(email, password);

                supabaseClient.login(request, new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(SetPinActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                            pinBuilder.setLength(0);
                            updatePinDisplay();
                        });
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        runOnUiThread(() -> {
                            Gson gson = new Gson();
                            AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                            if (auth == null || auth.getAccess_token() == null) {
                                Toast.makeText(SetPinActivity.this, "Не удалось получить токен", Toast.LENGTH_LONG).show();
                                return;
                            }

                            DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
                            DataBinding.saveUuidUser(auth.getUser().getId());

                            sessionManager.createLoginSession(
                                    email,
                                    password,
                                    auth.getAccess_token(),
                                    auth.getUser().getId()
                            );


                            sessionManager.savePin(pinBuilder.toString());

                            Intent intent = new Intent(SetPinActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }
        }
    }

    private void deleteDigit() {
        if (pinBuilder.length() > 0) {
            pinBuilder.deleteCharAt(pinBuilder.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        pinDisplay.setText("•".repeat(pinBuilder.length()));
    }
}