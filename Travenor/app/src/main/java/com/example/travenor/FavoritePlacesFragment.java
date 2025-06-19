package com.example.travenor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.FavoriteWithHotel;
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
                        Log.d("ServerResponse", responseBody);

                        Type listType = new TypeToken<ArrayList<FavoriteWithHotel>>(){}.getType();
                        List<FavoriteWithHotel> favorites = new Gson().fromJson(responseBody, listType);

                        favoriteHotels.clear();

                        if (favorites != null && !favorites.isEmpty()) {
                            for (FavoriteWithHotel item : favorites) {
                                Hotel hotel = item.getHotel();
                                if (hotel != null) {
                                    favoriteHotels.add(hotel);
                                }
                            }
                        }

                        adapter.updateHotels(favoriteHotels);

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Ошибка анализа данных", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}