package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.example.travenor.Models.AuthResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    TextInputLayout passwordLayout , emailLayout;
    Button loginBtn;
    TextView signUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        DataBinding.init(getApplicationContext());

        initViews();
        setupTextWatchers();
        setupLoginButton();
        setupSignUpRedirect();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        emailLayout = findViewById(R.id.emailTextLayout);
        passwordEditText = findViewById(R.id.password);
        passwordLayout = findViewById(R.id.passwordLayout);
        loginBtn = findViewById(R.id.logInBtn);
        signUpText = findViewById(R.id.signup_button);
    }

    private void setupTextWatchers() {
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateEmail();
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword();
            }
        });
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Введите email");
            return false;
        }
        String pattern = "^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,}$";
        Pattern regex = Pattern.compile(pattern);
        if (!regex.matcher(email).matches()) {
            emailLayout.setError("Неверный формат email");
            return false;
        }
        emailLayout.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Введите пароль");
            return false;
        } else if (password.length() < 6 || password.length() > 8) {
            passwordLayout.setError("Пароль должен быть от 6 до 8 символов");
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }

    private void setupLoginButton() {
        loginBtn.setOnClickListener(v -> {
            if (validateEmail() && validatePassword()) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                performLogin(email, password);
            }
        });
    }

    private void performLogin(String email, String password) {
        SupabaseClient supabaseClient = new SupabaseClient();
        LoginRequest request = new LoginRequest(email, password);

        supabaseClient.login(request, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(Login.this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(String responseBody) {
                runOnUiThread(() -> {
                    Toast.makeText(Login.this, "Вход выполнен", Toast.LENGTH_SHORT).show();

                    Gson gson = new Gson();
                    AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                    if (auth == null || auth.getAccess_token() == null) {
                        Toast.makeText(Login.this, "Ошибка: не удалось получить токен", Toast.LENGTH_LONG).show();
                        return;
                    }

                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                });
            }
        });
    }

    private void setupSignUpRedirect() {
        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}