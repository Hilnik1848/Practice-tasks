package com.example.travenor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travenor.FavoritesAdapter;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Favorite;
import com.example.travenor.Models.Hotel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritePlacesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private List<Hotel> favoriteHotels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_places, container, false);

        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> requireActivity().finish());

        recyclerView = view.findViewById(R.id.favoritePlacesRecyclerView);
        setupRecyclerView();
        loadFavorites();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(favoriteHotels, requireContext());
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchFavoritesForUser(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(requireContext(), "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(String responseBody) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Type listType = new TypeToken<ArrayList<Favorite>>(){}.getType();
                        List<Favorite> favorites = new Gson().fromJson(responseBody, listType);

                        if (favorites == null || favorites.isEmpty()) {
                            Toast.makeText(requireContext(), "Избранное пусто", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        favoriteHotels.clear();

                        for (Favorite fav : favorites) {
                            loadHotelData(supabaseClient, fav.getHotelId());
                        }

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Ошибка анализа избранного", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadHotelData(SupabaseClient supabaseClient, String hotelId) {
        supabaseClient.fetchHotelById(hotelId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(requireContext(), "Ошибка загрузки отеля: " + hotelId, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(String hotelResponse) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Type hotelType = new TypeToken<ArrayList<Hotel>>(){}.getType();
                        List<Hotel> hotels = new Gson().fromJson(hotelResponse, hotelType);

                        if (hotels != null && !hotels.isEmpty()) {
                            favoriteHotels.add(hotels.get(0));
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(requireContext(), "Отель не найден: " + hotelId, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Ошибка данных отеля: " + hotelId, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}