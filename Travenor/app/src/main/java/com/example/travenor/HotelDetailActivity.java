package com.example.travenor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.Hotel;
import com.example.travenor.Models.DataBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HotelDetailActivity extends AppCompatActivity {
    private static final String EXTRA_HOTEL_ID = "hotel_id";

    private ImageView hotelImage;
    private TextView hotelName;
    private TextView hotelLocation;
    private TextView hotelDescription;
    private TextView hotelPrice;
    private RatingBar hotelRating;

    public static void start(Context context, String hotelId) {
        Intent intent = new Intent(context, HotelDetailActivity.class);
        intent.putExtra(EXTRA_HOTEL_ID, hotelId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        initViews();
        loadHotelDetails();
    }

    private void initViews() {
        hotelImage = findViewById(R.id.hotel_detail_image);
        hotelName = findViewById(R.id.hotel_detail_name);
        hotelLocation = findViewById(R.id.hotel_detail_location);
        hotelDescription = findViewById(R.id.hotel_detail_description);
        hotelPrice = findViewById(R.id.hotel_detail_price);
        hotelRating = findViewById(R.id.hotel_detail_rating);
    }

    private void loadHotelDetails() {
        String hotelId = getIntent().getStringExtra(EXTRA_HOTEL_ID);
        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(this, "Ошибка: ID отеля не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchHotelDetails(hotelId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(HotelDetailActivity.this, "Ошибка загрузки данных отеля", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(String responseBody) {
                runOnUiThread(() -> {
                    try {
                        Type listType = new TypeToken<ArrayList<Hotel>>(){}.getType();
                        List<Hotel> hotels = new Gson().fromJson(responseBody, listType);

                        if (hotels != null && !hotels.isEmpty()) {
                            displayHotelDetails(hotels.get(0));
                        } else {
                            Toast.makeText(HotelDetailActivity.this, "Отель не найден", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(HotelDetailActivity.this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void displayHotelDetails(Hotel hotel) {
        hotelName.setText(hotel.getName());
        hotelLocation.setText(hotel.getLocation());
        hotelDescription.setText(hotel.getDescription());
        hotelRating.setRating((float) hotel.getRating());

        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            String imageUrl = hotel.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/hotel/" + imageUrl;
            }

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(hotelImage);
        }
    }
}