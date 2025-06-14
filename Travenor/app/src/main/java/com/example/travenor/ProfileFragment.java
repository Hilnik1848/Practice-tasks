package com.example.travenor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.ProfileResponse;
import com.google.gson.Gson;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileEmail;
    private ImageView profileImage, editIcon;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileImage = view.findViewById(R.id.profileImage);
        editIcon = view.findViewById(R.id.editIcon);
        logoutButton = view.findViewById(R.id.logoutButton);

        loadUserProfile();

        logoutButton.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.logoutUser();

            Intent intent = new Intent(requireContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (requireActivity() != null) {
                requireActivity().finish();
            }
        });

        editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }


    private void loadUserProfile() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchUserProfile(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(requireContext(), "Ошибка загрузки профиля: " + e.getMessage(), Toast.LENGTH_LONG).show()
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

                            SessionManager sessionManager = new SessionManager(requireContext());
                            sessionManager.saveFullName(profile.getFull_name());

                            profileName.setText(profile.getFull_name());

                            profileEmail.setText(sessionManager.getEmail());

                            if (profile.getAvatar_url() != null && !profile.getAvatar_url().isEmpty()) {
                                String avatarUrl = profile.getAvatar_url();

                                if (!avatarUrl.startsWith("http")) {
                                    avatarUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/avatars/"  + avatarUrl;
                                }

                                Glide.with(requireContext())
                                        .load(avatarUrl)
                                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                        .error(android.R.drawable.ic_menu_report_image)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(profileImage);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Профиль не найден", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Ошибка парсинга данных", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}