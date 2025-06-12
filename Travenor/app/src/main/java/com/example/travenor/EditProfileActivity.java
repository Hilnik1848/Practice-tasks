package com.example.travenor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.travenor.Models.DataBinding;
import com.example.travenor.Models.ProfileUpdate;

import java.io.IOException;


public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editPassword, editEmail;
    ImageView profileImage;
    Button saveChangesBtn;

    SessionManager sessionManager;
    private String savedName, savedEmail, savedPassword;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.currentEmailText);
        editPassword = findViewById(R.id.editPassword);
        profileImage = findViewById(R.id.profileImage);
        saveChangesBtn = findViewById(R.id.saveChangesBtn);

        sessionManager = new SessionManager(this);

        savedName = sessionManager.getFullName();
        savedEmail = sessionManager.getEmail();
        savedPassword = sessionManager.getPassword();

        if (savedName != null && !savedName.isEmpty()) {
            editName.setText(savedName);
        }

        if (savedEmail != null && !savedEmail.isEmpty()) {
            editEmail.setText(savedEmail);
        }

        if (savedPassword != null && !savedPassword.isEmpty()) {
            editPassword.setText(savedPassword);
        }

        profileImage.setOnClickListener(v -> openGallery());
        saveChangesBtn.setOnClickListener(v -> saveAllChanges());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
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
            Toast.makeText(this, "Нет изменений", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        if (isNameChanged) {
            sessionManager.saveFullName(newName);
            supabaseClient.updateProfile(DataBinding.getUuidUser(), new ProfileUpdate(newName), new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this, "Ошибка обновления имени", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this, "Имя обновлено на сервере", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }

        if (isEmailChanged) {
            supabaseClient.changeEmail(EditProfileActivity.this, newEmail, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this, "Ошибка смены email", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        sessionManager.saveEmail(newEmail);
                        Toast.makeText(EditProfileActivity.this, "Email обновлён", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

        if (isPasswordChanged) {
            supabaseClient.changePassword(newPassword, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this, "Ошибка смены пароля", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        sessionManager.savePassword(newPassword);
                        Toast.makeText(EditProfileActivity.this, "Пароль изменён", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

        if (selectedImageUri != null) {
            String fileName = "profile_" + DataBinding.getUuidUser() + ".png";
            supabaseClient.uploadAvatar(selectedImageUri, fileName, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    Log.e("UploadError", "Ошибка загрузки", e);
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this, "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(String responseBody) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Фото загружено", Toast.LENGTH_SHORT).show();

                        ProfileUpdate profileUpdate = new ProfileUpdate();
                        profileUpdate.setAvatar_url(fileName);

                        supabaseClient.updateProfile(DataBinding.getUuidUser(), profileUpdate, new SupabaseClient.SBC_Callback() {
                            @Override
                            public void onFailure(IOException e) {
                                Log.e("AvatarUpdate", "Ошибка обновления ссылки на фото", e);
                                Toast.makeText(EditProfileActivity.this, "Не удалось обновить ссылку на фото", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String responseBody) {
                                Toast.makeText(EditProfileActivity.this, "Ссылка на фото обновлена", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }, EditProfileActivity.this);
        } else {
            Toast.makeText(this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
        }}
}