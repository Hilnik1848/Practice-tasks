package com.example.travenor;


import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.LoginRequest;
import com.example.travenor.Models.ProfileUpdate;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
                    assert response.body() != null;
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
                .addHeader("Authorization", Objects.requireNonNull(DataBinding.getBearerToken()))
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
                    assert response.body() != null;
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
                    assert response.body() != null;
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
                        assert response.body() != null;
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



    public void fetchUserProfile(SBC_Callback callback) {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new IOException("Пользователь не авторизован"));
            return;
        }

        String url = DOMAIN_NAME + REST_PATH + "profiles?id=eq." + userId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    callback.onResponse(body);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Неизвестная ошибка";
                    callback.onFailure(new IOException("Ошибка сервера: " + response.code() + ", " + errorBody));
                }
            }
        });
    }
    public void updateProfile(String userId, ProfileUpdate profileUpdate, SBC_Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        Gson gson = new Gson();
        String json = gson.toJson(profileUpdate);
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + userId)
                .patch(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("OK");
                } else {
                    callback.onFailure(new IOException("Ошибка сервера"));
                }
            }
        });
    }

    public void changePassword(String newPassword, SBC_Callback callback) {
        String url = DOMAIN_NAME + "/auth/v1/user";

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{ \"password\": \"" + newPassword + "\" }";

        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("OK");
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Неизвестная ошибка";
                    callback.onFailure(new IOException("Ошибка сервера: " + response.code() + "\n" + errorBody));
                }
            }
        });
    }


    public void changeEmail(Context context, String newEmail, SBC_Callback callback) {
        JSONObject jsonBody = new JSONObject();
        SessionManager sessionManager = new SessionManager(context);

        try {
            jsonBody.put("target_user_id", sessionManager.getUserId());
            jsonBody.put("new_email", newEmail);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "rpc/change_user_email_verified")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + sessionManager.getBearerToken())
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
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Ошибка сервера: " + response.code()));
                }
            }
        });
    }


    public void uploadAvatar(Uri uri, String fileName, SBC_Callback callback, Context context) {
        String realPath = RealPathUtil.getRealPath(context, uri);
        if (realPath == null) {
            callback.onFailure(new IOException("Не удалось получить путь файла"));
            return;
        }

        File file = new File(realPath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, requestBody)
                .build();

        String url = DOMAIN_NAME + "/storage/v1/object/avatars/" + fileName;

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Empty response";
                    callback.onFailure(new IOException("Upload failed: " + response.code() + ", Body: " + errorBody));
                }
            }
        });
    }

    public void fetchHotels(final SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "Hotels?select=*";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Failed to fetch hotels"));
                }
            }
        });
    }

    public void fetchHotelDetails(String hotelId, final SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "Hotels?id=eq." + hotelId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Failed to fetch hotel details"));
                }
            }
        });
    }

    public void checkFavorite(String userId, String hotelId, final FavoriteCallback callback) {
        String url = DOMAIN_NAME + REST_PATH + "favorites?user_id=eq." + userId + "&hotel_id=eq." + hotelId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    boolean exists = !body.equals("[]");
                    callback.onResult(exists);
                } else {
                    callback.onError(new IOException("Ошибка проверки избранного"));
                }
            }
        });
    }

    public void addFavorite(String userId, String hotelId, final SimpleCallback callback) {
        String json = "{ \"user_id\": \"" + userId + "\", \"hotel_id\": \"" + hotelId + "\" }";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json);

        String url = DOMAIN_NAME + REST_PATH + "favorites";

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Не удалось добавить в избранное"));
                }
            }
        });
    }

    public void removeFavorite(String userId, String hotelId, final SimpleCallback callback) {
        String url = DOMAIN_NAME + REST_PATH + "favorites?user_id=eq." + userId + "&hotel_id=eq." + hotelId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Не удалось удалить из избранного"));
                }
            }
        });
    }

    public void fetchFavoritesForUser(String userId, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "favorites?user_id=eq." + userId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Не удалось загрузить избранное"));
                }
            }
        });
    }

    public void fetchHotelById(String hotelId, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "Hotels?id=eq." + hotelId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Не удалось загрузить отель"));
                }
            }
        });
    }


    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
        void onError(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }
    public interface SBC_Callback {
        void onFailure(IOException e);
        void onResponse(String responseBody);
    }

    }
