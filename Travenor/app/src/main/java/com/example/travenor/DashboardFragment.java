package com.example.travenor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travenor.Models.Booking;
import com.example.travenor.Models.DataBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule, container, false);
        recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ScheduleAdapter(bookingList, requireContext());
        recyclerView.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void loadBookings() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchBookingsForUser(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(requireContext(), "Ошибка загрузки брони", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Booking>>(){}.getType();
                List<Booking> bookings = gson.fromJson(responseBody, listType);

                bookingList.clear();
                if (bookings != null) {
                    bookingList.addAll(bookings);
                }

                new Handler(Looper.getMainLooper()).post(adapter::notifyDataSetChanged);
            }
        });
    }
}