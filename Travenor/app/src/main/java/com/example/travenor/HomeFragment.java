package com.example.travenor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.ProfileResponse;
import com.google.gson.Gson;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private TextView usernameText;
    private ImageView profileIcon;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        usernameText = view.findViewById(R.id.usernameText);
        profileIcon = view.findViewById(R.id.profileIcon);

        loadUserProfile();

        return view;
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
}