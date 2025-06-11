package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button nextButton;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        nextButton = findViewById(R.id.NextBTN);
        supabaseClient = new SupabaseClient();

        nextButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
                return;
            }

            supabaseClient.sendRecoveryEmail(email, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ForgotPasswordActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPasswordActivity.this, responseBody, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpScreen.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        });
    }
}