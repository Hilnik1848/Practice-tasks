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
import com.example.travenor.Models.HotelRoom;
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

            if (startDateMillis == 0 || (endDateMillis != 0 && selectedDate < startDateMillis)) {
                startDateMillis = selectedDate;
                textViewStartDate.setText(formatDate(startDateMillis));
                endDateMillis = 0;
                textViewEndDate.setText("--");
            } else if (selectedDate > startDateMillis) {
                endDateMillis = selectedDate;
                textViewEndDate.setText(formatDate(endDateMillis));
                updateTotalPrice();
            } else {
                Toast.makeText(this, "Дата выезда должна быть после даты заезда", Toast.LENGTH_SHORT).show();
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
        Gson gson = new Gson();
        String jsonBody = gson.toJson(booking);
        Log.d("API_REQUEST", "Sending booking: " + jsonBody);

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
                    Log.d("API_REQUEST", "Headers: " + request.headers());
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
                    Toast.makeText(BookingFormActivity.this, "Бронирование успешно создано!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "empty";
                        Log.e("API_ERROR", "Error: " + response.code() + " - " + errorBody);
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
                Log.e("API_ERROR", "Network error", t);
            }
        });
    }

    public interface SupabaseApiService {
        @Headers({
                "apikey: " + API_KEY,
                "Authorization: Bearer " + API_KEY,
                "Content-Type: application/json"
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
    }

    private void loadRooms(String hotelId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
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
                        String roomType = room.getRoom_type();
                        roomTypes.add(roomType != null ? roomType : "Неизвестный тип");
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