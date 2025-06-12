package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travenor.Models.DataBinding;

import java.io.IOException;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPassEditText;
    private Button resetBtn;
    private SupabaseClient supabaseClient;
    private String email, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding.init(getApplicationContext());
        setContentView(R.layout.set_password);

        DataBinding.init(this);
        initializeViews();

        supabaseClient = new SupabaseClient();
        email = getIntent().getStringExtra("email");
        token = getIntent().getStringExtra("token");

        if (email == null || token == null) {
            showToastAndFinish("Ошибка: данные восстановления отсутствуют");
            return;
        }
    }

    private void initializeViews() {
        newPasswordEditText = findViewById(R.id.pasRes);
        confirmPassEditText = findViewById(R.id.confPasRes);
        resetBtn = findViewById(R.id.ResBTN);

        setupResetButton();
    }

    private void setupResetButton() {
        resetBtn.setOnClickListener(v -> {
            String password = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPassEditText.getText().toString().trim();

            if (!validatePasswords(password, confirmPassword)) {
                return;
            }

            supabaseClient.updatePassword(password, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ResetPasswordActivity.this, "Ошибка изменения пароля: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, "Пароль успешно изменён", Toast.LENGTH_SHORT).show();
                        navigateToLoginScreen();
                    });
                }
            });
        });
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToOtpScreen(String message, String email) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OtpScreen.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginScreen() {
        Toast.makeText(this, "Пароль успешно изменён", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}