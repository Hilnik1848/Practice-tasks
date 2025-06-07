package com.example.travenor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travenor.Models.AuthResponse;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.example.travenor.Models.ProfileUpdate;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup);

        DataBinding.init(getApplicationContext());

        initViews();
        setupSignUpButton();
        setupSignInRedirect();

    }


    private void initViews() {
        nameLayout = findViewById(R.id.nameTextLayout);
        nameEditText = findViewById(R.id.nameEditText);

        emailLayout = findViewById(R.id.emailTextLayout);
        emailEditText = findViewById(R.id.emailEditText);

        passwordLayout = findViewById(R.id.passwordTextLayout);
        passwordEditText = findViewById(R.id.passwordEditText);

        confirmPasswordLayout = findViewById(R.id.confirmPasswordTextLayout);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (!isValidEmail(email)) return;
            if (!isValidPassword(password)) return;
            if (!doPasswordsMatch(password, confirmPassword)) return;
            if (!validateShortText(nameEditText, nameLayout)) return;

            SupabaseClient supabaseClient = new SupabaseClient();
            LoginRequest loginRequest = new LoginRequest(email, password);

            supabaseClient.registr(loginRequest, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        Log.d("registr:onResponse", responseBody);

                        Gson gson = new Gson();
                        AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                        if (auth == null || auth.getAccess_token() == null) {
                            Toast.makeText(LoginActivity.this, "Не удалось получить токен", Toast.LENGTH_LONG).show();
                            return;
                        }

                        DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
                        DataBinding.saveUuidUser(auth.getUser().getId());

                        ProfileUpdate profileUpdate = new ProfileUpdate(name, "profile1.png");
                        supabaseClient.updateProfile(profileUpdate, new SupabaseClient.SBC_Callback() {
                            @Override
                            public void onFailure(IOException e) {
                                runOnUiThread(() -> {
                                    Log.e("updateProfile:onFailure", e.getLocalizedMessage());
                                    Toast.makeText(LoginActivity.this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onResponse(String response) {
                                runOnUiThread(() -> {
                                    Log.d("updateProfile:onResponse", response);
                                    Toast.makeText(LoginActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                });
                            }
                        });
                    });
                }
            });
        });
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Введите email");
            return false;
        }

        // Паттерн: name@domen.ru → name и domen состоят только из [a-z0-9]
        String pattern = "^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(email);

        if (!matcher.matches()) {
            emailLayout.setError("Неверный формат email");
            return false;
        }

        emailLayout.setError(null);
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password.isEmpty()) {
            passwordLayout.setError("Введите пароль");
            return false;
        } else if (password.length() < 6 || password.length() > 8) {
            passwordLayout.setError("Пароль должен быть от 6 до 8 символов");
            return false;
        }

        passwordLayout.setError(null);
        return true;
    }

    private boolean doPasswordsMatch(String pass, String confirmPass) {
        if (!pass.equals(confirmPass)) {
            confirmPasswordLayout.setError("Пароли не совпадают");
            return false;
        }
        confirmPasswordLayout.setError(null);
        return true;
    }


    public boolean validateShortText(EditText editText, TextInputLayout layout) {
        String input = editText.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            layout.setError("Это поле обязательное");
            return false;
        } else if (input.length() > 20) {
            layout.setError("Не более 20 символов");
            return false;
        }

        layout.setError(null);
        return true;
    }

    private void setupSignInRedirect() {
        findViewById(R.id.signInButton).setOnClickListener(v ->
                startActivity(new Intent(this, Login.class))
        );
    }


}