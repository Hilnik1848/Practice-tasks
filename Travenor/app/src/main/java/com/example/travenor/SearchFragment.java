package com.example.travenor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.Hotel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements
        HotelsAdapterFilter.OnHotelClickListener,
        HotelsAdapterFilter.OnFavoriteClickListener,
        HotelsAdapterFilter.OnFilterListener {

    private SearchView searchView;
    private ImageButton filterButton;
    private TextView headerTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView hotelsRecyclerView;
    private HotelsAdapterFilter hotelsAdapter;
    private List<Hotel> hotels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.search);
        filterButton = view.findViewById(R.id.filter);
        headerTitle = view.findViewById(R.id.headerTitle);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        hotelsRecyclerView = view.findViewById(R.id.hotels_recycler_view);

        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupFilterButtonWithDialog();
        loadHotels();

        return view;
    }

    private void setupRecyclerView() {
        hotelsAdapter = new HotelsAdapterFilter(hotels, getContext(), this, this);
        hotelsAdapter.setFilterListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        hotelsRecyclerView.setLayoutManager(layoutManager);
        hotelsRecyclerView.setAdapter(hotelsAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                hotelsAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadHotels();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
    }

    private void setupFilterButtonWithDialog() {
        filterButton.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment filterDialog = new FilterBottomSheetDialogFragment();
            filterDialog.setOnApplyFilterListener(new FilterBottomSheetDialogFragment.OnApplyFilterListener() {
                @Override
                public void onApplyFilter(String country, float rating, List<String> amenities) {
                    hotelsAdapter.setFilterCountry(country);
                    hotelsAdapter.setFilterMinRating(rating);
                    hotelsAdapter.setFilterAmenities(amenities);
                    hotelsAdapter.getFilter().filter("");
                }

                @Override
                public void onResetFilter() {
                    hotelsAdapter.setFilterCountry("");
                    hotelsAdapter.setFilterMinRating(0f);
                    hotelsAdapter.setFilterAmenities(new ArrayList<>());
                    hotelsAdapter.getFilter().filter("");
                }
            });
            filterDialog.show(getChildFragmentManager(), "FilterBottomSheet");
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
                        hotelsAdapter.updateData(newHotels);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onFilterComplete(int count) {
        headerTitle.setText("Найдено " + count + " отелей");
    }

    @Override
    public void onHotelClick(Hotel hotel) {
        HotelDetailActivity.start(getContext(), hotel.getId());
    }

    @Override
    public void onHotelLongClick(Hotel hotel) {
        Toast.makeText(getContext(), "Отель: " + hotel.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Hotel hotel) {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Пожалуйста, войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.toggleFavorite(userId, hotel.getId(), new SupabaseClient.SimpleCallback() {
            @Override
            public void onSuccess() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Избранное обновлено", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}