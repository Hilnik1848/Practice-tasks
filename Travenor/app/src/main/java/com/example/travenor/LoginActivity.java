package com.example.travenor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
        setupTextWatchers();
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

    private void setupTextWatchers() {
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateShortText(nameEditText, nameLayout);
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                isValidEmail(emailEditText.getText().toString().trim());
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                isValidPassword(passwordEditText.getText().toString().trim());
                doPasswordsMatch(
                        passwordEditText.getText().toString().trim(),
                        confirmPasswordEditText.getText().toString().trim()
                );
            }
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                doPasswordsMatch(
                        passwordEditText.getText().toString().trim(),
                        confirmPasswordEditText.getText().toString().trim()
                );
            }
        });
    }

    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            boolean isNameValid = validateShortText(nameEditText, nameLayout);
            boolean isEmailValid = isValidEmail(email);
            boolean isPasswordValid = isValidPassword(password);
            boolean isConfirmValid = doPasswordsMatch(password, confirmPassword);

            if (isNameValid && isEmailValid && isPasswordValid && isConfirmValid) {
                attemptRegistration(name, email, password);
            }
        });
    }

    private void attemptRegistration(String name, String email, String password) {
        SupabaseClient supabaseClient = new SupabaseClient();
        LoginRequest loginRequest = new LoginRequest(email, password);

        supabaseClient.registr(loginRequest, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(String responseBody) {
                runOnUiThread(() -> handleRegistrationResponse(name, email, password, responseBody));
            }
        });
    }

    private void handleRegistrationResponse(String name, String email, String password, String responseBody) {
        try {
            Log.d("Registration", "Response: " + responseBody);
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();

            Gson gson = new Gson();
            AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

            if (auth == null || auth.getAccess_token() == null) {
                Toast.makeText(this, "Ошибка: неверный ответ сервера", Toast.LENGTH_LONG).show();
                return;
            }

            DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
            DataBinding.saveUuidUser(auth.getUser().getId());

            updateProfileAfterRegistration(name, email, password, auth);
        } catch (Exception e) {
            Log.e("Registration", "Error parsing response", e);
            Toast.makeText(this, "Ошибка обработки ответа", Toast.LENGTH_LONG).show();
        }
    }

    private void updateProfileAfterRegistration(String name, String email, String password, AuthResponse auth) {
        ProfileUpdate profileUpdate = new ProfileUpdate(name, "profile1.png");
        new SupabaseClient().updateProfile(profileUpdate, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    Log.e("ProfileUpdate", "Error", e);
                    Toast.makeText(LoginActivity.this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                    completeRegistration(email, password, auth);
                });
            }

            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    Log.d("ProfileUpdate", "Success");
                    Toast.makeText(LoginActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                    completeRegistration(email, password, auth);
                });
            }
        });
    }

    private void completeRegistration(String email, String password, AuthResponse auth) {
        SessionManager sessionManager = new SessionManager(LoginActivity.this);
        sessionManager.createLoginSession(
                email,
                password,
                auth.getAccess_token(),
                auth.getUser().getId()
        );

        SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPref.edit().putString("auth_status", "Authorized").apply();

        if (!sessionManager.isPinSet()) {
            startActivity(new Intent(LoginActivity.this, SetPinActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        finish();
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Введите email");
            return false;
        }

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

    private boolean doPasswordsMatch(String pass, String confirmPass) {
        if (TextUtils.isEmpty(confirmPass)) {
            confirmPasswordLayout.setError("Подтвердите пароль");
            return false;
        } else if (!pass.equals(confirmPass)) {
            confirmPasswordLayout.setError("Пароли не совпадают");
            return false;
        }

        confirmPasswordLayout.setError(null);
        return true;
    }

    private boolean validateShortText(EditText editText, TextInputLayout layout) {
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