package com.example.travenor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travenor.Models.BookingRequest;
import com.example.travenor.Models.Chat;
import com.example.travenor.Models.HotelRoom;
import com.example.travenor.Models.Manager;
import com.example.travenor.Models.ProfileUpdate;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class BookingFormActivity extends AppCompatActivity {

    private Spinner roomTypeSpinner;
    private List<HotelRoom> roomList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int guestCount = 1;
    private double basePrice = 0;
    private CalendarView calendarView;
    private TextView textViewStartDate, textViewEndDate, totalPriceText, guestCountText;
    private long startDateMillis = 0, endDateMillis = 0;
    private String hotelId, hotelName;
    private boolean isSelectingStartDate = true;
    private static final String SUPABASE_URL = "https://mmbdesfnabtcbpjwcwde.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bookingform);

        hotelId = getIntent().getStringExtra("hotel_id");
        hotelName = getIntent().getStringExtra("hotel_name");
        basePrice = getIntent().getDoubleExtra("hotel_price", 0);

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(this, "Ошибка: не передан ID отеля", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRoomSpinner();
        setupCalendar();
        setupGuestCounter();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.BackBTN);
        TextView titleTextView = findViewById(R.id.titleTextView);
        Button continueButton = findViewById(R.id.continueButton);
        roomTypeSpinner = findViewById(R.id.room_type);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        totalPriceText = findViewById(R.id.totalPriceText);
        calendarView = findViewById(R.id.calendarView);
        guestCountText = findViewById(R.id.guestCountText);

        titleTextView.setText("Бронирование: " + hotelName);
        btnBack.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> completeBooking());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomTypeSpinner.setAdapter(adapter);

        textViewStartDate.setOnClickListener(v -> {
            isSelectingStartDate = true;
            Toast.makeText(this, "Выберите дату заезда", Toast.LENGTH_SHORT).show();
        });

        textViewEndDate.setOnClickListener(v -> {
            if (startDateMillis == 0) {
                Toast.makeText(this, "Сначала выберите дату заезда", Toast.LENGTH_SHORT).show();
                return;
            }
            isSelectingStartDate = false;
            Toast.makeText(this, "Выберите дату выезда", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRoomSpinner() {
        loadRooms(hotelId);
        roomTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!roomList.isEmpty()) {
                    basePrice = roomList.get(position).getPrice_per_night();
                    updateTotalPrice();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupCalendar() {
        calendarView.setMinDate(System.currentTimeMillis());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            long selectedDate = calendar.getTimeInMillis();

            if (selectedDate < System.currentTimeMillis()) {
                Toast.makeText(BookingFormActivity.this,
                        "Нельзя выбрать прошедшую дату",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSelectingStartDate) {
                startDateMillis = selectedDate;
                textViewStartDate.setText(formatDate(startDateMillis));
                endDateMillis = 0;
                textViewEndDate.setText("--");
            } else {
                if (selectedDate > startDateMillis) {
                    endDateMillis = selectedDate;
                    textViewEndDate.setText(formatDate(endDateMillis));
                    updateTotalPrice();
                } else {
                    Toast.makeText(BookingFormActivity.this,
                            "Дата выезда должна быть после даты заезда",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupGuestCounter() {
        ImageButton btnMinus = findViewById(R.id.btnMinusGuest);
        ImageButton btnPlus = findViewById(R.id.btnPlusGuest);
        btnMinus.setOnClickListener(v -> {
            if (guestCount > 1) {
                guestCount--;
                guestCountText.setText(String.valueOf(guestCount));
                updateTotalPrice();
            }
        });
        btnPlus.setOnClickListener(v -> {
            if (guestCount < 10) {
                guestCount++;
                guestCountText.setText(String.valueOf(guestCount));
                updateTotalPrice();
            }
        });
    }

    private void updateTotalPrice() {
        if (startDateMillis == 0 || endDateMillis == 0) return;
        long diff = endDateMillis - startDateMillis;
        int days = (int) TimeUnit.MILLISECONDS.toDays(diff);
        if (days <= 0) return;
        double total = basePrice * days * guestCount;
        totalPriceText.setText(String.format(Locale.getDefault(), "Итого: %.2f ₽", total));
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private void completeBooking() {
        if (startDateMillis == 0 || endDateMillis == 0) {
            Toast.makeText(this, "Выберите даты заезда и выезда", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String checkInDate = sdf.format(new Date(startDateMillis));
        String checkOutDate = sdf.format(new Date(endDateMillis));

        if (roomList.isEmpty() || roomTypeSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Выберите тип номера", Toast.LENGTH_SHORT).show();
            return;
        }

        long diff = endDateMillis - startDateMillis;
        int days = (int) TimeUnit.MILLISECONDS.toDays(diff);
        if (days <= 0) {
            Toast.makeText(this, "Некорректные даты", Toast.LENGTH_SHORT).show();
            return;
        }

        HotelRoom selectedRoom = roomList.get(roomTypeSpinner.getSelectedItemPosition());
        double totalSum = selectedRoom.getPrice_per_night() * days * guestCount;

        BookingRequest booking = new BookingRequest(
                userId,
                checkInDate,
                checkOutDate,
                guestCount,
                selectedRoom.getId(),
                Integer.parseInt(hotelId)
        );
        booking.setSumm(totalSum);

        sendBookingRequest(booking);
    }

    private void sendBookingRequest(BookingRequest booking) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json")
                            .header("Prefer", "return=representation")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);
        Call<Void> call = apiService.createBooking(booking);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    getManagerAndCreateChat(booking.getUser_id(), booking.getHotel_id());
                    Toast.makeText(BookingFormActivity.this, "Бронирование успешно создано!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "empty";
                        Toast.makeText(BookingFormActivity.this,
                                "Ошибка создания бронирования: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("API_ERROR", "Error parsing error response", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BookingFormActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getManagerAndCreateChat(String userId, int hotelId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);
        Call<List<Manager>> call = apiService.getManagerByHotelId(
                "eq." + hotelId,
                "id,user_id,hotel_id"
        );

        call.enqueue(new Callback<List<Manager>>() {
            @Override
            public void onResponse(Call<List<Manager>> call, Response<List<Manager>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Manager manager = response.body().get(0);
                    checkAndCreateChat(userId, manager.getId(), hotelId);
                } else {
                    Log.e("API_ERROR", "Не удалось найти менеджера для отеля: " + hotelId);
                }
            }

            @Override
            public void onFailure(Call<List<Manager>> call, Throwable t) {
                Log.e("API_ERROR", "Ошибка сети при поиске менеджера", t);
            }
        });
    }

    private void checkAndCreateChat(String userId, int managerId, int hotelId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);

        Call<List<Chat>> call = apiService.checkExistingChat(
                "eq." + userId,
                "eq." + managerId,
                "eq." + hotelId,
                "id"
        );

        call.enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful()) {
                    if (response.body() == null || response.body().isEmpty()) {
                        createNewChat(userId, managerId, hotelId);
                    } else {
                        Log.d("CHAT", "Чат уже существует: " + new Gson().toJson(response.body()));
                    }
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("API_ERROR", "Ошибка проверки чата: " + error);
                    } catch (IOException e) {
                        Log.e("API_ERROR", "Ошибка чтения ошибки", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Log.e("API_ERROR", "Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void createNewChat(String userId, int managerId, int hotelId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json")
                            .header("Prefer", "return=representation")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Chat newChat = new Chat();
        newChat.setUser_id(userId);
        newChat.setManager_id(managerId);
        newChat.setHotel_id(hotelId);

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);
        Call<Chat> call = apiService.createChat(newChat);

        call.enqueue(new Callback<Chat>() {
            @Override
            public void onResponse(Call<Chat> call, Response<Chat> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Chat createdChat = response.body();
                    Log.d("CHAT", "Новый чат создан: " + new Gson().toJson(createdChat));
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("API_ERROR", "Ошибка создания чата: " + error);
                    } catch (IOException e) {
                        Log.e("API_ERROR", "Ошибка чтения ошибки", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Chat> call, Throwable t) {
                Log.e("API_ERROR", "Ошибка сети при создании чата: " + t.getMessage());
            }
        });
    }

    public interface SupabaseApiService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY,
                "Content-Type: application/json",
                "Prefer: return=representation"
        })
        @POST("bookings")
        Call<Void> createBooking(@Body BookingRequest request);

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("hotel_room")
        Call<List<HotelRoom>> getRoomsByHotelId(
                @Query("hostel_id") String hostelId,
                @Query("select") String select
        );

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("managers")
        Call<List<Manager>> getManagerByHotelId(
                @Query("hotel_id") String hotelId,
                @Query("select") String select
        );

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer "
        })
        @GET("chats")
        Call<List<Chat>> checkExistingChat(
                @Query("user_id") String userId,
                @Query("manager_id") String managerId,
                @Query("hotel_id") String hotelId,
                @Query("select") String select
        );

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY,
                "Content-Type: application/json",
                "Prefer: return=representation"
        })
        @POST("chats")
        Call<Chat> createChat(@Body Chat chat);

        @Headers({
                "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk",
                "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tYmRlc2ZuYWJ0Y2Jwandjd2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5NTg4MDMsImV4cCI6MjA2NDUzNDgwM30.zU9xsd7HMVuLi6OkiKTaB723ek2YNomMgrqnKKvSvQk"
        })
        @GET("chats")
        Call<List<Chat>> getAllChats(@Query("select") String select);

        @GET("managers")
        Call<List<Manager>> getManagerById(@Query("id") String managerId);

        @GET("profiles")
        Call<ProfileUpdate> getProfileByUserId(@Query("id") String userId);

        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY
        })
        @GET("chats")
        Call<List<Chat>> getAllChats(
                @Query("user_id") String userId,
                @Query("select") String select
        );
    }

    private void loadRooms(String hotelId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseApiService apiService = retrofit.create(SupabaseApiService.class);
        Call<List<HotelRoom>> call = apiService.getRoomsByHotelId("eq." + hotelId, "id,room_type,price_per_night,hostel_id");

        call.enqueue(new Callback<List<HotelRoom>>() {
            @Override
            public void onResponse(Call<List<HotelRoom>> call, Response<List<HotelRoom>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList = response.body();
                    if (roomList.isEmpty()) {
                        Toast.makeText(BookingFormActivity.this, "Нет доступных номеров", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> roomTypes = new ArrayList<>();
                    for (HotelRoom room : roomList) {
                        roomTypes.add(room.getRoom_type());
                    }

                    adapter.clear();
                    adapter.addAll(roomTypes);
                    adapter.notifyDataSetChanged();

                    basePrice = roomList.get(0).getPrice_per_night();
                    updateTotalPrice();
                } else {
                    Toast.makeText(BookingFormActivity.this, "Ошибка загрузки номеров: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<HotelRoom>> call, Throwable t) {
                Toast.makeText(BookingFormActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}