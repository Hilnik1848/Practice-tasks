package com.example.travenor;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetPinActivity extends AppCompatActivity {
    private TextView pinDisplay;
    private StringBuilder pinBuilder = new StringBuilder();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);
        pinDisplay = findViewById(R.id.pinDisplay);
        sessionManager = new SessionManager(this);

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
                sessionManager.savePin(pinBuilder.toString());
                Toast.makeText(this, "PIN установлен", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
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