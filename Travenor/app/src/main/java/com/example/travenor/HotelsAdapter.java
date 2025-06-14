package com.example.travenor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Hotel;
import com.example.travenor.R;

import java.util.List;

public class HotelsAdapter extends RecyclerView.Adapter<HotelsAdapter.HotelViewHolder> {
    private List<Hotel> hotels;
    private Context context;
    private OnHotelClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
        void onHotelLongClick(Hotel hotel);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Hotel hotel);
    }

    public HotelsAdapter(List<Hotel> hotels, Context context, OnHotelClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.hotels = hotels;
        this.context = context;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);

        holder.hotelName.setText(hotel.getName());
        holder.hotelLocation.setText(hotel.getLocation());
        holder.hotelRating.setRating((float) hotel.getRating());

        SupabaseClient supabaseClient = new SupabaseClient();


        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            String imageUrl = hotel.getImageUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/hotel/" + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img3)
                    .error(R.drawable.img3)
                    .into(holder.hotelImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHotelClick(hotel);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onHotelLongClick(hotel);
            }
            return true;
        });

        holder.favoriteButton.setOnClickListener(v -> {
            String userId = DataBinding.getUuidUser();
            String hotelId = hotel.getId();

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(context, "Пожалуйста, войдите в аккаунт", Toast.LENGTH_SHORT).show();
                return;
            }

            supabaseClient.checkFavorite(userId, hotelId, new SupabaseClient.FavoriteCallback() {
                @Override
                public void onResult(boolean isFavorite) {
                    if (isFavorite) {
                        supabaseClient.removeFavorite(userId, hotelId, new SupabaseClient.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    holder.favoriteButton.setImageResource(R.drawable.like);
                                    Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    } else {
                        supabaseClient.addFavorite(userId, hotelId, new SupabaseClient.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    holder.favoriteButton.setImageResource(R.drawable.like);
                                    Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(context, "Ошибка добавления", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Ошибка проверки избранного", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView hotelImage;
        TextView hotelName;
        TextView hotelLocation;
        RatingBar hotelRating;
        ImageButton favoriteButton;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotel_image);
            hotelName = itemView.findViewById(R.id.hotel_name);
            hotelLocation = itemView.findViewById(R.id.hotel_location);
            hotelRating = itemView.findViewById(R.id.hotel_rating);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }
    }
}