package com.example.travenor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.Hotel;
import java.util.ArrayList;
import java.util.List;

public class HotelsAdapterFilter extends RecyclerView.Adapter<HotelsAdapterFilter.HotelViewHolder>
        implements Filterable {

    private String filterCountry = "";
    private float filterMinRating = 0f;
    private List<String> filterAmenities = new ArrayList<>();
    private List<Hotel> hotels;
    private List<Hotel> hotelsFull;
    private Context context;
    private OnHotelClickListener listener;
    private OnFavoriteClickListener favoriteListener;
    private OnFilterListener filterListener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
        void onHotelLongClick(Hotel hotel);
    }


    public void setFilterCountry(String country) {
        this.filterCountry = country;
    }

    public void setFilterMinRating(float minRating) {
        this.filterMinRating = minRating;
    }

    public void setFilterAmenities(List<String> amenities) {
        this.filterAmenities = amenities;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Hotel hotel);
    }

    public interface OnFilterListener {
        void onFilterComplete(int count);
    }

    public HotelsAdapterFilter(List<Hotel> hotels, Context context,
                               OnHotelClickListener listener,
                               OnFavoriteClickListener favoriteListener) {
        this.hotels = hotels;
        this.hotelsFull = new ArrayList<>(hotels);
        this.context = context;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    public void setFilterListener(OnFilterListener filterListener) {
        this.filterListener = filterListener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hotel_item, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    @Override
    public Filter getFilter() {
        return hotelFilter;
    }
    private String extractCountryFromAddress(String address) {
        String[] parts = address.split(",");
        if (parts.length > 0) {
            return parts[parts.length - 1].trim();
        }
        return "";
    }
    private Filter hotelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Hotel> filteredList = new ArrayList<>();

            String filterPattern = constraint.toString().toLowerCase().trim();

            if ((constraint == null || constraint.length() == 0)
                    && filterCountry.isEmpty()
                    && filterMinRating == 0f
                    && filterAmenities.isEmpty()) {
                filteredList.addAll(hotelsFull);
            } else {
                for (Hotel hotel : hotelsFull) {
                    boolean matchesQuery = filterPattern.isEmpty() ||
                            hotel.getName().toLowerCase().contains(filterPattern) ||
                            containsIgnoreCasePartial(hotel.getName(), filterPattern);

                    String hotelAddress = hotel.getAddres().toLowerCase();
                    String hotelCountry = extractCountryFromAddress(hotelAddress);
                    boolean matchesCountry = filterCountry.isEmpty() ||
                            hotelCountry.contains(filterCountry.toLowerCase());

                    boolean matchesRating = hotel.getRating() >= filterMinRating;

                    boolean matchesAmenities = filterAmenities.isEmpty();
                    if (!filterAmenities.isEmpty()) {
                        String description = hotel.getDescription().toLowerCase();
                        for (String amenity : filterAmenities) {
                            if (description.contains(amenity)) {
                                matchesAmenities = true;
                                break;
                            }
                        }
                    }

                    if (matchesQuery && matchesCountry && matchesRating && matchesAmenities) {
                        filteredList.add(hotel);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        private boolean containsIgnoreCasePartial(String fullText, String query) {
            if (fullText == null || query == null) return false;
            return fullText.toLowerCase().contains(query);
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            hotels.clear();
            hotels.addAll((List<? extends Hotel>) results.values);
            notifyDataSetChanged();

            if (filterListener != null) {
                filterListener.onFilterComplete(results.count);
            }
        }
    };

    public void updateData(List<Hotel> newHotels) {
        hotelsFull = new ArrayList<>(newHotels);
        hotels = new ArrayList<>(newHotels);
        notifyDataSetChanged();

        if (filterListener != null) {
            filterListener.onFilterComplete(hotels.size());
        }
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(Hotel hotel) {
            hotelName.setText(hotel.getName());
            hotelLocation.setText(hotel.getAddres());
            hotelRating.setRating((float) hotel.getRating());

            if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
                String imageUrl = hotel.getImageUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/hotel/" + imageUrl;
                }
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.img3)
                        .error(R.drawable.img3)
                        .into(hotelImage);
            }



            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHotelClick(hotel);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onHotelLongClick(hotel);
                }
                return true;
            });

            favoriteButton.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteClick(hotel);
                }
            });
        }
    }
}