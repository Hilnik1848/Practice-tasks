package com.example.travenor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.ProfileUpdate;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class EditProfileFragment extends Fragment {

    private EditText editName, editPassword, editEmail;
    private ImageView profileImage;
    private MaterialButton saveChangesBtn;
    private SessionManager sessionManager;
    private String savedName, savedEmail, savedPassword;
    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_profile, container, false);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.currentEmailText);
        editPassword = view.findViewById(R.id.editPassword);
        profileImage = view.findViewById(R.id.profileImage);
        saveChangesBtn = view.findViewById(R.id.saveChangesBtn);

        sessionManager = new SessionManager(requireContext());

        savedName = sessionManager.getFullName();
        savedEmail = sessionManager.getEmail();
        savedPassword = sessionManager.getPassword();

        if (savedName != null && !savedName.isEmpty()) editName.setText(savedName);
        if (savedEmail != null && !savedEmail.isEmpty()) editEmail.setText(savedEmail);
        if (savedPassword != null && !savedPassword.isEmpty()) editPassword.setText(savedPassword);

        profileImage.setOnClickListener(v -> openGallery());
        saveChangesBtn.setOnClickListener(v -> saveAllChanges());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(profileImage);
        }
    }

    private void saveAllChanges() {
        String newName = editName.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        boolean isNameChanged = !newName.isEmpty() && !newName.equals(savedName);
        boolean isEmailChanged = !newEmail.isEmpty() && !newEmail.equals(savedEmail);
        boolean isPasswordChanged = !newPassword.isEmpty() && !newPassword.equals(savedPassword);

        if (!isNameChanged && !isEmailChanged && !isPasswordChanged && selectedImageUri == null) {
            showToast("Нет изменений");
            requireActivity().onBackPressed();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        AtomicInteger completedUpdates = new AtomicInteger(0);
        int totalUpdates = 0;

        if (isNameChanged) totalUpdates++;
        if (isEmailChanged) totalUpdates++;
        if (isPasswordChanged) totalUpdates++;
        if (selectedImageUri != null) totalUpdates++;

        final int finalTotalUpdates = totalUpdates;

        Runnable onTaskComplete = () -> {
            if (completedUpdates.incrementAndGet() == finalTotalUpdates && isAdded() && !isDetached()) {
                showToast("Данные успешно обновлены");
                requireActivity().onBackPressed();
            }
        };

        if (isNameChanged) {
            sessionManager.saveFullName(newName);
            supabaseClient.updateProfile(DataBinding.getUuidUser(), new ProfileUpdate(newName), new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    showToast("Ошибка обновления имени");
                    onTaskComplete.run();
                }

                @Override
                public void onResponse(String responseBody) {
                    showToast("Имя обновлено");
                    onTaskComplete.run();
                }
            });
        }

        if (isEmailChanged) {
            supabaseClient.changeEmail(requireContext(), newEmail, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    showToast("Ошибка смены email");
                    onTaskComplete.run();
                }

                @Override
                public void onResponse(String responseBody) {
                    sessionManager.saveEmail(newEmail);
                    showToast("Email обновлён");
                    onTaskComplete.run();
                }
            });
        }

        if (isPasswordChanged) {
            supabaseClient.changePassword(newPassword, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    showToast("Ошибка смены пароля");
                    onTaskComplete.run();
                }

                @Override
                public void onResponse(String responseBody) {
                    sessionManager.savePassword(newPassword);
                    showToast("Пароль изменён");
                    onTaskComplete.run();
                }
            });
        }

        if (selectedImageUri != null) {
            String fileName = "profile_" + DataBinding.getUuidUser() + ".png";
            supabaseClient.uploadAvatar(selectedImageUri, fileName, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    Log.e("UploadError", "Ошибка загрузки", e);
                    showToast("Ошибка загрузки фото");
                    onTaskComplete.run();
                }

                @Override
                public void onResponse(String responseBody) {
                    showToast("Фото загружено");
                    ProfileUpdate profileUpdate = new ProfileUpdate();
                    profileUpdate.setAvatar_url(fileName);
                    supabaseClient.updateProfile(DataBinding.getUuidUser(), profileUpdate, new SupabaseClient.SBC_Callback() {
                        @Override
                        public void onFailure(IOException e) {
                            showToast("Не удалось обновить ссылку на фото");
                            onTaskComplete.run();
                        }

                        @Override
                        public void onResponse(String responseBody) {
                            showToast("Ссылка на фото обновлена");
                            onTaskComplete.run();
                        }
                    });
                }
            }, requireContext());
        } else {
            onTaskComplete.run();
        }
    }

    private void showToast(String message) {
        if (isAdded() && !isDetached()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}