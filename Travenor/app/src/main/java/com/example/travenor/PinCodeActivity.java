package com.example.travenor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travenor.Models.AuthResponse;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.google.gson.Gson;
import java.io.IOException;

public class PinCodeActivity extends AppCompatActivity {
    private TextView pinDisplay;
    private StringBuilder pinBuilder = new StringBuilder();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code);

        pinDisplay = findViewById(R.id.pinDisplay);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isPinSet() || !sessionManager.canAutoLogin()) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        setButtonListeners();
    }

    private void setButtonListeners() {
        View.OnClickListener digitListener = v -> {
            String digit = ((Button) v).getText().toString();
            addDigit(digit);
        };

        findViewById(R.id.btn1).setOnClickListener(digitListener);
        findViewById(R.id.btn2).setOnClickListener(digitListener);
        findViewById(R.id.btn3).setOnClickListener(digitListener);
        findViewById(R.id.btn4).setOnClickListener(digitListener);
        findViewById(R.id.btn5).setOnClickListener(digitListener);
        findViewById(R.id.btn6).setOnClickListener(digitListener);
        findViewById(R.id.btn7).setOnClickListener(digitListener);
        findViewById(R.id.btn8).setOnClickListener(digitListener);
        findViewById(R.id.btn9).setOnClickListener(digitListener);
        findViewById(R.id.btn0).setOnClickListener(digitListener);

        findViewById(R.id.btnClear).setOnClickListener(v -> deleteDigit());
    }



    private void addDigit(String digit) {
        if (pinBuilder.length() < 4) {
            pinBuilder.append(digit);
            updatePinDisplay();
            if (pinBuilder.length() == 4) {
                checkAndSubmitPin();
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

    private void checkAndSubmitPin() {
        String enteredPin = pinBuilder.toString();
        String savedPin = sessionManager.getPin();

        if (savedPin != null && savedPin.equals(enteredPin)) {
            showSuccessAnimationAndProceed();
        } else {
            showErrorAnimation(() -> {
                Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                pinBuilder.setLength(0);
                updatePinDisplay();
            });
        }
    }

    private void showSuccessAnimationAndProceed() {
        pinDisplay.setTextColor(Color.GREEN);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            autoLogin();
        }, 500);
    }

    private void showErrorAnimation(Runnable onEndAction) {
        pinDisplay.setTextColor(Color.RED);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        findViewById(R.id.pinDisplay).startAnimation(shake);
        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                pinDisplay.setTextColor(Color.BLACK);
                if (onEndAction != null) onEndAction.run();
            }
        });
    }

    private void autoLogin() {
        String email = sessionManager.getEmail();
        String password = sessionManager.getPassword();

        if (email == null || password == null) {
            Toast.makeText(this, "Ошибка: нет данных для входа", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(PinCodeActivity.this, "Ошибка автоматического входа", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PinCodeActivity.this, Login.class));
                    finish();
                });
            }

            @Override
            public void onResponse(String responseBody) {
                runOnUiThread(() -> {
                    Gson gson = new Gson();
                    AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);
                    if (auth == null || auth.getAccess_token() == null) {
                        Toast.makeText(PinCodeActivity.this, "Не удалось получить токен", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(PinCodeActivity.this, Login.class));
                        finish();
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

                    startActivity(new Intent(PinCodeActivity.this, MainActivity.class));
                    finish();
                });
            }
        });
    }
}