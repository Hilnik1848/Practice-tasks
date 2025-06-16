package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Hotel;
import com.example.travenor.Models.ProfileResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements HotelsAdapter.OnHotelClickListener {

    private TextView usernameText;
    private ImageView profileIcon,heartButton;
    private RecyclerView hotelsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HotelsAdapter hotelsAdapter;
    private List<Hotel> hotels = new ArrayList<>();

    private Set<String> favoriteHotelIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        usernameText = view.findViewById(R.id.usernameText);
        profileIcon = view.findViewById(R.id.profileIcon);
        hotelsRecyclerView = view.findViewById(R.id.destinationsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        heartButton = view.findViewById(R.id.heartButton);

        heartButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FavoritePlacesActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        setupSwipeRefresh();

        loadUserProfile();
        loadHotels();

        return view;
    }

    private void setupRecyclerView() {
        hotelsAdapter = new HotelsAdapter(hotels, getContext(), this, hotel -> {
            if (favoriteHotelIds.contains(hotel.getId())) {
                favoriteHotelIds.remove(hotel.getId());
                Toast.makeText(getContext(), hotel.getName() + " удалён из избранного", Toast.LENGTH_SHORT).show();
            } else {
                favoriteHotelIds.add(hotel.getId());
                Toast.makeText(getContext(), hotel.getName() + " добавлен в избранное", Toast.LENGTH_SHORT).show();
            }
            hotelsAdapter.notifyDataSetChanged();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        hotelsRecyclerView.setLayoutManager(layoutManager);
        hotelsRecyclerView.setAdapter(hotelsAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadHotels();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
    }

    private void loadUserProfile() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            usernameText.setText("Гость");
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchUserProfile(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        usernameText.setText("Ошибка загрузки")
                );
            }

            @Override
            public void onResponse(String responseBody) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Gson gson = new Gson();
                        ProfileResponse[] profiles = gson.fromJson(responseBody, ProfileResponse[].class);

                        if (profiles != null && profiles.length > 0) {
                            ProfileResponse profile = profiles[0];

                            usernameText.setText(profile.getFull_name());

                            if (profile.getAvatar_url() != null && !profile.getAvatar_url().isEmpty()) {
                                String avatarUrl = profile.getAvatar_url();

                                if (!avatarUrl.startsWith("http")) {
                                    avatarUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/avatars/"  + avatarUrl;
                                }

                                Glide.with(requireContext())
                                        .load(avatarUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(profileIcon);
                            }
                        }
                    } catch (Exception e) {
                        usernameText.setText("Ошибка данных");
                    }
                });
            }
        });
    }

    private void loadHotels() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchHotels(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Ошибка загрузки отелей: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(String responseBody) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Type listType = new TypeToken<ArrayList<Hotel>>(){}.getType();
                        List<Hotel> newHotels = new Gson().fromJson(responseBody, listType);

                        hotels.clear();
                        hotels.addAll(newHotels);
                        hotelsAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onHotelClick(Hotel hotel) {
        HotelDetailActivity.start(getContext(), hotel.getId());
    }

    @Override
    public void onHotelLongClick(Hotel hotel) {
        showContextMenu(hotel);
    }

    private void showContextMenu(Hotel hotel) {
        Toast.makeText(getContext(), "Отель: " + hotel.getName(), Toast.LENGTH_SHORT).show();
    }
}