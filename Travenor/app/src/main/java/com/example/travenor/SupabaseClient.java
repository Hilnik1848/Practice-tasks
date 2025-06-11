package com.example.travenor;

import static android.util.Patterns.DOMAIN_NAME;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.example.travenor.Models.ProfileUpdate;
import com.example.travenor.Models.RecoveryRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    public static String DOMAIN_NAME = "https://mmbdesfnabtcbpjwcwde.supabase.co/";
    public static String REST_PATH = "rest/v1/";
    public static String AUTH_PATH = "auth/v1/";
    public static String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk";

    OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json");
    public void registr(LoginRequest loginRequest, final SBC_Callback callback){
        MediaType mediaType = MediaType.parse("application/json");
        Gson gson = new Gson();
        String json = gson.toJson(loginRequest);
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "signup")
                .method("POST", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера: " + response));
                }
            }
        });
    }

    public void updateProfile(ProfileUpdate profile, final SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + DataBinding.getUuidUser())
                .method("PATCH", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера: " + response.code()));
                }
            }
        });
    }


    public void login(LoginRequest loginRequest, SBC_Callback callback) {
        MediaType mediaType = MediaType.get("application/json");
        String json = new Gson().toJson(loginRequest);
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "token?grant_type=password")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    callback.onResponse(body);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера: " + response.code()));
                }
            }
        });
    }

    public void sendRecoveryEmail(String email, final SBC_Callback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
        } catch (Exception e) {
            callback.onFailure(new IOException("Ошибка формирования данных: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "recover")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Код отправлен на ваш email");
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Неизвестная ошибка";
                    callback.onFailure(new IOException("Ошибка: " + errorBody));
                }
            }
        });
    }

    public void verifyOtp(String email, String token, final SBC_Callback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("token", token);
            payload.put("type", "recovery");
        } catch (Exception e) {
            callback.onFailure(new IOException("Ошибка формирования данных: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "verify")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String accessToken = json.getString("access_token");

                        DataBinding.saveBearerToken(accessToken);
                        callback.onResponse(accessToken);
                    } catch (Exception e) {
                        callback.onFailure(new IOException("Ошибка обработки ответа: " + e.getMessage()));
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Неизвестная ошибка";
                    try {
                        JSONObject errorJson = new JSONObject(errorBody);
                        String errorCode = errorJson.optString("error_code", "");
                        String errorMsg = errorJson.optString("msg", "Ошибка сервера");

                        if ("otp_expired".equals(errorCode)) {
                            callback.onFailure(new IOException("OTP истёк"));
                        } else if ("invalid_token".equals(errorCode)) {
                            callback.onFailure(new IOException("Неверный токен"));
                        } else {
                            callback.onFailure(new IOException("Ошибка: " + errorMsg));
                        }
                    } catch (Exception e) {
                        callback.onFailure(new IOException("Ошибка сервера: " + errorBody));
                    }
                }
            }
        });
    }

    public void updatePassword(String newPassword, final SBC_Callback callback) {
        String accessToken = DataBinding.getBearerToken();
        if (accessToken == null) {
            callback.onFailure(new IOException("Требуется авторизация"));
            return;
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("password", newPassword);
        } catch (Exception e) {
            callback.onFailure(new IOException("Ошибка формирования данных: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "user")
                .put(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Пароль успешно изменён");
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Неизвестная ошибка";
                    callback.onFailure(new IOException("Ошибка при смене пароля: " + errorBody));
                }
            }
        });
    }
    public interface SBC_Callback {
        void onFailure(IOException e);
        void onResponse(String responseBody);
    }
}
