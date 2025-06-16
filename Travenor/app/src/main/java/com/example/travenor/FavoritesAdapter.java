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


import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Hotel> hotels;
    private final Context context;
    private final SupabaseClient supabaseClient;

    public interface OnRemoveListener {
        void onRemoved(int position);
    }

    public FavoritesAdapter(List<Hotel> hotels, Context context) {
        this.hotels = hotels != null ? new ArrayList<>(hotels) : new ArrayList<>();
        this.context = context;
        this.supabaseClient = new SupabaseClient();
    }

    public void updateHotels(List<Hotel> newHotels) {
        if (newHotels == null) return;
        hotels.clear();
        hotels.addAll(newHotels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_hotel, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);

        holder.hotelName.setText(hotel.getName());
        holder.hotelLocation.setText(hotel.getAddres());
        holder.hotelRating.setRating((float) hotel.getRating());

        String imageUrl = hotel.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http")) {
                imageUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/hotel/"  + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img3)
                    .error(R.drawable.img3)
                    .into(holder.hotelImage);
        }

        holder.itemView.setOnClickListener(v ->
                HotelDetailActivity.start(context, hotel.getId())
        );

        holder.favoriteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos == RecyclerView.NO_POSITION) return;

            String userId = DataBinding.getUuidUser();
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(context, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
                return;
            }

            supabaseClient.removeFavorite(userId, hotel.getId(), new SupabaseClient.SimpleCallback() {
                @Override
                public void onSuccess() {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        hotels.remove(pos);
                        notifyItemRemoved(pos);
                        Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView hotelImage;
        TextView hotelName;
        TextView hotelLocation;
        RatingBar hotelRating;
        ImageButton favoriteButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotel_image);
            hotelName = itemView.findViewById(R.id.hotel_name);
            hotelLocation = itemView.findViewById(R.id.hotel_location);
            hotelRating = itemView.findViewById(R.id.hotel_rating);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }
    }
}